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
package eu.monnetproject.bliss.experiments;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.ParallelBinarizedReader;
import eu.monnetproject.bliss.WordMap;
import eu.monnetproject.math.sparse.SparseIntArray;

/**
 * 
 * @author John McCrae
 */
public class PerceptronNormalization {

	public static void main(String[] args) throws Exception {
		final CLIOpts opts = new CLIOpts(args);
		final File corpus = opts.roFile("corpus", "The corpus");
		final File wordMapFile = opts.roFile("wordMap", "The word map");
		final int J = opts.intValue("J", "The number of documents to handle");
		final PrintStream out = opts.outFileOrStdout();
		if (!opts.verify(PerceptronNormalization.class)) {
			return;
		}
		final int W = WordMap.calcW(wordMapFile);
		final double[][] wts = new double[W][2];
		for(int l = 0; l < 2; l++) {
			for(int w = 0; w < W; w++) {
				wts[w][l] = 1.0 / Math.sqrt(W);
			}
		}

		final ParallelBinarizedReader slowIn = new ParallelBinarizedReader(
				CLIOpts.openInputAsMaybeZipped(corpus));
		for (int j = 0; j < J; j++) {
			final ParallelBinarizedReader fastIn = new ParallelBinarizedReader(
					CLIOpts.openInputAsMaybeZipped(corpus));
			final SparseIntArray[] doc1 = slowIn.nextFreqPair(W);
			double[] mean1 = new double[2];
			for (int l = 0; l < 2; l++) {
				for (int w : doc1[l].keySet()) {
					mean1[l] += doc1[l].doubleValue(w) * doc1[l].doubleValue(w) * wts[w][l];
				}
				mean1[l] = Math.sqrt(mean1[l]);
			}

			for (int j2 = 0; j < J; j++) {
				final SparseIntArray[] doc2 = fastIn.nextFreqPair(W);
				if (j != j2) {
					double[] mean2 = new double[2];
					for (int l = 0; l < 2; l++) {
						for (int w : doc2[l].keySet()) {
							mean2[l] += doc2[l].doubleValue(w)
									* doc2[l].doubleValue(w) * wts[w][l];
						}
						mean2[l] = Math.sqrt(mean2[l]);
					}
					for (int l = 0; l < 2; l++) {
						double objective = 0.0;
						final SparseIntArray v = new SparseIntArray(W);
						for (int w : doc2[l].keySet()) {
							double d = doc2[l].doubleValue(w)
									* doc2[l].doubleValue(w) / mean1[l]
									- doc1[l].doubleValue(w)
									* doc2[l].doubleValue(w) / mean2[l];
							d *= wts[w][l];
							v.add(w, d);
							objective += d;
						}
						objective /= mean1[l];
						mean1[l] *= mean1[l];
						mean2[l] *= mean2[l];
						for (int w : v.keySet()) {
							double delta = v.doubleValue(w) * (1.0 - objective);
							mean1[l] += doc1[l].doubleValue(w) * delta;
							mean2[l] += doc2[l].doubleValue(w) * delta;
							wts[w][l] += delta;
						}
						mean1[l] = Math.sqrt(mean1[l]);
						mean2[l] = Math.sqrt(mean2[l]);
					}
				}
			}
			fastIn.close();
			for (int l = 0; l < 2; l++) {
				double norm = 0.0;
				for (int w = 0; w < W; w++) {
					norm += wts[w][l] * wts[w][l];
				}
				norm = Math.sqrt(norm);
				for (int w = 0; w < W; w++) {
					wts[w][l] /= norm;
				}
			}
		}
		slowIn.close();
		for (int w = 0; w < W; w++) {
			out.println(wts[w][0] + "," + wts[w][1]);
		}
		out.flush();
		out.close();
	}
}
