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


mkdir -p acquis/

cd acquis/

if [ ! -e getAlignmentWithText.pl ] 
then
  wget http://wt.jrc.it/lt/Acquis/JRC-Acquis.3.0/alignments/getAlignmentWithText.pl || die "Cannot download Perl extraction script"
fi

if [ ! -d $SRCLANG ]
then
  wget http://wt.jrc.it/lt/Acquis/JRC-Acquis.3.0/corpus/jrc-$SRCLANG.tgz || die "Cannot download corpus for $SRCLANG"
  tar xzvf jrc-$SRCLANG.tgz
fi

if [ ! -d $TRGLANG ]
then 
  wget http://wt.jrc.it/lt/Acquis/JRC-Acquis.3.0/corpus/jrc-$TRGLANG.tgz || die "Cannot download corpus for $TRGLANG"
  tar xzvf jrc-$TRGLANG.tgz
fi

mkdir -p $SRCLANG-$TRGLANG/

if [ ! -e jrc-$SRCLANG-$TRGLANG.xml ]
then 
  wget http://wt.jrc.it/lt/Acquis/JRC-Acquis.3.0/alignments/jrc-$SRCLANG-$TRGLANG.xml.gz || die "Cannot download alignment"
  gunzip jrc-$SRCLANG-$TRGLANG.xml.gz
fi

if [ ! -e $SRCLANG-$TRGLANG/aligned.xml.gz ]
then
  perl getAlignmentWithText.pl jrc-$SRCLANG-$TRGLANG.xml | gzip > $SRCLANG-$TRGLANG/aligned.xml.gz
fi

mvn exec:java -f ../experiments/pom.xml -Dexec.mainClass=eu.monnetproject.bliss.experiments.IntegerizeAcquis -Dexec.args="$SRCLANG-$TRGLANG/aligned.xml.gz $SRCLANG-$TRGLANG/wordMap $SRCLANG-$TRGLANG/corpus.gz"

echo "You can delete acquis/$SRCLANG/ and acquis/$TRGLANG/ to save space"
