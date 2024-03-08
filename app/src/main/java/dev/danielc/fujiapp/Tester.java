// Basic test suite
// Copyright 2023 Daniel C - https://github.com/petabyt/fujiapp
package dev.danielc.fujiapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.util.Log;
import android.content.Intent;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.os.Looper;
import android.os.Bundle;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;

import android.content.ClipboardManager;
import android.widget.Toast;

public class Tester extends AppCompatActivity {
    private Handler handler;

    Bluetooth bt;

    private void connectBluetooth() {
        bt = new Bluetooth();
        Intent intent;
        try {
            intent = bt.getIntent();
        } catch (Exception e) {
            fail("无法使用蓝牙: " + e.toString());
            return;
        }

        if (intent != null) {
            try {
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                fail("无法使用蓝牙: 权限被拒绝（或蓝牙已关闭）" + e);
                return;
            }

            log("已获得蓝牙访问权限");

            // TODO: Finish bluetooth tests
        } else {
            return;
        }

        bt.getConnectedDevice();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.regressiontesting));

        handler = new Handler(Looper.getMainLooper());

        Backend.cTesterInit(this);

        if (Backend.cRouteLogs() == 0) {
            log("将日志路由到内存缓冲区");
        }

        ConnectivityManager m = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        Context ctx = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Backend.fujiConnectToCmd();
                } catch (Exception e) {
                    fail("WIFI: " + e.toString());

                    try {
                        Backend.connectUSB(ctx);
                    } catch (Exception e2) {
                        fail("USB: " + e2.toString());

                        verboseLog = Backend.cEndLogs();
                        return;
                    }
                }

                log("已建立连接，开始测试");

                mainTest(m);

                verboseLog = Backend.cEndLogs();
                log("点击复制按钮与开发人员共享详细日志");
            }
        }).start();
    }

    private String verboseLog = null;
    private String currentLogs = "";
    public void log(String str) {
        Log.d("fudge-tester", str);
        handler.post(new Runnable() {
            @Override
            public void run() {
                currentLogs += str + "<br>";
                TextView testerLog = findViewById(R.id.testerLog);
                testerLog.setText(Html.fromHtml(currentLogs));
            }
        });
    }

    public void fail(String str) {
        log("<font color='#EE0000'>[FAIL] " + str + "</font>");
    }

    public void mainTest(ConnectivityManager m) {
        int rc = Backend.cFujiTestSuite(Backend.chosenIP);
        log("Return code: " + rc);
        if (rc != 0) return;

        log("Test completed.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getTitle() == "copy") {
            if (verboseLog != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Fudge log", verboseLog);
                clipboard.setPrimaryClip(clip);
            } else {
                Toast.makeText(this, "测试尚未完成", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "copy");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setIcon(R.drawable.baseline_content_copy_24);
        return true;
    }
}
