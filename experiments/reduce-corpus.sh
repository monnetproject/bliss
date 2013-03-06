#!/bin/bash

FOLDER=../acquis/en-es
CORPUS=corpus.gz
DOWN=1
FREQMIN=1

./downsample $FOLDER/$CORPUS $DOWN $FOLDER/sampleModel.gz
./count-corpus $FOLDER/sampleModel.gz $FOLDER/wordMap 100 $FOLDER/sampleModel.freqs
./clean-corpus $FOLDER/sampleModel.gz $FOLDER/wordMap $FOLDER/sampleModel.freqs $FREQMIN 100 $FOLDER/sampleModel.clean.gz
./filter-split $FOLDER/sampleModel.clean.gz 1 10 $FOLDER/sampleModel.train.gz $FOLDER/sampleModel.test.gz
./resample-corpus $FOLDER/sampleModel.train.gz $FOLDER/sampleModel.test.gz $FOLDER/wordMap $FOLDER/sample.wordMap $FOLDER/sample-train $FOLDER/sample-test 
./clean-corpus -unk -stopWords $FOLDER/sampleModel.gz $FOLDER/wordMap $FOLDER/sampleModel.freqs $FREQMIN 100 $FOLDER/sampleModel.uc.gz
./resample-corpus $FOLDER/sampleModel.uc.gz $FOLDER/sampleModel.uc.gz $FOLDER/wordMap $FOLDER/sample.uc.wordMap $FOLDER/sample.uc /dev/null
rm $FOLDER/sampleModel*

