/**
 * *******************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *******************************************************************************
 */
package eu.monnetproject.bliss.clesa;

import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetricFactory;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jmccrae
 */
public class ONETASimilarityFactory implements SimilarityMetricFactory<ParallelBinarizedReader>{
    
    @Override
    public SimilarityMetric makeMetric(ParallelBinarizedReader reader, int W) throws IOException {
        final File lMatrix1 = new File(System.getProperty("onetaL1"));
        if(!lMatrix1.exists()) {
            throw new IllegalArgumentException("Please specify L matrix");
        }
        final File lMatrix2 = new File(System.getProperty("onetaL2"));
        if(!lMatrix2.exists()) {
            throw new IllegalArgumentException("Please specify L matrix");
        }
        return new ONETASimilarity(reader.readAll(W), lMatrix1, lMatrix2, W);
    }

    @Override
    public NGramSimilarityMetric makeNGramMetric(ParallelBinarizedReader dat, int W, int n) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    

    @Override
    public Class<ParallelBinarizedReader> datatype() {
        return ParallelBinarizedReader.class;
    }
}
