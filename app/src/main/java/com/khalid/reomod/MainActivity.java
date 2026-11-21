package com.khalid.reomod;

import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener {

    private static final int REQ = 300;

    private TextView logView, breatheText, statusText;
    private ScrollView logScroll;
    private MaterialCardView breatheCard;
    private MaterialButton btnStart, btnRestore, btnConnect;

    private MaterialSwitch swAnim, swHaptic, swRam, swGame, swCpu,
            swBlur, swGpu, swCache, swBgLimit, swDropCache;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Optimizer opt;
    private volatile boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        opt = new Optimizer(this::log);
        Shizuku.addRequestPermissionResultListener(this);
        updateStatus();
    }

    private void bindViews() {
        logView = findViewById(R.id.logView);
        logScroll = findViewById(R.id.logScroll);
        breatheText = findViewById(R.id.breatheText);
        breatheCard = findViewById(R.id.breatheCard);
        statusText = findViewById(R.id.statusText);
        btnStart = findViewById(R.id.btnStart);
        btnRestore = findViewById(R.id.btnRestore);
        btnConnect = findViewById(R.id.btnConnect);

        swAnim = findViewById(R.id.swAnim);
        swHaptic = findViewById(R.id.swHaptic);
        swRam = findViewById(R.id.swRam);
        swGame = findViewById(R.id.swGame);
        swCpu = findViewById(R.id.swCpu);
        swBlur = findViewById(R.id.swBlur);
        swGpu = findViewById(R.id.swGpu);
        swBgLimit = findViewById(R.id.swBgLimit);
        swDropCache = findViewById(R.id.swDropCache);
        swCache = findViewById(R.id.swCache);

        btnConnect.setOnClickListener(v -> requestPerm());
        btnStart.setOnClickListener(v -> onStartClicked());
        btnRestore.setOnClickListener(v -> onRestoreClicked());
    }

    private void updateStatus() {
        try {
            if (Shizuku.isPreV11()) {
                statusText.setText("✗ Shizuku quá cũ — cần v11+");
                statusText.setTextColor(0xFFFF6B6B);
                return;
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                statusText.setText("● Shizuku đã kết nối");
                statusText.setTextColor(0xFF4ADE80);
            } else {
                statusText.setText("○ Chưa cấp quyền Shizuku");
                statusText.setTextColor(0xFFFBBF24);
            }
        } catch (Throwable t) {
            statusText.setText("✗ Shizuku chưa cài");
            statusText.setTextColor(0xFFFF6B6B);
        }
    }

    private void requestPerm() {
        try {
            if (Shizuku.isPreV11()) { log("Cần Shizuku v11+"); return; }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                log("Shizuku đã sẵn sàng"); updateStatus(); return;
            }
            Shizuku.requestPermission(REQ);
        } catch (Throwable t) {
            log("Lỗi Shizuku: " + t.getMessage());
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == REQ) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) log("✓ Đã cấp quyền Shizuku");
            else log("✗ Từ chối cấp quyền");
            updateStatus();
        }
    }

    private boolean hasPerm() {
        try {
            return !Shizuku.isPreV11()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable t) {
            return false;
        }
    }

    private void onStartClicked() {
        if (busy) return;
        if (!hasPerm()) { log("Cần cấp quyền Shizuku trước"); return; }
        busy = true;
        btnStart.setEnabled(false);
        btnRestore.setEnabled(false);

        runBreathingRitual(() -> new Thread(() -> {
            applySelected();
            handler.post(() -> {
                log("═══ HOÀN TẤT — VÀO TRẬN TỰ TIN ═══");
                busy = false;
                btnStart.setEnabled(true);
                btnRestore.setEnabled(true);
            });
        }).start());
    }

    private void onRestoreClicked() {
        if (busy) return;
        if (!hasPerm()) { log("Cần cấp quyền Shizuku trước"); return; }
        busy = true;
        btnStart.setEnabled(false);
        btnRestore.setEnabled(false);
        new Thread(() -> {
            opt.restoreAll();
            handler.post(() -> {
                busy = false;
                btnStart.setEnabled(true);
                btnRestore.setEnabled(true);
            });
        }).start();
    }

    private void applySelected() {
        if (swHaptic.isChecked()) opt.reduceHaptic();
        if (swAnim.isChecked()) opt.disableAnimations();
        if (swBlur.isChecked()) opt.disableBlur();
        if (swGpu.isChecked()) {
            opt.forceGpuRendering();
            opt.disableHwOverlays();
        }
        if (swBgLimit.isChecked()) opt.limitBackgroundProcesses();
        if (swRam.isChecked()) opt.killBackground();
        if (swCache.isChecked()) opt.trimCaches();
        if (swDropCache.isChecked()) opt.dropKernelCaches();
        if (swGame.isChecked()) {
            for (String g : Optimizer.games()) opt.gameMode(g);
        }
        if (swCpu.isChecked()) opt.cpuPerformance();
    }

    private void runBreathingRitual(Runnable onDone) {
        breatheCard.setVisibility(View.VISIBLE);
        breatheCard.setScaleX(1.0f);
        breatheCard.setScaleY(1.0f);
        runPhase("Hít vào...", 4, 1.0f, 1.3f, () ->
                runPhase("Giữ...", 4, 1.3f, 1.3f, () ->
                        runPhase("Thở ra...", 4, 1.3f, 1.0f, () -> {
                            breatheCard.setScaleX(1.0f);
                            breatheCard.setScaleY(1.0f);
                            breatheCard.setVisibility(View.GONE);
                            onDone.run();
                        })
                )
        );
    }

    private void runPhase(String label, int seconds, float startScale, float endScale, Runnable onEnd) {
        if (startScale != endScale) {
            ValueAnimator anim = ValueAnimator.ofFloat(startScale, endScale);
            anim.setDuration(seconds * 1000L);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addUpdateListener(a -> {
                float v = (float) a.getAnimatedValue();
                breatheCard.setScaleX(v);
                breatheCard.setScaleY(v);
            });
            anim.start();
        }

        final int[] left = {seconds};
        breatheText.setText(label + " " + left[0]);

        Runnable tick = new Runnable() {
            @Override
            public void run() {
                left[0]--;
                if (left[0] > 0) {
                    breatheText.setText(label + " " + left[0]);
                    handler.postDelayed(this, 1000);
                } else {
                    onEnd.run();
                }
            }
        };
        handler.postDelayed(tick, 1000);
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
    }
}
