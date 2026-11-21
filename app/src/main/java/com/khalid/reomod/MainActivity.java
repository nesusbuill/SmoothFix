package com.khalid.reomod;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

    private static final int REQ = 300;

    private TextView logView, statusText;
    private ScrollView logScroll;
    private MaterialButton btnStart, btnRestore, btnConnect;

    private MaterialSwitch swFile, swAnim, swRam, swGame;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Optimizer opt;
    private volatile boolean busy = false;

    private final Shizuku.OnBinderReceivedListener binderListener = () -> handler.post(this::updateStatus);
    private final Shizuku.OnBinderDeadListener binderDeadListener = () -> handler.post(() -> {
        statusText.setText("✗ Shizuku đã ngắt kết nối");
        statusText.setTextColor(0xFFFF6B6B);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        opt = new Optimizer(this::log);

        Shizuku.addRequestPermissionResultListener(this);
        Shizuku.addBinderReceivedListenerSticky(binderListener);
        Shizuku.addBinderDeadListener(binderDeadListener);

        updateStatus();
    }

    private void bindViews() {
        logView = findViewById(R.id.logView);
        logScroll = findViewById(R.id.logScroll);
        statusText = findViewById(R.id.statusText);
        btnStart = findViewById(R.id.btnStart);
        btnRestore = findViewById(R.id.btnRestore);
        btnConnect = findViewById(R.id.btnConnect);

        swFile = findViewById(R.id.swFile);
        swAnim = findViewById(R.id.swAnim);
        swRam = findViewById(R.id.swRam);
        swGame = findViewById(R.id.swGame);

        btnConnect.setOnClickListener(v -> requestPerm());
        btnStart.setOnClickListener(v -> onStartClicked());
        btnRestore.setOnClickListener(v -> onRestoreClicked());
    }

    private void updateStatus() {
        try {
            if (!Shizuku.pingBinder()) {
                statusText.setText("○ Shizuku chưa chạy");
                statusText.setTextColor(0xFFFBBF24);
                return;
            }
            if (Shizuku.isPreV11()) {
                statusText.setText("✗ Cần Shizuku v11+");
                statusText.setTextColor(0xFFFF6B6B);
                return;
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                statusText.setText("● Đã kết nối & cấp quyền");
                statusText.setTextColor(0xFF4ADE80);
            } else {
                statusText.setText("○ Chưa cấp quyền Shizuku");
                statusText.setTextColor(0xFFFBBF24);
            }
        } catch (Throwable t) {
            statusText.setText("✗ Lỗi Shizuku");
            statusText.setTextColor(0xFFFF6B6B);
        }
    }

    private void requestPerm() {
        try {
            if (!Shizuku.pingBinder()) {
                log("Mở app Shizuku trước đã.");
                return;
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                log("Shizuku đã sẵn sàng");
                return;
            }
            Shizuku.requestPermission(REQ);
        } catch (Throwable t) {
            log("Lỗi: " + t.getMessage());
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == REQ) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                log("✓ Đã cấp quyền Shizuku");
            } else {
                log("✗ Từ chối cấp quyền");
            }
            updateStatus();
        }
    }

    private boolean hasPerm() {
        try {
            return Shizuku.pingBinder()
                    && !Shizuku.isPreV11()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnStart.setEnabled(enabled);
        btnRestore.setEnabled(enabled);
    }

    private void onStartClicked() {
        if (busy) return;
        if (!hasPerm()) {
            log("Cần cấp quyền Shizuku trước");
            return;
        }
        busy = true;
        setButtonsEnabled(false);
        log("--- BẮT ĐẦU TỐI ƯU ---");

        new Thread(() -> {
            if (swFile.isChecked()) {
                opt.patchGameFile("com.dts.freefireth", "Free Fire Thường");
                opt.patchGameFile("com.dts.freefiremax", "Free Fire Max");
            }
            if (swAnim.isChecked()) opt.disableAnimations();
            if (swRam.isChecked()) opt.killBackground();
            if (swGame.isChecked()) opt.gameMode();

            handler.post(() -> {
                log("═══ XONG XUÔI - CHIẾN ═══");
                busy = false;
                setButtonsEnabled(true);
            });
        }).start();
    }

    private void onRestoreClicked() {
        if (busy) return;
        if (!hasPerm()) {
            log("Cần cấp quyền Shizuku trước");
            return;
        }
        busy = true;
        setButtonsEnabled(false);
        
        new Thread(() -> {
            opt.restoreGameFile("com.dts.freefireth", "Free Fire Thường");
            opt.restoreGameFile("com.dts.freefiremax", "Free Fire Max");
            opt.restoreSystem();
            
            handler.post(() -> {
                busy = false;
                setButtonsEnabled(true);
                log("--- ĐÃ KHÔI PHỤC TOÀN BỘ ---");
            });
        }).start();
    }

    private void log(String m) {
        handler.post(() -> {
            logView.append("\n" + m);
            logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(this);
        Shizuku.removeBinderReceivedListener(binderListener);
        Shizuku.removeBinderDeadListener(binderDeadListener);
    }
}
