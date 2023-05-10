// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.mediapipe.apps.facemeshgpu;

import android.os.Bundle;
import android.util.Log;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.lang.management.RuntimeErrorException;
import java.io.IOException;

import android.widget.TextView;
import android.widget.ImageView;

//import cosumo de api
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/** Main activity of MediaPipe face mesh app. */
public class FaceMesh extends com.google.mediapipe.apps.basic.MainActivity {
  private static final String TAG = "MainActivity";

  private static final String INPUT_NUM_FACES_SIDE_PACKET_NAME = "num_faces";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "multi_face_landmarks";
  // Max number of faces to detect/process.
  private static final int NUM_FACES = 1;

  private float ry1, ry2, ay1, ay2,la ,lb ,ratio ,ratiob ,totalBlinkTime, totalMopen;
  private TextView tv,con;
  private ImageView imgv;
  private long blinkDuration, mOpenDuration, startTime;
  private long IniObs;
  private long currentTime;
  private float perclos,perlov;
  private static final int TIME_OBS =15;
  private boolean eye_blinked, eye_open;
  private boolean mo, mc;


  //coneccion api

  private void post(){
    try {
      URL url = new URL("https://parpadeo.onrender.com/post/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      //String dato ="{}";
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);
      String jsonInputString = "{}";
      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }
      int responseCode = conn.getResponseCode();
      System.out.println("POST Response Code :: " + responseCode);

      if (responseCode == HttpURLConnection.HTTP_OK) {
        // success
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        // print result
        System.out.println(response.toString());
      } else {
        System.out.println("POST request not worked");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    IniObs= System.currentTimeMillis();
   

    AndroidPacketCreator packetCreator = processor.getPacketCreator();
    Map<String, Packet> inputSidePackets = new HashMap<>();
    inputSidePackets.put(INPUT_NUM_FACES_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_FACES));
    processor.setInputSidePackets(inputSidePackets);

    tv = findViewById(R.id.tv);

    //imgv = findViewById(R.id.imageView);

    eye_open = true;
    eye_blinked = true;

    mo=true;
    mc=true;
    
    
    tv.setText("Preview");

    

    processor.addPacketCallback(
        OUTPUT_LANDMARKS_STREAM_NAME,
        (packet) -> {

          //Log.v(TAG, "Received multi face landmarks packet.");
          List<NormalizedLandmarkList> multiFaceLandmarks =
              PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());
          

          ry1 = multiFaceLandmarks.get(0).getLandmarkList().get(5).getY()*1920f;
          ry2 = multiFaceLandmarks.get(0).getLandmarkList().get(4).getY()*1920f;

          // ojo derecho
          ay1 = multiFaceLandmarks.get(0).getLandmarkList().get(374).getY()*1920f;
          ay2 = multiFaceLandmarks.get(0).getLandmarkList().get(386).getY()*1920f;

          // ojo izquierdo
          //ay1 = multiFaceLandmarks.get(0).getLandmarkList().get(145).getY()*1920f;
          //ay2 = multiFaceLandmarks.get(0).getLandmarkList().get(159).getY()*1920f;

          //boca
          la = multiFaceLandmarks.get(0).getLandmarkList().get(13).getY()*1920f;
          lb = multiFaceLandmarks.get(0).getLandmarkList().get(14).getY()*1920f;

          ratiob = (lb - la) / (ry2 - ry1);
          tv.setText("ratio: " + ratiob);
          if (ratiob < 5.0) {
            if (mc) {
                tv.setText("cerrada " + ratiob);
                startTime = System.currentTimeMillis();
                
                mc = false;
                mo = true;
            }
        } else {
            if (mo) {
                tv.setText("bostezo " + ratiob);
                mOpenDuration = System.currentTimeMillis() - startTime;
                mc = true;
                mo = false;
            }
        }
          ratio = (ay1 - ay2) / (ry2 - ry1);
          if (ratio < 0.8) {
            if (eye_blinked) {
                //tv.setText("Eye is blinked " + (currentTime - IniObs));
                startTime = System.currentTimeMillis();
                
                eye_blinked = false;
                eye_open = true;
            }
        } else {
            if (eye_open) {
                blinkDuration = System.currentTimeMillis() - startTime;
                eye_blinked = true;
                eye_open = false;
            }
        }
        currentTime = System.currentTimeMillis();
        if (currentTime - IniObs >= TIME_OBS * 60 * 1000) {
            // Cálculo de Perclos
            perclos = (blinkDuration/1000) / (TIME_OBS * 60);
            Log.d(TAG, "Perclos: " + perclos);
            //calculo de Perlov
            perlov=(mOpenDuration/1000)/(TIME_OBS*60);
            // Reiniciar contador y tiempo de observación
            IniObs = currentTime;
            post();
        }
        totalBlinkTime += blinkDuration;
        totalMopen += mOpenDuration;
    
            // if(ratio < 0.8){
            //   if(eye_blinked){
            //     tv.setText("Eye is blinked" + totalBlinkTime);
            //     startTime = System.currentTimeMillis();
            //     eye_blinked = false;
            //     eye_open = true;
            //   }
            // }
            // else{
            //   if(eye_open)
            //   {
            //     tv.setText("Eye is open" + blinkDuration);
            //     blinkDuration = System.currentTimeMillis() - startTime;
            //     eye_blinked = true;
            //     eye_open = false;
            //   }
            // }


          
          /*Log.v(
              TAG,
              "[TS:"
                  + packet.getTimestamp()
                  + "] "
                  + getMultiFaceLandmarksDebugString(multiFaceLandmarks));*/
        });

  }
  



  private static String getMultiFaceLandmarksDebugString(
      List<NormalizedLandmarkList> multiFaceLandmarks) {
    if (multiFaceLandmarks.isEmpty()) {
      return "No face landmarks";
    }
    String multiFaceLandmarksStr = "Number of faces detected: " + multiFaceLandmarks.size() + "\n";
    int faceIndex = 0;
    for (NormalizedLandmarkList landmarks : multiFaceLandmarks) {
      multiFaceLandmarksStr +=
          "\t#Face landmarks for face[" + faceIndex + "]: " + landmarks.getLandmarkCount() + "\n";
      int landmarkIndex = 0;
      for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {
        multiFaceLandmarksStr +=
            "\t\tLandmark ["
                + landmarkIndex
                + "]: ("
                + landmark.getX()
                + ", "
                + landmark.getY()
                + ", "
                + landmark.getZ()
                + ")\n";
        ++landmarkIndex;
      }
      ++faceIndex;
    }
    return multiFaceLandmarksStr;
  }
}