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

import eu.monnetproject.bliss.betalm.impl.BetaSimFunction;
import eu.monnetproject.math.sparse.SparseIntArray;
import eu.monnetproject.bliss.CLIOpts;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jmccrae
 */
public class CalculateBetaMean {
    public static double calcBetaMean(BetaSimFunction function, File corpus, SourceType sourceType, int W) throws IOException {
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpus));
        
        double mean = 0.0;
        int docsRead = 0;
        final IntArrayList doc = new IntArrayList();
        while(true) {
            try {
                final int w = in.readInt();
                if(w == 0) {
                    if((docsRead % 2 == 1 && sourceType == SourceType.FIRST) ||
                            (docsRead % 2 == 0 && sourceType == SourceType.SECOND) ||
                            sourceType == SourceType.SIMPLE) {
                        final double score = function.scoreNGrams(doc,W);
                        docsRead++;
                        mean = score / docsRead + ((double)(docsRead-1) / (double)docsRead) * mean;
                        doc.clear();  
                    } else {
                        docsRead++;
                        doc.clear();
                    }
                } else {
                    doc.add(w);
                }
            } catch(EOFException x) {
                break;
            }
        }
        return mean;
    }

}
