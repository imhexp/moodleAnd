package com.hexp.centros;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextIdEA, editTextContraseña;
    private Button btnIniciarSesion;
    private Button btnAtras;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // views
        editTextIdEA = findViewById(R.id.username);
        editTextContraseña = findViewById(R.id.password);
        btnIniciarSesion = findViewById(R.id.btn_iniciarsesion);
        btnAtras = findViewById(R.id.btn_atras);

        editTextIdEA.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        // variables y sharedpreferences
        Context context = LoginActivity.this;
        MasterKey llaveMoodleAnd;
        try {
            llaveMoodleAnd = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        try {
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "moodleAnd_sharedpreferences",
                    llaveMoodleAnd,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        // mirar si el IdEA y contraseña ya están en las sharedpreferences
        String usuario = sharedPreferences.getString("username", null);
        String contraseña = sharedPreferences.getString("password", null);

        if (usuario != null && contraseña != null) {
            // si hay usuario y contraseña, ir a moodle
            String province = sharedPreferences.getString("provincia", "default");
            abrirMoodle(province);
            return;
        }

        // iicio de sesion
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // valores introducidos
                String IdEA = editTextIdEA.getText().toString().trim();
                String contraseña = editTextContraseña.getText().toString().trim();

                // mirar si los campos estan completos
                if (IdEA.isEmpty() || contraseña.isEmpty()) {
                    Snackbar.make(v, "Introduce tu IdEA y contraseña para continuar.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // guardar en sharedpreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", IdEA);
                editor.putString("password", contraseña);
                editor.apply();

                // abrir moodle pasando la provincia
                String provincia = sharedPreferences.getString("provincia", "default");
                abrirMoodle(provincia);
            }
        });


        // boton patras
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                volverAtras();
            }
        });
    }

    private void abrirMoodle(String provincia) {
        Intent intent = new Intent(LoginActivity.this, MoodleActivity.class);
        intent.putExtra("provincia", provincia);
        startActivity(intent);
        finish();
    }

    private void volverAtras() {
        Intent intent = new Intent(LoginActivity.this, ProvinciaActivity.class);
        startActivity(intent);
        finish();
    }
}
