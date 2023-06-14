-verbose

-libraryjars <java.home>/jmods/(!**.jar;!module-info.class)

-printmapping proguard.map

# keep the plugin and its onEnable/onDisable
-keep class net.dumbdogdiner.dogcore.DogCorePlugin {
    public void onEnable();
}

# keep event handlers
-keepclassmembers class * extends org.bukkit.event.Listener {
    @org.bukkit.event.EventHandler *;
}

# keep commands
-keepclassmembers class * {
    @revxrsal.commands.annotation.Command *;
}

# keep enum constants and fields
-keep enum * {
    *;
}

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

-keep class * extends java.sql.Driver

-repackageclasses

-dontobfuscate

-allowaccessmodification

# we don't use any kotlin helpers
-dontwarn kotlin.**
# we aren't using the OSGI bundles
-dontwarn org.osgi.**
# we aren't using Jarkarta persistence
-dontwarn jakarta.persistence.**
# plugin is not built for Windows
-dontwarn com.sun.jna.**
-dontwarn waffle.windows.**

# for the command library we use
-dontwarn net.kyori.adventure.platform.bukkit.BukkitAudiences
-dontwarn lombok.Lombok

# some other stuff i don't know
-dontwarn java.lang.invoke.MethodHandle
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
