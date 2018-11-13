package com.sparktalk.hunandchoo.appointmentapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

import java.util.Map;

public class LocationSelectActivity extends AppCompatActivity {

    WebView webView;

    String lati;
    String longi;

    Button select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_select);

        webView = (WebView)findViewById(R.id.locationSelectAct_webV);

        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);

        webView.addJavascriptInterface(this, "AndroidApp");

        webView.loadUrl("file:///android_asset/javapage.html");

        select = (Button)findViewById(R.id.locationSelectAct_selectBtn);

        lati = "0";
        longi = "0";

        select.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                webView.loadUrl("javascript:returnLocation()");
                finish();
            }
        });
    }

    @JavascriptInterface
    public void receiveLocation(String value) {
        SharedPreferences sp = getSharedPreferences("locationInfo", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("location", value);
        editor.commit();
    }
}
