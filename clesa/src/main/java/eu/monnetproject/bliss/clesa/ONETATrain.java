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

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.WordMap;
import eu.monnetproject.math.sparse.DiskBackedRealVector;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.eigen.CholeskyDecomposition;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author John McCrae
 */
public class ONETATrain {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);

        final File corpus = opts.roFile("corpus[.gz|.bz2]", "The corpus");

        final File wordMap = opts.roFile("wordMap", "The word map");

        final File outFile1 = opts.woFile("outFile", "The file to write the source L-matrix to");
        final File outFile2 = opts.woFile("outFile", "The file to write the target L-matrix to");

        opts.restAsSystemProperties();

        if (!opts.verify(ONETATrain.class)) {
            return;
        }
        final int W = WordMap.calcW(wordMap);
        train(corpus, W, outFile1, outFile2);
    }

    public static void train(final File corpus, final int W, final File kernelFile1, final File kernelFile2) throws IOException {
        final ParallelBinarizedReader pbr = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));

        System.err.println("Creating CL-ESA metric");
        final SimilarityMetric metric = new CLESAFactory().makeMetric(pbr, W);

        final int J = metric.K();

        pbr.close();

        final ParallelBinarizedReader pbr2 = new ParallelBinarizedReader(CLIOpts.openInputAsMaybeZipped(corpus));

        SparseIntArray[] s;
        
        final DataOutputStream kOut1 = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(kernelFile1));
        final DataOutputStream kOut2 = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(kernelFile2));
        
        int j = 0;

        System.err.print("Calculating kernel");
        while ((s = pbr2.nextFreqPair(W)) != null) {
            final Vector<Double> v0 = metric.simVecSource(s[0]);
            for(int i = 0; i < v0.length(); i++) {
                kOut1.writeDouble(v0.doubleValue(i));
            }
            final Vector<Double> v1 = metric.simVecTarget(s[1]);
            for(int i = 0; i < v1.length(); i++) {
                kOut2.writeDouble(v1.doubleValue(i));
            }
            if (++j % 10 == 0) {
                System.err.print(".");
            }
        }
        System.err.println();
        kOut1.flush();
        kOut1.close();
        kOut2.flush();
        kOut2.close();

        System.err.print("Cholesky decomp source kernel");
        CholeskyDecomposition.denseOnDiskDecomp(kernelFile1, J);
        System.err.println();

        System.err.print("Cholesky decomp target kernel");
        CholeskyDecomposition.denseOnDiskDecomp(kernelFile2, J);
        System.err.println();
    }
}
