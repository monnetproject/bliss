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
package eu.monnetproject.bliss.lsa;

import eu.monnetproject.bliss.NGramSimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetric;
import eu.monnetproject.bliss.SimilarityMetricFactory;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author John McCrae
 */
public class LSASimilarityMetricFactory implements SimilarityMetricFactory<InputStream> {
    
    
    @Override
    public SimilarityMetric makeMetric(InputStream data, int W) throws IOException {
        final LSAModel model = LSAModel.load(data);
        
        if(W != model.W) {
            throw new IllegalArgumentException("W="+W+" but matrix has " + model.W + " rows");
        }
        return new LSASimilarityMetric(model.U1,model.U2, model.S);
    }

    @Override
    public NGramSimilarityMetric makeNGramMetric(InputStream dat, int W) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
    @Override
    public Class<InputStream> datatype() {
        return InputStream.class;
    }
    

}
