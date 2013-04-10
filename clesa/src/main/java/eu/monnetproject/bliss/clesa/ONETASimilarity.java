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
import eu.monnetproject.bliss.ParallelReader;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.math.sparse.SparseMatrix;
import eu.monnetproject.math.sparse.SparseRealArray;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.Vectors;
import eu.monnetproject.math.sparse.eigen.CholeskyDecomposition;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jmccrae
 */
public class ONETASimilarity implements SimilarityMetric {

    public final SparseIntArray[][] x;
    public final int J;
    public final int W;
    public final CLESASimilarity[] similarity;
    public final File[] L;

    public ONETASimilarity(File data, File LMatrix1, File LMatrix2) throws IOException {
        final ParallelReader pr = ParallelReader.fromFile(data);
        this.J = pr.x.length;
        this.W = pr.W();
        this.x = pr.x;
        this.similarity = new CLESASimilarity[]{
            CLESA.getSim(x, 0, W),
            CLESA.getSim(x, 1, W)
        };
        this.L = new File[] { LMatrix1,LMatrix2 };
    }

    public ONETASimilarity(SparseIntArray[][] x, File LMatrix1, File LMatrix2, int W) {
        this.J = x.length;
        this.W = W;
        this.x = x;
        this.similarity = new CLESASimilarity[]{
            CLESA.getSim(x, 0, W),
            CLESA.getSim(x, 1, W)
        };
        this.L = new File[] { LMatrix1,LMatrix2 };
    }
    
    @Override
    public Vector<Double> simVecSource(Vector<Integer> termVec) {
        
        double[] sim = new double[J];
        for (int j = 0; j < J; j++) {
            sim[j] = similarity[0].score(termVec, x[j][0]);
        }
        final SparseRealArray vSim = SparseRealArray.fromArray(sim);
        final Vector<Double> vSim2 = CholeskyDecomposition.solveOnDisk(L[0], J, vSim);
        return CholeskyDecomposition.solveTOnDisk(L[0], J, vSim2);
    }

    @Override
    public Vector<Double> simVecTarget(Vector<Integer> termVec) {
        
        double[] sim = new double[J];
        for (int j = 0; j < J; j++) {
            sim[j] = similarity[1].score(termVec, x[j][1]);
        }
        final SparseRealArray vSim = SparseRealArray.fromArray(sim);
        final Vector<Double> vSim2 = CholeskyDecomposition.solveOnDisk(L[1], J, vSim);
        return CholeskyDecomposition.solveTOnDisk(L[1], J, vSim2);
    }

    @Override
    public int K() {
        return J;
    }

    @Override
    public int W() {
        return W;
    }
}
