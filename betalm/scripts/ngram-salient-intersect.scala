import scala.io._

val ifrsIn = Source.fromFile("ifrs.en.txt").getLines.map(_.trim)
var ngrams = collection.mutable.Set[String]()
var ngs : List[String] = Nil
val N = 3


for(line <- ifrsIn) {
  val ws = line.split(" ")
  for(w <- ws) {
    ngs ::= w
    for(i <- 1 to 3) {
      ngrams += ngs.take(i).mkString(" ")
    }
  }
}

val uniqIn = Source.fromFile("salience.uniq").getLines.map(_.trim)

for(line <- uniqIn) {
  val ws = line.split(" ")
  if(ngrams contains ws.drop(1).mkString(" ")) {
    println(line)
  }
}
