import java.io._
import java.util.Scanner

val lmFile = System.getProperty("lm")

val in = new Scanner(new File(lmFile))

var n = 1

var counts : List[Int] = Nil

var count = 0;

while(in.hasNextLine()) {
   val line = in.nextLine();
   if(line == ("\\"+n+"-grams:")) {
	counts ::= count
	count = 0;
	n = n + 1
   } else if(line == "\\end\\") {
	counts ::= count
   } else if(!(line matches "\\s*")) {
	count = count + 1
   }
}

var countArr = new Array[Int](counts.length -1)

for((count,n) <- counts.reverse.zipWithIndex.tail) {
  println(n+"="+count)
  countArr(n-1) = count
}

in.close

val outFile = new File(lmFile+"2")

val out = new PrintWriter(outFile)

val in2 = new Scanner(new File(lmFile))

val ngramLine = "ngram (\\d+)=.*".r

while(in2.hasNextLine()) {
  val line = in2.nextLine()
  line match {
    case ngramLine(n) => out.println("ngram "+n+"="+countArr(n.toInt -1))
    case _ => out.println(line)
  }
}

in2.close
out.flush
out.close

//import java.nio.file.Files._

//move(outFile.toPath(),new File(lmFile).toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING)
