package com.hexp.centros;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MoodleActivity extends AppCompatActivity {

    private WebView moodleWebView;
    private ProgressBar progressBar;
    private boolean IdEAinyectado = false;
    private boolean errorManejado = false;
    private ValueCallback<Uri[]> rutaCallback;
    private static final int resultadoSelectorArchivos = 1;
    private View vistaPantallaCompleta = null;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moodle);

        // ajustes del webview, colores y views
        moodleWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);
        WebSettings webSettings = moodleWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDatabasePath(getApplicationContext().getCacheDir().getAbsolutePath());
        int fondoWebView = getResources().getColor(R.color.md_theme_background, getTheme());
        moodleWebView.setBackgroundColor(fondoWebView);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(moodleWebView, true);
        String provincia = getIntent().getStringExtra("provincia");
        String url = urlProvincias(provincia);

        // variables y sharedpreferences
        Context context = MoodleActivity.this;
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
        
        moodleWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // cuando la página cargue, quitar el progressbar
                progressBar.setVisibility(View.GONE);
                
                // inyectar credenciales si es necesario (solo si estamos en el CAS)
                if (url.contains("edea.juntadeandalucia.es")) {
                    // inyectar credenciales solo una vez. no quitar esto, si no hay muchas redirecciones al login.
                    if (!IdEAinyectado) {
                        inyectarIdEA();
                        IdEAinyectado = true;
                    }

                    // comprobar si el CAS devuelve error (credenciales incorrectas), solo una vez.
                    if (!errorManejado) {
                        erroresLogin();
                    }
                }
            }

            @SuppressLint("InlinedApi") // el minimo para este intent es API 30. este proyecto se compila, como minimo, para API 25
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    // (pendiente a refactor) deeplinking a otras aplicaciones siempre que sea posible.
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER);
                    startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    // si no hay más remedio, cargarlo en el webview
                    if (url.contains("juntadeandalucia.es")) {
                        view.loadUrl(url);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                    }
                    return true;

                }
            }
        });

        // selección de archivos, pantalla completa, entre otros tweaks y ajustes para que el webview cargue moodle centros
        moodleWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (vistaPantallaCompleta != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                vistaPantallaCompleta = view;
                FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
                decorView.addView(vistaPantallaCompleta, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                moodleWebView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (vistaPantallaCompleta == null) {
                    return;
                }
                FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
                decorView.removeView(vistaPantallaCompleta);
                vistaPantallaCompleta = null;
                moodleWebView.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                MoodleActivity.this.rutaCallback = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    // (pendiente a refactor - metodo deprecado)
                    startActivityForResult(intent, resultadoSelectorArchivos);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MoodleActivity.this, "No se ha podido abrir el selector de archivos", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });

        // finalmente abrir moodle centros
        moodleWebView.loadUrl(url);
    }
    
    // al cerrar
    @Override
    protected void onDestroy() {
        if (moodleWebView != null) {
            moodleWebView.loadUrl("about:blank");
            moodleWebView.clearHistory();
            moodleWebView.clearCache(true);
            moodleWebView.removeAllViews();
            moodleWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == resultadoSelectorArchivos) {
            if (rutaCallback == null) return;
            Uri[] result = (data == null || resultCode != RESULT_OK) ? null : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            rutaCallback.onReceiveValue(result);
            rutaCallback = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void erroresLogin() {
        // si se presenta un error en el CAS
        String jsCheckError = "document.querySelector('.alert.alert-danger') !== null";

        moodleWebView.evaluateJavascript(jsCheckError, value -> {
            if ("true".equals(value) && !errorManejado) {
                errorManejado = true;

                runOnUiThread(() -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("username");
                    editor.remove("password");
                    editor.apply();

                    Intent intent = new Intent(MoodleActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void inyectarIdEA() {
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");

        String js = "var usernameField = document.getElementById('username');" +
                "var passwordField = document.getElementById('password');" +
                "if (usernameField) usernameField.value = '" + username + "';" +
                "if (passwordField) passwordField.value = '" + password + "';" +
                "var submitButton = document.querySelector('input[name=\"submit\"]');" +
                "if (submitButton) submitButton.click();";

        moodleWebView.evaluateJavascript(js, null);
    }

    private String urlProvincias(String provincia) {
        switch (provincia) {
            case "Almería": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Falmeria%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Cádiz": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Fcadiz%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Córdoba": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Fcordoba%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Granada": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Fgranada%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Huelva": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Fhuelva%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Jaén": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Fjaen%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Málaga": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Fmalaga%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Sevilla": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fcentros%2Fsevilla%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Formación Profesional (FP)": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fformacionprofesional%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            case "Profesorado": return "https://edea.juntadeandalucia.es/cas/login?service=https%3A%2F%2Feducacionadistancia.juntadeandalucia.es%2Fprofesorado%2Flogin%2Findex.php%3FauthCAS%3DCAS";
            default: return "https://google.com";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // manejar cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.acceptCookie();
    }

    @Override
    public void onBackPressed() {
        // (pendiente a refactor) si se puede retroceder al pulsar atrás, se hará
        if (moodleWebView != null && moodleWebView.canGoBack()) {
            moodleWebView.goBack();  // retroceder una pagina en el historial
        } else {
            super.onBackPressed();  // si no, se cierra la actividad
        }
    }
}
