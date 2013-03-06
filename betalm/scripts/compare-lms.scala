import scala.io._

val in = Source.fromFile(args(0)).getLines zip Source.fromFile(args(1)).getLines

val valueLine = "(-\\d\\.\\d+)\t(.*)".r

for((lm1,lm2) <- in) {
  (lm1,lm2) match {
    case (valueLine(v1,n1),valueLine(v2,_)) => {
      val v1d = v1.toDouble
      val v2d = v2.toDouble
      if(v1d > v2d) {
        println("1st. " + n1)
      } else if(v1d < v2d) {
        println("2nd. " + n1)
      } else {
        println("==== " + n1)
     }
   }
   case _ =>
  }
}
