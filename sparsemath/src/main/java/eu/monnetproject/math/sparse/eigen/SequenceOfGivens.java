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
package eu.monnetproject.math.sparse.eigen;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.*;

/**
 *
 * @author John McCrae
 */
public class SequenceOfGivens {

    private List<Givens> seq = new ArrayList<Givens>();

    public SequenceOfGivens() {
    }
    
    public SequenceOfGivens add(int j, int k, double c, double s) {
        seq.add(new Givens(j, k, c, s));
        return this;
    }
    
    public double[][] applyTo(double[][] matrix) {
        int n = matrix[0].length;
        for(Givens g : seq) {
            for(int i = 0; i < n; i++) {
                final double t = matrix[i][g.j] * g.s + matrix[i][g.k] * g.c;
                matrix[i][g.j] = matrix[i][g.j] * g.c - matrix[i][g.k] * g.s;
                matrix[i][g.k] = t;
            }
        }
        return matrix;
    }
    
    
    
    private static class Givens {
        int j,k;
        double c,s;

        public Givens(int j, int k, double c, double s) {
            this.j = j;
            this.k = k;
            this.c = c;
            this.s = s;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.j;
            hash = 37 * hash + this.k;
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.c) ^ (Double.doubleToLongBits(this.c) >>> 32));
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.s) ^ (Double.doubleToLongBits(this.s) >>> 32));
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
            final Givens other = (Givens) obj;
            if (this.j != other.j) {
                return false;
            }
            if (this.k != other.k) {
                return false;
            }
            if (Double.doubleToLongBits(this.c) != Double.doubleToLongBits(other.c)) {
                return false;
            }
            if (Double.doubleToLongBits(this.s) != Double.doubleToLongBits(other.s)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.seq != null ? this.seq.hashCode() : 0);
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
        final SequenceOfGivens other = (SequenceOfGivens) obj;
        if (this.seq != other.seq && (this.seq == null || !this.seq.equals(other.seq))) {
            return false;
        }
        return true;
    }
    
    
}
