package com.example.detectordesomnolencia;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button buttonReg = (Button) findViewById(R.id.buttonReg);
        Button buttonLog = (Button) findViewById(R.id.button_log);
         EditText usuarioL = (EditText)findViewById(R.id.UsuarioLog);
         EditText passwordL = (EditText)findViewById(R.id.PasswordLog);
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buttonReg = new Intent(Login.this, Registro.class);
                Login.this.startActivity(buttonReg);
                //Login.this.finish();
            }
        });

        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buttonLog = new Intent(Login.this, Detector.class);
                Login.this.startActivity(buttonLog);
/*                String usuario = usuarioL.getText().toString();
                String password = passwordL.getText().toString();
                Response.Listener<String> respuesta = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonRespuesta = new JSONObject(response);
                            boolean ok = jsonRespuesta.getBoolean("success");
                            if (ok == true){
                                String usuario = jsonRespuesta.getString("usuario");
                                String password =jsonRespuesta.getString("password");
                                Intent detector = new Intent(Login.this,Detector.class);
                                detector.putExtra("usuario",usuario);
                                Login.this.startActivity(detector);
                                Login.this.finish();
                            }else{
                                AlertDialog.Builder alerta = new AlertDialog.Builder(Login.this);
                                alerta.setMessage("fallo login")
                                        .setNegativeButton("reintente",null)
                                        .create()
                                        .show();
                            }
                        }catch (JSONException e){
                            e.getMessage();
                        }
                    }
                };
                LoginRequest l = new LoginRequest(usuario,password,respuesta);
                RequestQueue cola = Volley.newRequestQueue(Login.this);
                cola.add(l);*/
            }
        });
    }
}