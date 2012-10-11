#!/bin/bash

die () {
    echo >&2 "$@"
    exit 1
}

if [ $# -ne 2 ] 
then
  die "Required two argument SRCLANG and TRGLANG"
fi

SRCLANG=$1
TRGLANG=$2

if [ ! -f wiki/$SRCLANG-$TRGLANG/ ];
then
  mkdir -p wiki/$SRCLANG-$TRGLANG/
fi

if [ ! -f "WikiExtractor.py" ];
then
  echo "Obtaining UniPisa WikiExtract Script"
  wget -q http://medialab.di.unipi.it/Project/SemaWiki/Tools/WikiExtractor.py || die "Please download WikiExtract.py script"
  chmod a+x WikiExtractor.py
fi

if [ ! -d experiments/target/ ];
then
   echo "Compiling Monnet Topics"
   mvn install || die "Maven2 build failed... is Maven2 installed?"
fi

if [ ! -f wiki/${SRCLANG}wiki-latest-pages-articles.xml.bz2 ]
then
  echo "Step 1. Download Wikipedia $SRCLANG"
  wget http://dumps.wikimedia.org/${SRCLANG}wiki/latest/${SRCLANG}wiki-latest-pages-articles.xml.bz2 -O wiki/${SRCLANG}wiki-latest-pages-articles.xml.bz2 || die "Could not download WikiPedia dump"
fi

if [ ! -f wiki/${SRCLANG}wiki.xml.gz ] && [ -f wiki/${SRCLANG}wiki-latest-pages-articles.xml.bz2 ]
then
  echo "Step 2. Extract Wikipedia $SRCLANG"
  bzcat wiki/${SRCLANG}wiki-latest-pages-articles.xml.bz2 | python WikiExtractor.py -cb 10M -o wiki/${SRCLANG}wiki
  find wiki/${SRCLANG}wiki -name '*.bz2' -exec bunzip2 -c {} \; | gzip > wiki/${SRCLANG}wiki.xml.gz
  rm -fr wiki/${SRCLANG}wiki/
fi

if [ ! -f wiki/${TRGLANG}wiki-latest-pages-articles.xml.bz2 ]
then
  echo "Step 3. Download Wikipedia $TRGLANG"
  wget http://dumps.wikimedia.org/${TRGLANG}wiki/latest/${TRGLANG}wiki-latest-pages-articles.xml.bz2 -O wiki/${TRGLANG}wiki-latest-pages-articles.xml.bz2 || die "Could not download WikiPedia dump"
fi

if [ ! -f wiki/${TRGLANG}wiki.xml.gz ] && [ -f wiki/${TRGLANG}wiki-latest-pages-articles.xml.bz2 ]
then
  echo "Step 4. Extract Wikipedia $TRGLANG"
  bzcat wiki/${TRGLANG}wiki-latest-pages-articles.xml.bz2 | python WikiExtractor.py -cb 10M -o wiki/${TRGLANG}wiki
  find wiki/${TRGLANG}wiki -name '*.bz2' -exec bunzip2 -c {} \; | gzip > wiki/${TRGLANG}wiki.xml.gz
  rm -fr wiki/${TRGLANG}wiki/
fi

cd wiki/$SRCLANG-$TRGLANG/

if [ ! -f ili ] && [ -f ../${SRCLANG}wiki-latest-pages-articles.xml.bz2 ]
then
   echo "Step 5. Build Interlingual Index"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.InterlingualIndex" -Dexec.args="../${SRCLANG}wiki-latest-pages-articles.xml.bz2 $TRGLANG ili"
fi

if [ ! -f ${SRCLANG}wiki.int.gz ] && [ -f ../${SRCLANG}wiki.xml.gz ]
then 
  echo "Step 6. Integerize $SRCLANG Wikipedia"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.IntegerizeCorpus" -Dexec.args="../${SRCLANG}wiki.xml.gz wordMap ${SRCLANG}wiki.int.gz"
fi

if [ ! -f ${TRGLANG}wiki.int.gz ] && [ -f ../${TRGLANG}wiki.xml.gz ]
then 
  echo "Step 7. Integerize $TRGLANG Wikipedia"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.IntegerizeCorpus" -Dexec.args="../${TRGLANG}wiki.xml.gz wordMap ${TRGLANG}wiki.int.gz"
fi


if [ ! -f ${SRCLANG}wiki.filt.gz ] && [ -f ${SRCLANG}wiki.int.gz ] && [ -f ili ]
then
   echo "Step  8. Filter/translate $SRCLANG by ILI"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.FilterByILI" -Dexec.args="${SRCLANG}wiki.int.gz ili ${SRCLANG}wiki.filt.gz src-trans"
fi

if [ ! -f ${TRGLANG}wiki.filt.gz ]
then
   echo "Step  9. Filter $TRGLANG by ILI"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.FilterByILI" -Dexec.args="${TRGLANG}wiki.int.gz ili ${TRGLANG}wiki.filt.gz trg"
fi

if [ ! -f ${SRCLANG}wiki.sort.gz ] && [ -f ${SRCLANG}wiki.filt.gz ]
then 
   echo "Step 10. Sort ${SCRLANG} data"
   zcat ${SRCLANG}wiki.filt.gz | LC_ALL=C sort | gzip > ${SRCLANG}wiki.sort.gz
fi


if [ ! -f ${TRGLANG}wiki.sort.gz ] && [ -f ${TRGLANG}wiki.filt.gz ]
then 
   echo "Step 11. Sort ${SCRLANG} data"
   zcat ${TRGLANG}wiki.filt.gz | LC_ALL=C sort | gzip > ${TRGLANG}wiki.sort.gz
fi

if [ ! -f ${SRCLANG}-${TRGLANG}.wiki.gz ] && [ -f ${SRCLANG}wiki.sort.gz ]&& [ -f ${TRGLANG}wiki.sort.gz ]
then
   echo "Step 12. Interleave and binarize data"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.InterleaveFiles" -Dexec.args="${SRCLANG}wiki.sort.gz ${TRGLANG}wiki.sort.gz ${SRCLANG}-${TRGLANG}.wiki.gz"
fi

if [ -f wordMap ]
then
   echo "Step 13. Counting W"
   W=`java -cp ../../experiments/target/classes/ eu.monnetproject.translation.topics.experiments.WordMapSize wordMap`
   echo "W=$W"
fi

if [ ! -f freqs ] && [ -f ${SRCLANG}-${TRGLANG}.wiki.gz ]
then
  echo "Step 14. Counting frequencies"
  mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.CountFrequencies" -Dexec.args="${SRCLANG}-${TRGLANG}.wiki.gz $W 100 freqs" 
fi

if [ ! -f ${SRCLANG}-${TRGLANG}.wiki.clean.gz ] && [ -f freqs ]
then
  echo "Step 15. Filtering corpus"
  mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.CleanCorpus" -Dexec.args="${SRCLANG}-${TRGLANG}.wiki.gz $W freqs 5 100 ${SRCLANG}-${TRGLANG}.wiki.clean.gz" 
fi