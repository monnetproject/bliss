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
package eu.monnetproject.bliss.betalm;

import eu.monnetproject.bliss.betalm.impl.BetaLMImpl;
import eu.monnetproject.math.sparse.RealVector;
import eu.monnetproject.math.sparse.Vector;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author John McCrae
 */
public class PrecomputedValues {

    public int W;
    public int J;
    public Vector<Double> df;
    public Vector<Double> mu;
    public double sumMu2;

    public static boolean isNecessary(BetaLMImpl.Method method) {
        switch (method) {
            case DF_DICE:
                return true;
            case DF_JACCARD:
                return true;
            case NORMAL_COS_SIM:
                return true;
            default:
                return false;
        }
    }

    /**
     * Compute general purpose statistics
     * @param corpus The corpus
     * @param W The number of unique tokens
     * @param type If SIMPLE then use all documents, If USE_FIRST then use second !!, If USE_SECOND then use first !!
     * @return The counts
     * @throws IOException If any of many things go wrong
     */
    public static PrecomputedValues precompute(File corpus, int W, SourceType type) throws IOException {
        final PrecomputedValues p = new PrecomputedValues();
        p.W = W;
        p.df = new RealVector(W);
        p.mu = new RealVector(W);
        final DataInputStream dis = new DataInputStream(new FileInputStream(corpus));
        boolean inDoc = type != SourceType.FIRST;
        final IntOpenHashSet seen = new IntOpenHashSet();
        long read = 0;
        while (dis.available() > 0) {
            int tk = dis.readInt();
            if (tk == 0) {
                if (type == SourceType.SIMPLE) {
                    p.J++;
                    seen.clear();
                } else {
                    if (inDoc) {
                        p.J++;
                        seen.clear();
                        inDoc = false;
                    } else {
                        inDoc = true;
                    }
                }
            } else if (inDoc) {
                if (tk < W) {
                    p.mu.add(tk,1);
                    if (!seen.contains(tk)) {
                        p.df.add(tk,1);
                        seen.add(tk);
                    }
                }
            }
            if (++read % 1048576 == 0) {
                System.err.print(".");
            }

        }
        System.err.println();

        for(int i = 0; i < W; i++) {
            p.mu.divide(i, read);
            p.df.divide(i, p.J);
            p.sumMu2 += p.mu.doubleValue(i) *p.mu.doubleValue(i);
        }
        return p;
    }
}
