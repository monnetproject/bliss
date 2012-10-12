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
package eu.monnetproject.math.sparse.eigen;

import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import eu.monnetproject.math.sparse.VectorFunction;
import eu.monnetproject.translation.topics.CLIOpts;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 * An implementation of SVD for super-large sparse matrices. For smaller
 * problems use a standard implementation (Apache Commons Math)
 *
 * @author John McCrae
 */
public class SingularValueDecomposition {

    public void calculate(File matrixFile, int W, int J, int K, double epsilon) {
        final LanczosAlgorithm.Solution iLanczos = LanczosAlgorithm.lanczos(new InnerProductMultiplication(matrixFile, W), randomUnitNormVector(J), K);
        final QRAlgorithm.Solution iQrSolve = QRAlgorithm.qrSolve(epsilon, iLanczos.tridiagonal(), null);
        final double[][] iEigens = iQrSolve.givensSeq().applyTo(iLanczos.q());
        
        
        final LanczosAlgorithm.Solution oLanczos = LanczosAlgorithm.lanczos(new OuterProductMultiplication(matrixFile, J), randomUnitNormVector(W), K);
        final QRAlgorithm.Solution oQrSolve = QRAlgorithm.qrSolve(epsilon, oLanczos.tridiagonal(), null);
        final double[][] oEigens = oQrSolve.givensSeq().applyTo(oLanczos.q());
        
        // Compare vectors and output solution
    }

    private Vector<Double> randomUnitNormVector(int J) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static class InnerProductMultiplication implements VectorFunction<Double> {

        private final File matrixFile;
        private final int W;

        public InnerProductMultiplication(File matrixFile, int W) {
            this.matrixFile = matrixFile;
            this.W = W;
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            try {
                double[] mid = new double[W];
                int n = 0;
                DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if(i != 0) {
                            mid[i] += v.doubleValue(n);
                        } else {
                            n++;
                        }
                    } catch(EOFException x) {
                        break;
                    }
                }
                data.close();
                n = 0;
                double[] a = new double[v.length()];
                data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if(i != 0) {
                            a[n] += mid[i];
                        } else {
                            n++;
                        }
                    } catch(EOFException x) {
                        break;
                    }
                }
                data.close();
                return new RealVector(a);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }

        }
    }
    
    private static class OuterProductMultiplication implements VectorFunction<Double> {
        
        private final File matrixFile;
        private final int J;

        public OuterProductMultiplication(File matrixFile, int J) {
            this.matrixFile = matrixFile;
            this.J = J;
        }

        @Override
        public Vector<Double> apply(Vector<Double> v) {
            try {
                double[] mid = new double[J];
                int n = 0;
                DataInputStream data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if(i != 0) {
                            mid[n] += v.doubleValue(i);
                        } else {
                            n++;
                        }
                    } catch(EOFException x) {
                        break;
                    }
                }
                data.close();
                n = 0;
                double[] a = new double[v.length()];
                data = new DataInputStream(CLIOpts.openInputAsMaybeZipped(matrixFile));
                while (data.available() > 0) {
                    try {
                        int i = data.readInt();
                        if(i != 0) {
                            a[i] += mid[n];
                        } else {
                            n++;
                        }
                    } catch(EOFException x) {
                        break;
                    }
                }
                data.close();
                return new RealVector(a);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }

        }
    }
}
