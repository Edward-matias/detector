package com.example.detectordesomnolencia;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Registro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        EditText NombreReg = (EditText) findViewById(R.id.UsuarioLog);
        EditText UsuarioReg = (EditText) findViewById(R.id.UsuarioReg);
        EditText PasswordReg = (EditText) findViewById(R.id.PasswordReg);
        EditText TelefonoReg = (EditText) findViewById(R.id.TelefonoReg);
        EditText TContactoReg = (EditText) findViewById(R.id.TContactoReg);
        EditText NContactoReg = (EditText) findViewById(R.id.NContactoReg);
        EditText EdadReg = (EditText) findViewById(R.id.EdadReg);
        Button BRegistrar = (Button) findViewById(R.id.BRegistrar);
        BRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = NombreReg.getText().toString();
                String usuario = UsuarioReg.getText().toString();
                String password = PasswordReg.getText().toString();
                String telefono = TelefonoReg.getText().toString();
                int edad = Integer.parseInt(EdadReg.getText().toString());
                String nombre_emer = NContactoReg.getText().toString();
                String telefono_emer = TContactoReg.getText().toString();

                Response.Listener<String> respuesta = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            boolean ok = jsonRes.getBoolean("success");
                            if (ok == true) {
                                Intent i = new Intent(Registro.this, Login.class);
                                Registro.this.startActivity(i);
                                Registro.this.finish();
                            } else {
                                AlertDialog.Builder alerta = new AlertDialog.Builder(Registro.this);
                                alerta.setMessage("Fallo el registro")
                                        .setNegativeButton("Reintentar", null)
                                        .create()
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.getMessage();
                        }
                    }

                };
                RegistroRequest r = new RegistroRequest(nombre, usuario, password, telefono, edad,
                        nombre_emer, telefono_emer, respuesta);
                RequestQueue cola = Volley.newRequestQueue(Registro.this);
                cola.add(r);

            }

        });

    }
}