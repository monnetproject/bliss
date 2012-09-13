#!/bin/bash

die () {
    echo >&2 "$@"
    exit 1
}

SRCLANG=$1
TRGLANG=$2
if [ "$#" -eq 3 ]
then
  SAMPLING=$3
else
  SAMPLING=1
fi

if [ ! -f wiki/$SRCLANG-$TRGLANG/ ];
then
  mkdir -p wiki/$SRCLANG-$TRGLANG/
fi

if [ ! -f "WikiExtract.py" ];
then
  echo "Obtaining UniPisa WikiExtract Script"
  wget -q http://medialab.di.unipi.it/Project/SemaWiki/Tools/WikiExtractor.py || die "Please download WikiExtract.py script"
  chmod a+x WikiExtract.py
fi

if [ ! -f "experiments/target" ];
then
   mvn install
fi

if [ ! -f wiki/${SRCLANG}wiki-latest-pages-articles.xml.bz2 ];
then
  echo "Step 1. Download Wikipedia $SRCLANG"
  wget http://dumps.wikimedia.org/${SRCLANG}wiki/latest/${SRCLANG}wiki-latest-pages-articles.xml.bz2 -O wiki/${SRCLANG}wiki-latest-pages-articles.xml.bz2 || die "Could not download WikiPedia dump"
fi

if [ -f wiki/${SRCLANG}wiki.xml.gz ]
then
  echo "Step 2. Extract Wikipedia $SRCLANG"
  bzcat wiki/${SRCLANG}wiki-latest-pages-articles.xml.bz2 -cb 10M -o wiki/${SRCLANG}wiki
  find wiki/${SRCLANG}wiki -name '*.bz2' -exec bunzip2 {} \; | gzip wiki/${SRCLANG}wiki.xml.gz
  rm -fr wiki/${SRCLANG}wiki/
fi

if [ ! -f wiki/${TRGLANG}wiki-latest-pages-articles.xml.bz2 ];
then
  echo "Step 3. Download Wikipedia $TRGLANG"
  wget http://dumps.wikimedia.org/${TRGLANG}wiki/latest/${TRGLANG}wiki-latest-pages-articles.xml.bz2 -O wiki/${TRGLANG}wiki-latest-pages-articles.xml.bz2 || die "Could not download WikiPedia dump"
fi

if [ -f wiki/${TRGLANG}wiki.xml.gz ]
then
  echo "Step 4. Extract Wikipedia $TRGLANG"
  bzcat wiki/${TRGLANG}wiki-latest-pages-articles.xml.bz2 -cb 10M -o wiki/${TRGLANG}wiki
  find wiki/${TRGLANG}wiki -name '*.bz2' -exec bunzip2 {} \; | gzip wiki/${TRGLANG}wiki.xml.gz
  rm -fr wiki/${TRGLANG}wiki/
fi

cd wiki/$SRCLANG-$TRGLANG/

if [ -f ili ]
then
   echo "Step 5. Build Interlingual Index"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.InterlingualIndex" -Dexec.args="../${SRCLANG}wiki-latest-pages-articles.xml.bz2 $TRGLANG ili"
fi

if [ -f ${SRCLANG}wiki.$SAMPLING.int.gz ]
then 
  echo "Step 6. Integerize $SRCLANG Wikipedia"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.IntegerizeCorpus" -Dexec.args="-s $SAMPLING ../${SRCLANG}wiki.xml.gz wordMap$SAMPLING ${SRCLANG}wiki.$SAMPLING.int.gz"
fi

if [ -f ${TRGLANG}wiki.$SAMPLING.int.gz ]
then 
  echo "Step 7. Integerize $TRGLANG Wikipedia"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.IntegerizeCorpus" -Dexec.args="-s $SAMPLING ../${TRGLANG}wiki.xml.gz wordMap$SAMPLING ${TRGLANG}wiki.$SAMPLING.int.gz"
fi


if [ -f ${SRCLANG}wiki.$SAMPLING.filt.gz ]
then
   echo "Step  8. Filter/translate $SRCLANG by ILI"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.FilterByILI" -Dexec.args="${SRCLANG}wiki.$SAMPLING.int.gz ili ${SRCLANG}wiki.$SAMPLING.filt.gz src-trans"
fi

if [ -f ${TRGLANG}wiki.$SAMPLING.filt.gz ]
then
   echo "Step  9. Filter $TRGLANG by ILI"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.FilterByILI" -Dexec.args="${TRGLANG}wiki.$SAMPLING.int.gz ili ${TRGLANG}wiki.$SAMPLING.filt.gz trg"
fi

if [ -f ${SRCLANG}wiki.$SAMPLING.sort.gz ]
then 
   echo "Step 10. Sort ${SCRLANG} data"
   LC_ALL=C sort ${SRCLANG}wiki.$SAMPLING.filt.gz > ${SRCLANG}wiki.$SAMPLING.sort.gz
fi


if [ -f ${TRGLANG}wiki.$SAMPLING.sort.gz ]
then 
   echo "Step 11. Sort ${SCRLANG} data"
   LC_ALL=C sort ${TRGLANG}wiki.$SAMPLING.filt.gz > ${SRCLANG}wiki.$SAMPLING.sort.gz
fi

if [ -f ${SRCLANG}wiki.$SAMPLING.gz ]
then
   echo "Step 12. Interleave and binarize data"
   mvn -f ../../experiments/pom.xml exec:java -Dexec.mainClass="eu.monnetproject.translation.topics.experiments.InterleaveFiles" -Dexec.args="${TRGLANG}wiki.$SAMPLING.sort.gz ${TRGLANG}wiki.$SAMPLING.sort.gz ${SRCLANG}wiki.$SAMPLING.gz"
fi