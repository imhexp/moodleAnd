package com.hexp.centros;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class CreditosActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creditos);

        // views
        Button btnVolver = findViewById(R.id.btn_volver);
        Button btnSrc = findViewById(R.id.btn_source);

        Context context = CreditosActivity.this;
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

        // volver
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(CreditosActivity.this, ProvinciaActivity.class);
                startActivity(intent);
            }
        });


        // boton source
        btnSrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String repo = "https://github.com/imhexp/moodleAnd";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(repo));
                startActivity(intent);
            }
        });
    }
}
