-ignorewarnings

# Retain public and private methods, fields, and classes
-keepnames class * {
    public private *;
}

-keep class com.mobility.enp.data.model.** {*;}

-keepattributes Exceptions, Signature, InnerClasses

# Specifically keep Room annotations
-keepattributes RuntimeVisibleAnnotations


