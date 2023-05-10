package com.example.detectordesomnolencia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.google.mediapipe.components.FrameProcessor;






import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Detector extends AppCompatActivity {

    private static final String TAG = "Detector";
    private static final String INPUT_NUM_FACES_SIDE_PACKET_NAME = "num_faces";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "multi_face_landmarks";
    // Max number of faces to detect/process.
    private static final int NUM_FACES = 1;
    private float ry1, ry2, ay1, ay2,la ,lb ,ratio ,ratiob ,totalBlinkTime, totalMopen;
    private TextView tv;
    private long blinkDuration, mOpenDuration, startTime;
    private long IniObs;
    private long currentTime;
    private float perclos,perlov;
    private static final int TIME_OBS =15;
    private boolean eye_blinked, eye_open;
    private boolean mo, mc;
    public FrameProcessor processor;

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
        setContentView(R.layout.activity_detector);
        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        Map<String, Packet> inputSidePackets = new HashMap<>();
        inputSidePackets.put(INPUT_NUM_FACES_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_FACES));
        processor.setInputSidePackets(inputSidePackets);

        tv = findViewById(R.id.tv);

        eye_open = true;
        eye_blinked = true;
        mo=true;
        mc=true;
        tv.setText("Preview");

        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    List<LandmarkProto.NormalizedLandmarkList> multiFaceLandmarks =
                            PacketGetter.getProtoVector(packet, LandmarkProto.NormalizedLandmarkList.parser());

                    ry1 = multiFaceLandmarks.get(0).getLandmarkList().get(5).getY()*1920F;
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
                        perclos = ((blinkDuration/1000) / (TIME_OBS * 60)*100);
                        Log.d(TAG, "Perclos: " + perclos);
                        //calculo de Perlov
                        perlov=(mOpenDuration/1000)/(TIME_OBS*60);
                        // Reiniciar contador y tiempo de observación
                        IniObs = currentTime;
                        post();
                    }
                    totalBlinkTime += blinkDuration;
                    totalMopen += mOpenDuration;

                });
    }

    private static String getMultiFaceLandmarksDebugString(
            List<LandmarkProto.NormalizedLandmarkList> multiFaceLandmarks) {
        if (multiFaceLandmarks.isEmpty()) {
            return "No face landmarks";
        }
        String multiFaceLandmarksStr = "Number of faces detected: " + multiFaceLandmarks.size() + "\n";
        int faceIndex = 0;
        for (LandmarkProto.NormalizedLandmarkList landmarks : multiFaceLandmarks) {
            multiFaceLandmarksStr +=
                    "\t#Face landmarks for face[" + faceIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
            for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
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