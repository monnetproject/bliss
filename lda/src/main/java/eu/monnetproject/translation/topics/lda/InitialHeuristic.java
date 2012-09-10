/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.translation.topics.lda;

/**
 *
 * @author John McCrae
 */
public class InitialHeuristic extends GibbsInference {

    private final double centroidBias;
    private final double[][] p; // [D][K]

    public InitialHeuristic(GibbsInput input, int K, double centroidBias) {
        super(input,K);
        this.centroidBias = centroidBias;
        this.p = new double[D][];
        for (int j = 0; j < D; j++) {
            p[j] = new double[K];
        }
    }

    public InitialHeuristic(int K, int D, int W, int[] DN, int[][] x, double centroidBias) {
        super(K, D, W, DN, x);
        this.centroidBias = centroidBias;
        this.p = new double[D][];
        for (int j = 0; j < D; j++) {
            p[j] = new double[K];
        }
    }

    public InitialHeuristic(int K, int D, int W, int L, int[] DN, int[][] x, int[] m, int[][] mu, double centroidBias) {
        super(K, D, W, L, DN, x, m, mu);
        this.centroidBias = centroidBias;
        this.p = new double[D][];
        for (int j = 0; j < D; j++) {
            p[j] = new double[K];
        }
    }

    public void heuristicInit() {
        int[][] hz = getHeuristicZ();
        initializeWithFixedZ(hz);
    }

    public int[][] getHeuristicZ() {
        // For each of the first K documents set the centroidBias
        for (int j = 0; j < K; j++) {
            double score = 0.0;
            for (int k = 0; k < K; k++) {
                if (k == j) {
                    score += centroidBias;
                    p[k][k] = score;
                } else {
                    score += ((1.0 - centroidBias) / (K - 1));
                    p[j][k] = score;
                }
            }
        }
        // For the remaining use overlap
        for (int j = K; j < D; j++) {
            int[] overlap = new int[K];
            int overlapSum = 0;
            for (int j2 = 0; j2 < K; j2++) {
                overlap[j2] = calcOverlap(j, j2);
                overlapSum += overlap[j2];
            }
            if (overlapSum == 0) {
                for (int k = 0; k < K; k++) {
                    p[j][k] = 1.0 / K;
                }
            } else {
                p[j][0] = (double) overlap[0] / overlapSum;
                for (int k = 1; k < K; k++) {
                    p[j][k] = p[j][k - 1] + (double) overlap[k] / overlapSum;
                }
            }
        }
        // Assign topics
        int[][] hz = new int[D][];
        for (int j = 0; j < D; j++) {
            hz[j] = new int[DN[j]];
            WORD:
            for (int i = 0; i < DN[j]; i++) {
                double u = random.nextDouble();
                for (int k = 0; k < K; k++) {
                    if (p[j][k] > u) {
                        hz[j][i] = k;
                        continue WORD;
                    }
                }
            }
        }
        return hz;
    }

    private int calcOverlap(int j, int j2) {
        int overlap = 0;
        OUTER:
        for (int i = 0; i < DN[j]; i++) {
            for (int i2 = 0; i2 < DN[j2]; i2++) {
                if (x[j][i] == x[j2][i2]) {
                    overlap++;
                    continue OUTER;
                }
            }
        }
        return overlap;
    }
}
