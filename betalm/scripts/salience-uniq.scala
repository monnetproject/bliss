import scala.io._
import scala.math._
import java.text.DecimalFormat

val in = Source.fromFile("salience.sorted").getLines().map(_.trim())

var last = ""
var counts : List[(Double,Int,Int)] = Nil
val df = new DecimalFormat("0.00000000000")
var salienceUnigram = collection.mutable.Map[String,Double]()

for(line <- in) {
  val ws = line split " "
  if(ws.size == 4) {
    val ng = ws(0)
    if(last != ng) {
      if(counts.size > 1 && last != "") {
        //if(last == "shares") { System.err.println(counts) }
        val count = (counts map { case (_,ct,_) =>  ct.toDouble }).sum
        val score = (counts map { case (sal,ct,n) => sal * ct / (count - 0.5 * ct) }).sum
        salienceUnigram(last) = score
      }
      last = ng
      counts = Nil
    }
    counts ::= ((ws(1).toDouble, ws(2).toInt, ws(3).toInt))
  }
}

val out = new java.io.PrintWriter("salience.unigram")
for((unigram,score) <- salienceUnigram) {
  out.println(df.format(score) + " " + unigram)
}
out.flush
out.close

val in2 = Source.fromFile("salience.sorted").getLines().map(_.trim())

var last2 = List[String]()

for(line <- in2) {
  val ws = line split " "
  val ng = ws dropRight 3 toList
  val ss = ws takeRight 3
  if(last2 != ng) {
    if(ws.size > 4) {
    val score = ng.size.toDouble / (ng map { x =>
       1.0 / salienceUnigram.getOrElse(x,1e-15) }).sum
    println(df.format(score) + " " + ng.mkString(" "))
    }
    last2 = ng
  }
}

//for(line <- in) {
//  val ws = line split " "
//  val ng = ws dropRight(2) mkString " "
//  val ss = ws takeRight(2)
//  if(last != ng) {
//    if(docCounts.count(_ != 0) > 1 && last != "") {
//      val count = docCounts.sum
//      val docSals = salCounts map { x =>
//	x / (count-x)
//      }
//      val score = ((salCounts zip docSals) map { case (x,y) =>
//        x*y
//      }).sum;
//      println(df.format(score) + " " + last)
//    } 
//    last = ng
//    docCounts = Nil
//    salCounts = Nil
//  } 
//  salCounts ::= ss(0).toDouble
//  docCounts ::= ss(1).toInt
//}

