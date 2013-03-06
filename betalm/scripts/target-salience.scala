import scala.io._
import scala.math._

val lc = Source.fromFile("translatedDocs").getLines.size
val sum = Source.fromFile("translatedDocs").getLines.map(line => (line split " ").head.toDouble).sum
val in = Source.fromFile("translatedDocs").getLines.map(_.trim())
val N = 5
val mean = sum / lc
System.err.println(mean)

for(line <- in) {
  val ws = line split " "
  val score = ws.head.toDouble / mean
  var ngram : List[String] = Nil
  var ngrams = scala.collection.mutable.Map[List[String],Int]()
  for(w <- ws.drop(1)) {
    if(w matches ".*[\\p{P}\\p{S}].*") {
      ngram = Nil
    } else {
      ngram ::= w
      for(n <- 1 to min(N,ngram.length)) {
        val ng = ngram take n
        if(ngrams contains ng) {
          ngrams(ng) += 1
        } else {
          ngrams(ng) = 1
        }
      }
    }
  }
  for((ng,freq) <- ngrams) {
    println(ng.reverse.mkString(" ") + " " + score + " " + freq + " " + ws.length)
  }
}
