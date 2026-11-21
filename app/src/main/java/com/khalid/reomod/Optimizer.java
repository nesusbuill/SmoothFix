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
            return "";
        }
    }

    private void run(String cmd) { sh(cmd); }

    // --- CHỨC NĂNG CAN THIỆP FILE GAME ---
    public void patchGameFile(String pkg, String gameName) {
        log("🛠️ Can thiệp file: " + gameName);
        run("am force-stop " + pkg);
        try { Thread.sleep(1000); } catch (Exception ignored) {}

        String dir = "/data/data/" + pkg;
        String dataIni = dir + "/files/data.ini";
        String prefXml = dir + "/shared_prefs/" + pkg + "_preferences.xml";

        // 1. Backup file
        run("mkdir -p /sdcard/ReoModBackup/");
        run("cp " + dataIni + " /sdcard/ReoModBackup/" + pkg + "_data.ini.bak");
        run("cp " + prefXml + " /sdcard/ReoModBackup/" + pkg + "_pref.xml.bak");
        log("  ✓ Đã backup file gốc ra /sdcard/ReoModBackup/");

        // 2. Can thiệp data.ini (Hạ Res, tắt bóng, tắt HDR)
        run("sed -i 's/^ResolutionQuality=.*/ResolutionQuality=1/' " + dataIni);
        run("sed -i 's/^GraphicsQuality=.*/GraphicsQuality=1/' " + dataIni);
        run("sed -i 's/^bShadow=.*/bShadow=0/' " + dataIni);
        run("sed -i 's/^bHDR=.*/bHDR=0/' " + dataIni);
        
        // 3. Can thiệp file cài đặt (Tắt shadow, ép FPS)
        run("sed -i '/ff_shadow/ s/value=\"[^\"]*\"/value=\"false\"/' " + prefXml);
        run("sed -i '/ff_quality/ s/value=\"[^\"]*\"/value=\"0\"/' " + prefXml);
        run("sed -i '/ff_max_fps/ s/value=\"[^\"]*\"/value=\"1\"/' " + prefXml);

        // 4. Phân quyền lại
        run("chmod 660 " + dataIni);
        run("chmod 660 " + prefXml);
        run("chown system:system " + dataIni);
        run("chown system:system " + prefXml);

        // 5. Xóa cache
        run("rm -rf " + dir + "/cache/*");

        log("  ✓ Đã hạ đồ họa & tắt bóng " + gameName);
    }

    public void restoreGameFile(String pkg, String gameName) {
        log("↺ Phục hồi file gốc: " + gameName);
        run("am force-stop " + pkg);
        
        String dir = "/data/data/" + pkg;
        String dataIni = dir + "/files/data.ini";
        String prefXml = dir + "/shared_prefs/" + pkg + "_preferences.xml";

        run("cp /sdcard/ReoModBackup/" + pkg + "_data.ini.bak " + dataIni);
        run("cp /sdcard/ReoModBackup/" + pkg + "_pref.xml.bak " + prefXml);
        
        run("chmod 660 " + dataIni);
        run("chmod 660 " + prefXml);
        run("chown system:system " + dataIni);
        run("chown system:system " + prefXml);

        log("  ✓ Đã phục hồi file gốc " + gameName);
    }

    // --- CÁC CHỨC NĂNG HỆ THỐNG ---
    public void reduceHaptic() {
        run("settings put system haptic_feedback_enabled 0");
        run("settings put system sound_effects_enabled 0");
        run("settings put system vibrate_on 0");
        log("✓ Đã tắt rung & âm chạm");
    }

    public void disableAnimations() {
        run("settings put global window_animation_scale 0");
        run("settings put global transition_animation_scale 0");
        run("settings put global animator_duration_scale 0");
        run("settings put global disable_window_blurs 1");
        log("✓ Đã tắt animation & blur");
    }

    public void killBackground() {
        String[] pkgs = {
                "com.zing.zalo", "com.facebook.katana", "com.facebook.orca",
                "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",
                "com.google.android.youtube", "com.spotify.music",
                "com.shopee.vn", "com.instagram.android", "com.twitter.android"
        };
        for (String p : pkgs) run("am force-stop " + p);
        run("am kill-all");
        run("pm trim-caches 1073741824");
        log("✓ Đã dọn RAM nền & cache");
    }

    public void gameMode() {
        run("cmd game set --mode performance com.dts.freefireth 2>/dev/null");
        run("cmd game set --mode performance com.dts.freefiremax 2>/dev/null");
        log("✓ Đã ép Game Mode FF");
    }

    public void restoreSystem() {
        run("settings put global window_animation_scale 1");
        run("settings put global transition_animation_scale 1");
        run("settings put global animator_duration_scale 1");
        run("settings put system haptic_feedback_enabled 1");
        run("settings put system sound_effects_enabled 1");
        run("settings put system vibrate_on 1");
        run("settings put global disable_window_blurs 0");
        log("✓ Đã khôi phục hệ thống");
    }
}
