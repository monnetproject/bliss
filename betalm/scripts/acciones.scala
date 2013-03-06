import scala.io._

val in = Source.fromFile("rankedDocs").getLines zip Source.fromFile("translatedDocs").getLines

println("COSSIM,ACCIONES,ACTIONS,SHARES")

for((lf,lt) <- in) {
  println(lf.substring(0,lf.indexOf(" ")) + "," +
     (if(lf.contains("acciones")) { "1" } else { "0" }) + "," +
     (if(lt.contains("actions")) { "1" } else { "0" }) + "," +
     (if(lt.contains("shares")) { "1" } else { "0" }))
}
