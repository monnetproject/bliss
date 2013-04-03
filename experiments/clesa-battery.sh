#!/bin/bash

echo "Wikipedia en-es"
echo "Normalized TF"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test
echo "Normalized DF"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=SORG

echo "Wikipedia en-de"
echo "Normalized TF"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test
echo "Normalized DF"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=SORG

echo "Acquis en-es"
echo "Normalized TF"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test
echo "Normalized DF"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=SORG

echo "Acquis de-en"
echo "Normalized TF"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test
echo "Normalized DF"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=SORG

echo "Wikipedia es-en"
echo "Normalized TF"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test
echo "Normalized DF"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding -inv ../corpora/wiki-en-es-train CLESA ../corpora/wiki-en-es.wordMap ../corpora/wiki-en-es-test clesaMethod=SORG

echo "Wikipedia de-en"
echo "Normalized TF"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test
echo "Normalized DF"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding -inv ../corpora/wiki-en-de-train CLESA ../corpora/wiki-en-de.wordMap ../corpora/wiki-en-de-test clesaMethod=SORG

echo "Acquis es-en"
echo "Normalized TF"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test
echo "Normalized DF"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding -inv ../corpora/acquis-en-es-train CLESA ../corpora/acquis-en-es.wordMap ../corpora/acquis-en-es-test clesaMethod=SORG

echo "Acquis en-de"
echo "Normalized TF"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test
echo "Normalized DF"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test tfNorm=false
echo "Normalized log(TF)"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=LOG_NORMALIZED
echo "Normalized log(DF)"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=LOG_NORMALIZED tfNorm=false
echo "TFIDF"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=TFIDF
echo "LUCENE"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=LUCENE
echo "OKAPI_BM25"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=OKAPI_BM25
echo "SORG"
./mate-finding -inv ../corpora/acquis-de-en-train CLESA ../corpora/acquis-de-en.wordMap ../corpora/acquis-de-en-test clesaMethod=SORG
