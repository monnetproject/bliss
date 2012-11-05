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
package eu.monnetproject.translation.topics.lda;

import eu.monnetproject.math.sparse.SparseIntArray;
import java.util.Random;

/**
 *
 * @author John McCrae
 */
public class Estimator {

    private final Random random = new Random();

//    public double[] topics(int[] d, GibbsData data, int iterations) {
//        log.warning("This implementation is inefficient and needs to be fixed");
//        begin = System.currentTimeMillis();
//        int[] z_local = new int[d.length];
//        for (int i = 0; i < d.length; i++) {
//            z_local[i] = random.nextInt(data.K);
//        }
//        for (int c = 0; c < iterations; c++) {
//            iterInfo(c, iterations);
//            for (int i = 0; i < d.length; i++) {
//                double bestP = 0.0;
//                for (int k = 0; k < data.K; k++) {
//                    double p = (data.N_k[k] + a(z_local, k) + data.alpha)
//                            * (data.N_kw[k].get(d[i]) + b(z_local, d, d[i], k) + data.beta)
//                            / (data.N_k[k] + a(z_local, k) + data.W * data.beta);
//                    if (p > bestP) {
//                        z_local[i] = k;
//                        bestP = p;
//                    }
//
//                }
//            }
//        }
//        int N = 0;
//        for (int k = 0; k < data.K; k++) {
//            N += data.N_k[k];
//        }
//        double[] theta = new double[data.K];
//        double theta_sum = 0.0;
//        for (int k = 0; k < data.K; k++) {
//            theta[k] = (a(z_local, k) + data.alpha) / (N + data.K * data.alpha);
//            theta_sum += theta[k];
//        }
//
//        for (int k = 0; k < data.K; k++) {
//            theta[k] = theta[k] / theta_sum;
//        }
//        return theta;
//    }

    public double[] topics(int[] d, int l, PolylingualGibbsData data, int iterations) {
        return topics(new int[][] { d } , new int[] { l }, data, iterations) ;
    }
//        begin = System.currentTimeMillis();
//        final int[] z_local = new int[d.length];
//        final int[] N_k = new int[data.K];
//        final SparseIntArray[] N_wk = new SparseIntArray[data.K];
//        for (int k = 0; k < data.K; k++) {
//            N_wk[k] = new SparseIntArray();
//        }
//        for (int i = 0; i < d.length; i++) {
//            z_local[i] = random.nextInt(data.K);
//            N_k[z_local[i]]++;
//            N_wk[z_local[i]].inc(d[i]);
//        }
//        for (int c = 0; c < iterations; c++) {
//            iterInfo(c, iterations);
//            for (int i = 0; i < d.length; i++) {
//                double bestP = 0.0;
//                for (int k = 0; k < data.K; k++) {
////                    double p = (data.N_lk[l][k] + a(z_local, k) + data.alpha)
////                            * (data.N_lkw[l][k].get(d[i]) + b(z_local, d, d[i], k) + data.beta)
////                            / (data.N_lk[l][k] + a(z_local, k) + data.W * data.beta);
//                    double p = (data.N_lk[l][k] + N_k[k] + data.alpha)
//                            * (data.N_lkw[l][k].get(d[i]) + N_wk[k].get(d[i]) + data.beta)
//                            / (data.N_lk[l][k] + N_k[k] + data.W * data.beta);
//                    if (p > bestP && z_local[i] != k) {
//                        N_k[z_local[i]]--;
//                        N_wk[z_local[i]].dec(d[i]);
//                        z_local[i] = k;
//                        N_wk[k].inc(d[i]);
//                        N_k[k]++;
//                        bestP = p;
//                    }
//
//                }
//            }
//        }
//        int N = 0;
//        for (int k = 0; k < data.K; k++) {
//            N += data.N_lk[l][k];
//        }
//        double[] theta = new double[data.K];
//        double theta_sum = 0.0;
//        for (int k = 0; k < data.K; k++) {
//            theta[k] = (a(z_local, k) + data.alpha) / (N + data.K * data.alpha);
//            theta_sum += theta[k];
//        }
//
//        for (int k = 0; k < data.K; k++) {
//            theta[k] = theta[k] / theta_sum;
//        }
//        return theta;
//    }

    public double[] topics(int[][] d, int[] l, PolylingualGibbsData data, int iterations) {
        assert(d.length == l.length);
        begin = System.currentTimeMillis();
        final int[][] z_local = new int[d.length][];
        final int[] N_k = new int[data.K];
        final int[][] N_jk = new int[l.length][data.K];
        final SparseIntArray[][] N_jwk = new SparseIntArray[l.length][data.K];
        for(int j = 0; j < l.length; j++) {
            for (int k = 0; k < data.K; k++) {
                N_jwk[j][k] = new SparseIntArray(data.W);
            }
        }
        for (int j = 0; j < d.length; j++) {
            z_local[j] = new int[d[j].length];
            for (int i = 0; i < d[j].length; i++) {
                z_local[j][i] = random.nextInt(data.K);
                N_k[z_local[j][i]]++;
                N_jwk[j][z_local[j][i]].inc(d[j][i]);
                N_jk[j][z_local[j][i]]++;
            }
        }
        
        for (int c = 0; c < iterations; c++) {
//            iterInfo(c, iterations);
            for (int j = 0; j < d.length; j++) {
                for (int i = 0; i < d[j].length; i++) {
                    double bestP = 0.0;
                    for (int k = 0; k < data.K; k++) {
//                    double p = (data.N_lk[l][k] + a(z_local, k) + data.alpha)
//                            * (data.N_lkw[l][k].get(d[i]) + b(z_local, d, d[i], k) + data.beta)
//                            / (data.N_lk[l][k] + a(z_local, k) + data.W * data.beta);
//                        double p = (data.N_lk[l[j]][k] + N_k[k] + data.alpha)
//                                * (data.N_lkw[l[j]][k].get(d[j][i]) + N_wk[k].get(d[j][i]) + data.beta)
//                                / (data.N_lk[l[j]][k] + N_k[k] + data.W * data.beta);
                        double p = ((double)N_jk[j][k] + data.alpha) *
                                ((double)data.N_lkw[l[j]][k].get(d[j][i]) + (double)N_jwk[j][k].get(d[j][i]) + data.beta) /
                                ((double)data.N_lk[l[j]][k] + (double)N_jk[j][k] + data.W * data.beta);
                        if (p > bestP && z_local[j][i] != k) {
                            N_k[z_local[j][i]]--;
                            N_jwk[j][z_local[j][i]].dec(d[j][i]);
                            N_jk[j][z_local[j][i]]--;
                            z_local[j][i] = k;
                            N_jk[j][k]++;
                            N_jwk[j][k].inc(d[j][i]);
                            N_k[k]++;
                            bestP = p;
                        }

                    }
                }
            }
        }
//        int N = 0;
//        for (int j = 0; j < l.length; j++) {
//            for (int k = 0; k < data.K; k++) {
//                N += data.N_lk[l[j]][k];
//            }
//        }
        double[] theta = new double[data.K];
        double theta_sum = 0.0;
        for (int k = 0; k < data.K; k++) {
            theta[k] = ((double)a(z_local, k) + data.alpha);// / (N + data.K * data.alpha);
            theta_sum += theta[k];
        }

        for (int k = 0; k < data.K; k++) {
            theta[k] = theta[k] / theta_sum;
        }
        //log.info("Theta: " + Arrays.toString(theta));
        return theta;
    }
    
    public double[] definiteTopics(int[][] d, int[] l, PolylingualGibbsData data, int iterations) {
        assert(d.length == l.length);
        double[] theta = new double[data.K];
        int sum = 0;
        
        for(int j = 0; j < d.length; j++) {
            for(int i = 0; i < d[j].length; i++) {
                int bestK = -1;
                int bestKW = -1;
                for(int k = 0; k < data.K; k++) {
                    final Integer v = data.N_lkw[l[j]][k].get(d[j][i]);
                    if(v > bestKW) {
                        bestKW = v;
                        bestK = k;
                    }
                }
                theta[bestK]++;
            }
        }
        
        
        
        for(int k = 0; k < data.K; k++) {
            theta[k] += data.alpha;
            theta[k] /= (data.K * data.alpha + sum);
        }
        
        return theta;
    }

//    public double wordProb(int w, int[] d, GibbsData data, int iterations) {
//        return wordProb(w, topics(d, data, iterations), data);
//    }

    public double wordProb(int w, double[] p, GibbsData data) {
        double prob = 0.0;
        for (int k = 0; k < data.K; k++) {
            prob += p[k] * data.phi(w,k);
        }
        return prob;
    }

    public double wordProb(int w, int[] d, int l, PolylingualGibbsData data, int iterations) {
        return wordProb(w, topics(d, l, data, iterations), data.monolingual(l));
    }

    public double priorWordProb(int w, GibbsData data) {
        double prob = 0.0;
        int sum = 0;
        for (int k = 0; k < data.K; k++) {
            prob += data.N_kw[k].get(w);
            sum += data.N_kw[k].sum();
        }
        return (prob + data.alpha) / (sum + data.K * data.alpha);
    }

    protected int a(int[] z, int k) {
        int sum = 0;
        for (int i = 0; i < z.length; i++) {
            if (z[i] == k) {
                sum++;
            }
        }
        return sum;
    }
    
    protected int a(int[][] zs, int k) {
        int sum = 0;
        for( int j = 0; j < zs.length; j++) {
            for(int i = 0; i < zs[j].length; i++) {
                if(zs[j][i] == k) {
                    sum++;
                }
            }
        }
        return sum;
    }

    protected int b(int[] z, int[] d, int w, int k) {
        int sum = 0;
        for (int i = 0; i < z.length; i++) {
            if (z[i] == k && d[i] == w) {
                sum++;
            }
        }
        return sum;
    }
    private long begin;

    private String timeString(long time) {
        return (time / 3600000) + "h" + (time % 3600000 / 60000) + "m" + (time % 60000 / 1000) + "s";
    }

    private void iterInfo(int iterNo, int iters) {
        long elapsed = System.currentTimeMillis() - begin;
        long expected = (long) (((double) (iters - iterNo) / (double) (iterNo)) * elapsed);
//        log.info("Iteration " + iterNo + "/" + iters + " (Elapsed: " + timeString(elapsed) + " ETA:" + timeString(expected) + ")");
    }
}
 