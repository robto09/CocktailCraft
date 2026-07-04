# CocktailCraft release rules.
#
# Ktor 3, Coil 2, Koin 4, coroutines, and the AndroidX/Glance artifacts all
# ship consumer R8 rules in their AARs/JARs, so they need nothing here.
# Koin 4's constructor DSL resolves via lambdas, not reflection — no rules.

# --- Room (via WorkManager) ---
# WorkDatabase_Impl is created reflectively (Class.forName + newInstance).
# R8 full mode (AGP 9 default) does not keep constructors for classes kept
# without member rules, and the Room consumer rules WorkManager pulls in
# predate full mode — verified startup crash without this.
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# --- kotlinx-serialization (official rules) ---
# Generated serializers for @Serializable classes (the shared KMP models) are
# looked up reflectively through their Companion/INSTANCE members; R8 cannot
# see those references.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
