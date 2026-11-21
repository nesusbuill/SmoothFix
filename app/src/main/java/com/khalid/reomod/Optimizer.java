package com.khalid.reomod;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import rikka.shizuku.Shizuku;

public class Optimizer {

    public interface LogCallback {
        void log(String msg);
    }

    private final LogCallback callback;

    public Optimizer(LogCallback callback) {
        this.callback = callback;
    }

    private void log(String m) {
        if (callback != null) callback.log(m);
    }

    private String sh(String cmd) {
        try {
            Process p = Shizuku.newProcess(new String[]{"sh", "-c", cmd}, null, null);
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            p.waitFor();
            r.close();
            return sb.toString();
        } catch (Exception e) {
            return "ERR: " + e.getMessage();
        }
    }

    private void run(String cmd) { sh(cmd); }

    public void reduceHaptic() {
        log("📳 Giảm rung & âm chạm...");
        run("settings put system haptic_feedback_enabled 0");
        run("settings put system sound_effects_enabled 0");
        run("settings put system vibrate_on 0");
        log("✓ Đã tắt rung & âm chạm");
    }

    public void disableAnimations() {
        log("✨ Tắt animation hệ thống...");
        run("settings put global window_animation_scale 0");
        run("settings put global transition_animation_scale 0");
        run("settings put global animator_duration_scale 0");
        log("✓ Animation = 0, UI phản hồi tức thì");
    }

    public void disableBlur() {
        log("🌫️ Tắt blur nền (giảm tải GPU)...");
        run("settings put global disable_window_blurs 1");
        log("✓ Đã tắt blur (Android 12+)");
    }

    public void forceGpuRendering() {
        log("🎨 Force GPU rendering...");
        run("setprop debug.egl.hw 1");
        run("setprop debug.sf.hw 1");
        log("✓ GPU rendering (áp dụng triệt để sau reboot)");
    }

    public void disableHwOverlays() {
        log("🖥️ Tắt HW overlays (giảm compositing)...");
        run("service call SurfaceFlinger 1008 i32 1 2>/dev/null");
        log("✓ HW overlays tắt (best-effort)");
    }

    public void limitBackgroundProcesses() {
        log("⚙️ Giới hạn background process...");
        run("settings put global background_process_limit 2");
        log("✓ Background process limit = 2");
    }

    public void killBackground() {
        log("🧠 Dọn RAM nền...");
        String[] pkgs = {
                "com.facebook.katana", "com.facebook.orca", "com.instagram.android",
                "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",
                "com.google.android.youtube", "com.spotify.music",
                "com.snapchat.android", "com.twitter.android", "com.discord",
                "com.netflix.mediaclient", "com.linkedin.android",
                "com.shopee.vn", "com.zing.zalo"
        };
        for (String p : pkgs) run("am force-stop " + p);
        run("am kill-all");
        log("✓ Đã dọn app nền");
    }

    public void trimCaches() {
        log("🗑️ Dọn cache hệ thống...");
        run("pm trim-caches 1073741824");
        log("✓ Cache đã dọn");
    }

    public void gameMode(String pkg) {
        log("🎮 Game Mode performance: " + pkg);
        run("cmd game set --mode performance " + pkg);
        log("✓ Game Mode (nếu máy hỗ trợ)");
    }

    public void cpuPerformance() {
        log("🔥 Ép CPU performance (best-effort)...");
        String[] paths = {
                "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                "/sys/devices/system/cpu/cpu4/cpufreq/scaling_governor",
                "/sys/devices/system/cpu/cpu7/cpufreq/scaling_governor"
        };
        for (String p : paths) run("echo performance > " + p + " 2>/dev/null");
        log("✓ Ép CPU xong (có thể fail nếu không root)");
    }

    public void dropKernelCaches() {
        log("💧 Drop kernel caches (best-effort)...");
        String out = sh("echo 3 > /proc/sys/vm/drop_caches 2>&1");
        if (out.trim().isEmpty() || !out.toLowerCase().contains("denied")) {
            log("✓ Kernel caches đã drop");
        } else {
            log("✗ Kernel chặn (cần root) — bỏ qua");
        }
    }

    public void restoreAll() {
        log("↺ Khôi phục mặc định...");
        run("settings put global window_animation_scale 1");
        run("settings put global transition_animation_scale 1");
        run("settings put global animator_duration_scale 1");
        run("settings put system haptic_feedback_enabled 1");
        run("settings put system sound_effects_enabled 1");
        run("settings put system vibrate_on 1");
        run("settings put global disable_window_blurs 0");
        run("settings put global background_process_limit 0");
        run("setprop debug.egl.hw 0");
        run("setprop debug.sf.hw 0");
        run("service call SurfaceFlinger 1008 i32 0 2>/dev/null");
        log("✓ Đã khôi phục mọi thứ về mặc định");
    }

    public static String[] games() {
        return new String[]{"com.dts.freefireth", "com.dts.freefiremax"};
    }
}
