Bilingual Similarity Suite (BLISS)
==================================

This package provides a set of tools for working with topic modelling and in 
particular in the cross-lingual case, and for application to machine translation.
The following algorithms are implemented

* Latent Dirichlet Allocation
* Cross-Lingual Explicit Semantic Analysis

And the following are planned

* Kernel Explicit Semantic Analysis
* Latent Semantic Analysis
* Coupled Probabilistic Latent Semantic Analysis

Building
--------

Translation Topics uses Maven to build, and can be simply installed with the 
following command

    mvn install

Building a corpus
-----------------

To build a corpus for this there are existing scripts that download the data from 
Wikipedia. These can be run with (for English to German)

    ./build-wikipedia-article.sh en de

Mate-finding trials
-------------------

Mate-finding trials can be run with the following command, from the `experiments`
sub-folder:

    mvn exec:java -Dexec.mainClass=eu.monnetproject.bliss.experiments.MateFindingTrial 
           -Dexec.args="trainFile metricFactory W testFile"

Where `W` is the number of distinct tokens in the corpus and `metricFactory` is:

* `eu.monnetproject.bliss.clesa.CLESA`: For CL-ESA
* (More to come)

Language model adaptation
-------------------------

Language models can be trained with the following command (from the `betalm` folder)

    mvn exec:java -Dexec.mainClass="betalm.compile" -Dexec.args="corpus.gz N wordMap W lmFile"
   
Where `N` is the order of the _n_-gram model and `W` the number of distinct tokens.
To adapt to a specific document provide in addition to -Dexec.args the following flags

        -Dexec.args="-b METHOD -f file[.gz] ..." 

Where `METHOD` is one of 

* COS_SIM
* NORMAL_COS_SIM
* KLD
* JACCARD
* DICE
* ROGERS_TANIMOTO
* DF_JACCARD
* DF_DICE
* WxWCLESA


