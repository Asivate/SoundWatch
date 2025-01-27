# Keep TensorFlow Lite classes and native methods
-keep class org.tensorflow.lite.** { *; }
-keep class org.tensorflow.lite.gpu.** { *; }
-keep class org.tensorflow.lite.support.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Socket.IO classes
-keep class io.socket.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Python/Chaquopy classes
-keep class com.chaquo.python.** { *; }

# Keep Android Wear classes
-keep class androidx.wear.** { *; }
-keep class com.google.android.wearable.** { *; }

# Keep JSON classes
-keep class org.json.** { *; }

# Keep model-specific classes
-keep class * implements org.tensorflow.lite.Interpreter$DataType { *; }
-keep class * extends org.tensorflow.lite.support.metadata.schema.ModelMetadata { *; }
-keep class * extends org.tensorflow.lite.support.metadata.schema.TensorMetadata { *; }

# Keep TensorFlow Lite delegates and operations
-keep class org.tensorflow.lite.delegates.** { *; }
-keep class org.tensorflow.lite.gpu.gl.** { *; }
-keep class org.tensorflow.lite.support.metadata.** { *; }
-keep class org.tensorflow.lite.support.tensorbuffer.** { *; }
-keep class org.tensorflow.lite.support.common.ops.** { *; }
-keep class org.tensorflow.lite.support.image.ops.** { *; }

# Keep annotations
-keep @interface org.tensorflow.lite.annotations.UsedByReflection
-keep @org.tensorflow.lite.annotations.UsedByReflection class * { *; }

# Keep MediaPlayer classes
-keep class android.media.** { *; }
-keep class androidx.media.** { *; }

# Keep Socket.IO Engine classes
-keep class io.socket.engineio.** { *; }
-keep class io.socket.engineio.client.** { *; }

# Keep Python native libraries
-keep class com.chaquo.python.** { *; }
-keepclasseswithmembers class * {
    @com.chaquo.python.annotation.* <methods>;
}

# Keep all model input/output classes
-keep class * implements org.tensorflow.lite.Interpreter$Delegate { *; }
-keep class * implements org.tensorflow.lite.Interpreter$Options { *; }

# Keep all serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep all enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
} 