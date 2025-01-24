package com.hexp.centros;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKey;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class ProvinciaActivity extends AppCompatActivity {

    private RadioGroup radioGroupProvincias;
    private Button btnSiguiente;
    private Button btnCreditos;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // views
        setContentView(R.layout.activity_provincia);
        radioGroupProvincias = findViewById(R.id.radioGroup_provincias);
        btnSiguiente = findViewById(R.id.btn_siguiente);
        btnCreditos = findViewById(R.id.btn_creditos);

        // variables y sharedpreferences
        Context context = ProvinciaActivity.this;
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

        boolean primeraVez = sharedPreferences.getBoolean("primeravez", true);

        if (!primeraVez) {
            // tirar directamente al inicio de sesion si ya se ha configurado la app antes
            String provincia = sharedPreferences.getString("provincia", "default");
            abrirLogin(provincia);
            return;
        }

        // boton inicio sesion
        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // provincia seleccionada
                int idSeleccionado = radioGroupProvincias.getCheckedRadioButtonId();
                RadioButton radialPulsado = findViewById(idSeleccionado);

                if (radialPulsado != null) {
                    String provinciaSeleccionada = radialPulsado.getText().toString();

                    // guardar provincia seleccionada en sharedpreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("primeravez", false);
                    editor.putString("provincia", provinciaSeleccionada);
                    editor.apply();

                    // abrir login
                    abrirLogin(provinciaSeleccionada);
                } else {
                    Snackbar.make(v, "Seleccione la provincia donde estudia para continuar.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        btnCreditos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCreditos();
            }
        });
    }

    private void abrirLogin(String provincia) {
        Intent intent = new Intent(ProvinciaActivity.this, LoginActivity.class);
        intent.putExtra("provincia", provincia);
        // abrir la actividad del login "LoginActivity"
        startActivity(intent);
        finish();
    }

    private void abrirCreditos() {
        Intent intent = new Intent(ProvinciaActivity.this, CreditosActivity.class);
        startActivity(intent);
        finish();
    }
}
