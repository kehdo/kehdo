# Add project-specific ProGuard rules here.
# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }

# Retrofit
-keepattributes Signature, Exceptions, *Annotation*, EnclosingMethod, InnerClasses
-keep class retrofit2.** { *; }

# Hilt
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.lifecycle.AndroidViewModel
