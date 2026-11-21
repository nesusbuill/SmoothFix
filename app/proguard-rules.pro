# Giữ lại toàn bộ thư viện Shizuku để R8 không xóa khi build Release
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep interface moe.shizuku.** { *; }

# Giữ lại Provider quan trọng
-keep class rikka.shizuku.ShizukuProvider { *; }
