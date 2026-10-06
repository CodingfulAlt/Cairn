# nav routes are @Serializable, keep their serializers
-keepclassmembers @kotlinx.serialization.Serializable class io.github.codingfulalt.cairn.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class io.github.codingfulalt.cairn.**$$serializer { *; }

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# widget actions are created from their class name
-keep class * implements androidx.glance.appwidget.action.ActionCallback { <init>(); }
