package com.example.detectordesomnolencia;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {
    private static final String ruta = "";
    private Map<String, String> parametros;

    public LoginRequest(String UsuarioLog, String PasswordLog, Response.Listener<String> listener) {
        super(Request.Method.POST, ruta, listener, null);
        parametros = new HashMap<>();
        parametros.put("Usuario", UsuarioLog + "");
        parametros.put("Password", PasswordLog + "");
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parametros;
    }
}
