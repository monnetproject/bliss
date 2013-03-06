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

if [ ! -d wiki/$SRCLANG-$TRGLANG/ ];
then
  die "Please run build-wikipedia-corpus first"
fi

cd wiki/$SRCLANG-$TRGLANG

mvn exec:java -f ../../experiments/pom.xml -Dexec.mainClass="eu.monnetproject.bliss.experiments.CalcThresh" -Dexec.args="-out stats 300000 freqs"

mvn exec:java -f ../../experiments/pom.xml -Dexec.mainClass="eu.monnetproject.bliss.experiments.CleanCorpus" -Dexec.args="-unk -stopWords $SRCLANG-$TRGLANG.wiki.clean.gz `cat stats.W` freqs `cat stats.thresh` 100 $SRCLANG-$TRGLANG.wiki.uc2.gz"

mvn exec:java -f ../../experiments/pom.xml -Dexec.mainClass="eu.monnetproject.bliss.experiments.ResampleInts" -Dexec.args="$SRCLANG-$TRGLANG.wiki.uc2.gz $SRCLANG-$TRGLANG.wiki.uc2.gz wordMap wordMap.uc $SRCLANG-$TRGLANG.wiki.uc.gz /dev/null"

rm $SRCLANG-$TRGLANG.wiki.uc2.gz

