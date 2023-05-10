package com.example.detectordesomnolencia;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class RegistroRequest extends StringRequest{
    private static final String ruta ="";
    private Map<String,String> parametros;
    public RegistroRequest(String NombreReg, String UsuarioReg, String PasswordReg,
                           String TelefonoReg, int EdadReg, String NContactoReg,
                           String TContacto, Response.Listener<String> listener){
        super(Request.Method.POST, ruta, listener,null);
        parametros=new HashMap<>();
        parametros.put ("Nombre",NombreReg+"");
        parametros.put ("Usuario",UsuarioReg+"");
        parametros.put ("Password",PasswordReg+"");
        parametros.put ("Edad",TelefonoReg+"");
        parametros.put ("Telefono",EdadReg+"");
        parametros.put ("NombreEmer",NContactoReg+"");
        parametros.put ("TelefonoEmer",TContacto+"");
    }

    @Nullable
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parametros;
    }
}
