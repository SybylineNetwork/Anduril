jsm class public jsm/test/Thingy java/lang/Object [java/lang/Runnable] {
  "<init>": jsm method () -> {
    aload 0
    invokespecial java/lang/Object <init> ()V
    return
  },
  "run": jsm method public () -> {
    getstatic java/lang/System out Ljava/io/PrintStream;
    ldc "Ran the thing"
    invokevirtual java/io/PrintStream println (Ljava/lang/String;)V
    return
  }
}
