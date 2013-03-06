#!/bin/bash

#./compile-betalm -v -b COS_SIM -f ../wiki/en-de/ifrs.de.gz -s 10 -a 0.05 -page ../wiki/en-de/en-de.wiki.uc.gz 5 ../wiki/en-de/wordMap.uc ../wiki/en-de/lm.uc-ifrs.en 2>&1 | tee out
./compile-betalm -v -b COS_SIM -f ../wiki/de-nl/ifrs.nl.gz -s 10 -a 0.05 -page ../wiki/de-nl/de-nl.wiki.uc.gz 5 ../wiki/de-nl/wordMap.uc ../wiki/de-nl/lm.uc-ifrs.de 2>&1 | tee out.de-nl
./compile-betalm -v -b COS_SIM -f ../wiki/nl-es/ifrs.es.gz -s 10 -a 0.05 -page ../wiki/nl-es/nl-es.wiki.uc.gz 5 ../wiki/nl-es/wordMap.uc ../wiki/nl-es/lm.uc-ifrs.nl 2>&1 | tee out.nl-es
./compile-betalm -v -t INTERLEAVED_USE_SECOND -b COS_SIM -f ../wiki/en-es/ifrs.en.gz -s 10 -a 0.05 -page ../wiki/en-es/en-es.wiki.uc.gz 5 ../wiki/en-es/wordMap.uc ../wiki/en-es/lm.uc-ifrs.es 2>&1 | tee out.nl-es

