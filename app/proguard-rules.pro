# Giữ lại toàn bộ Shizuku API để R8 không xóa
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep interface moe.shizuku.** { *; }

# Giữ lại các lớp liên quan đến Shizuku Provider
-keep class rikka.shizuku.ShizukuProvider { *; }
