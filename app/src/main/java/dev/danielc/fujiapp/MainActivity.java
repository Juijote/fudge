// Copyright 2023 Daniel C - https://github.com/petabyt/fujiapp
package dev.danielc.fujiapp;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        Backend.init();

        handler = new Handler(Looper.getMainLooper());

        findViewById(R.id.reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Backend.jni_print_clear();
                connectClick(v);
            }
        });

        Backend.jni_print("Download location: " + Backend.getDownloads() + "\n");

        // Test activity
        // Intent intent = new Intent(MainActivity.this, test.class);
        // startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Backend.logLocation = "main";
    }

    public void connectClick(View v) {
        // Socket must be opened on WiFi - otherwise it will prefer cellular
        Backend.jni_print("Attempting connection...\n");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                ConnectivityManager.setProcessDefaultNetwork(network);
                if (!Conn.connect(Backend.FUJI_IP, Backend.FUJI_CMD_PORT, Backend.TIMEOUT)) {
                    Backend.logLocation = "gallery";
                    Intent intent = new Intent(MainActivity.this, gallery.class);
                    startActivity(intent);
                }
                connectivityManager.unregisterNetworkCallback(this);
            }
        };

        connectivityManager.requestNetwork(requestBuilder.build(), networkCallback);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void setErrorText(String arg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView error_msg = findViewById(R.id.error_msg);
                error_msg.setText(arg);
            }
        });
    }
}
