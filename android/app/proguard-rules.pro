# Sem ofuscar: só remove o que não é usado, para evitar quebras por reflexão.
-dontobfuscate
-keep class androidx.health.connect.** { *; }
-keep class com.yourlife.app.** { *; }
-dontwarn androidx.health.connect.**
