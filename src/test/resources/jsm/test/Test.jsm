jsm class public jsm.test.Test
{
  Thingy:      jsm import jsm.test.Thingy
  Vec:         jsm import jsm.math.Vector
  Sys:         jsm import java.lang.System
  Printer:     jsm import java.io.PrintStream
  Str:         jsm import java.lang.String
  Int:         jsm import java.lang.Integer
  
  println:     jsm call invokevirtual Printer println (Str)
  printlnO:    jsm call invokevirtual Printer println (java.lang.Object)
  scaleV:      jsm call invokestatic Vec scale (double,double,double,double)->(double,double,double)
  
  constructor: jsm method public () -> void
  {
    <- this
    $supernew ()
    getstatic Sys out Printer dup dup
    ldc "The meaning of life is:"
	:: println
    <- this
    invokevirtual this thing ()int
	invokestatic Str valueOf (int)Str
	:: println
	$lambda (int in) -> (int) {
	  <- in
	  bipush 32
	  iadd -> in
	  $return in
	}
	:: printlnO
    return
  }
  thing: jsm method public () -> int
  {
    bipush 42
    return
  }
  pull: jsm method public () -> Thingy
  {
    <- this
    getfield this cache Thingy
      dup
      ifnull create
      return
      $label create
      pop
    $new Thingy ()
      dup
      <- this
      swap
      putfield this cache Thingy
    return
  }
  cache: jsm field public Thingy
  decode: jsm method public (Str str) -> int
  {
    <- str
    invokestatic Int decode (Str)Int
    invokevirtual Int longValue ()long
    l2i
    bipush 34
    imul
    return
  }
  scale: jsm method public (double xx, double yy, double zz, double factor) -> (double,double,double)
  {
	<- (xx, yy, zz, factor)
	:: scaleV -> (xx, yy, zz)
    $return (xx, yy, zz)
  }
}
