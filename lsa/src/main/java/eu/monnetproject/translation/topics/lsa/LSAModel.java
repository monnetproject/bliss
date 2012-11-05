/*********************************************************************************
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
package eu.monnetproject.translation.topics.lsa;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 *
 * @author John McCrae
 */
public class LSAModel {

    public final double[][] U1;
    public final double[][] U2;
    public final double[] S;
    public final int K;
    public final int W;

    public LSAModel(double[][] U1, double[][] U2, double[] S, int K, int W) {
        this.U1 = U1;
        this.U2 = U2;
        this.S = S;
        this.K = K;
        this.W = W;
        assert(K == U1.length);
        assert(K == U2.length);
        assert(K == S.length);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Arrays.deepHashCode(this.U1);
        hash = 19 * hash + Arrays.deepHashCode(this.U2);
        hash = 19 * hash + Arrays.hashCode(this.S);
        hash = 19 * hash + this.K;
        hash = 19 * hash + this.W;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LSAModel other = (LSAModel) obj;
        if (!Arrays.deepEquals(this.U1, other.U1)) {
            return false;
        }
        if (!Arrays.deepEquals(this.U2, other.U2)) {
            return false;
        }
        if (!Arrays.equals(this.S, other.S)) {
            return false;
        }
        if (this.K != other.K) {
            return false;
        }
        if (this.W != other.W) {
            return false;
        }
        return true;
    }

    public static LSAModel load(InputStream inputStream) throws IOException {
        final DataInputStream in = new DataInputStream(inputStream);
        final int K = in.readInt();
        final int W2 = in.readInt();
        final int W = W2/2;
        //final int J = in.readInt();
        final double[][] U1 = new double[K][W];
        final double[][] U2 = new double[K][W];
        for(int k = 0; k < K; k++) {
            for(int w = 0; w < W; w++) {
                U1[k][w] = in.readDouble();
            }
            for(int w = W; w < 2* W; w++) {
                U2[k][w-W] = in.readDouble();
            }
        }
        final double[] S = new double[K];
        for(int k =0; k < K; k++) {
            try {
                S[k] = in.readDouble();
                System.err.println("S["+k+"]="+S[k]);
            } catch(EOFException x) {
                System.err.println("K="+k);
                throw x;
            }
        }
        in.close();
        return new LSAModel(U1, U2, S, K, W);
    }
    
    @Override
    public String toString() {
        return "LSAModel{" + "U1=" + U1 + ", U2=" + U2 + ", S=" + S + ", K=" + K + ", W=" + W + '}';
    }

}
