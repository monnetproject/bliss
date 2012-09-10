/**
 * ********************************************************************************
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
package eu.monnetproject.translation.topics.sim;

import eu.monnetproject.translation.topics.SparseArray;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Notation:
 *   W: Size of vocabulary across all languages
 *   J: Number of documents in train set
 *   X: The JxW matrix, such that x[w,j] = #{word w is in document j in the source language}
 *   Y: The JxW matrix, such that y[w,j] = #{word w is in document j in the target language}
 *   D(X): A WxW diagonal matrix, whose values are the inverse document frequencies of X
 * 
 * Standard CLESA can thus be defined as
 *   clesa(x,y) = cosSim( X D(X) x, Y D(Y) y )
 * 
 * The vector of a word w is given by X D(X) i_w where i_ww = 1, i_ww' = 0 if w != w'
 * The mapping of all such vectors is given by X D(X) I = X D(X)
 * 
 * Hence the word-mapped translation vector similarity is 
 *   wxwclesa(x,y) = cosSim( [ (Y D(Y))^T , X D(X) x ] , y )
 * 
 * where [ A, x ] yields a vector of the cos-sim of the columns of A with x
 * i.e., [A,x] = A norm^-1(A) x / norm(x) where norm is the (by-column) 2-norm
 * 
 * @author John McCrae
 */
public class WxWCLESA implements SimilarityMetric {
    // Dimensions
    private final int W, J;
    // The target WxJ matrix
    private final SparseArray[] y; // W
    // The source JxW matrix. The second index is language, but we don't bother
    // with this so it should always be zero after we have calculated df and Y
    private final SparseArray[][] Xt; // J x 2
    // The norms of Y D(Y)
    private final double[] norm;
    // The document frequency in source
    public final int[] df; // W
    // The doucument frequency in target
    public final int[] df_f; // W

    public WxWCLESA(File file) throws IOException {
        // Load data
        final ParallelReader data = ParallelReader.fromFile(file);
        this.W = data.W;
        this.J = data.x.length;
        // We collect X and Y^T
        this.Xt = data.x;
        this.y = transpose(data.x, 1);
        // Calculate document frequencies
        this.df = new int[W];
        this.df_f = new int[W];
        initDF();
        // Calculate norm of Y D(Y)
        this.norm = new double[W];
        
        for (int w = 0; w < W; w++) {
            // Not we do not create empty arrays when we transpose, we must check null
            if(y[w] == null)
                continue;
            
            for (Map.Entry<Integer, Integer> e : y[w].entrySet()) {
                norm[w] += e.getValue() * e.getValue() / df_f[w] / df_f[w];
            }
            norm[w] = Math.sqrt(norm[w]);
        }
        System.err.println("WxW CLESA inited");
    }

    /**
     * We initialize the document frequency df_j = #{ documents containing j } / D
     */
    private void initDF() {
        for (int j = 0; j < Xt.length; j++) {
            for (int w : Xt[j][0].keySet()) {
                df[w]++;
            }
            for (int w : Xt[j][1].keySet()) {
                df_f[w]++;
            }
        }
    }
    /**
     * Transpose the matrix to a WxJ matrix
     */
    private SparseArray[] transpose(SparseArray[][] x, int l) {
        final SparseArray[] Xt = new SparseArray[W];
        for (int j = 0; j < x.length; j++) {
            for (Map.Entry<Integer, Integer> e : x[j][l].entrySet()) {
                if (Xt[e.getKey()] == null) {
                    Xt[e.getKey()] = new SparseArray(x.length);
                }
                Xt[e.getKey()].put(j, e.getValue());
            }
        }
        return Xt;
    }
    
    /**
     * We calculate the mapping X D(X) x
     * (sparse vector not used here as we assume the input document overlaps with most
     * document in at least one high-frequency word)
     * @param termVec x
     * @return X D(X) x
     */
    public double[] sourceVector(SparseArray termVec) {
        double[] sim = new double[Xt.length];
        for (int j = 0; j < Xt.length; j++) {
            for (int w : Xt[j][0].keySet()) {
                sim[j] += (double) (termVec.get(w) * Xt[j][0].get(w)) / df[w];
            }
        }
        return sim;
    }

    
    @Override
    public double[] simVecSource(SparseArray termVec) {
        // Map the term vector to CLESA space
        final double[] inMapped = sourceVector(termVec);
        // Find its norm in this space
        double inNorm = 0.0;
        for (int j = 0; j < J; j++) {
            inNorm += inMapped[j] * inMapped[j];
        }
        inNorm = Math.sqrt(inNorm);
        
        final double[] sim = new double[W];

        // Calculate predicated y in place-by-place manner
        for (int w = 0; w < W; w++) {
            // Skip empty (null) rows
            if(y[w] == null)
                continue;
            // sim[w] = D(Y)^T D(Y) inMapped  = D(Y)^T D(Y) X D(X) x
            for (Map.Entry<Integer, Integer> e : y[w].entrySet()) {
                sim[w] += inMapped[e.getKey()] * e.getValue() / df_f[w];
            }
            sim[w] = sim[w] / inNorm;
        }
        return sim;
    }

    @Override
    public double[] simVecTarget(SparseArray termVec) {
        return termVec.toDoubleArray();
    }

    @Override
    public int W() {
        return W;
    }
}
