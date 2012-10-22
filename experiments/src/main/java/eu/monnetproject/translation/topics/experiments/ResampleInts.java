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
package eu.monnetproject.translation.topics.experiments;

import eu.monnetproject.translation.topics.CLIOpts;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author John McCrae
 */
public class ResampleInts {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File corpus1File = opts.roFile("corpus1[.gz|.bz2]", "The (train) corpus");
        final File corpus2File = opts.roFile("corpus2[.gz|.bz2]", "The (test) corpus");
        final File wordMapFile = opts.roFile("wordMap", "The previous word map");
        final File newWordMapFile = opts.woFile("newWordMap", "The new word map");
        final File corpus1OutFile = opts.woFile("corpus1out[.gz|.bz2]", "The (train) corpus out file");
        final File corpus2OutFile = opts.woFile("corpus1out[.gz|.bz2]", "The (test) corpus out file");

        if (!opts.verify(ResampleInts.class)) {
            return;
        }
        final IntRBTreeSet words = new IntRBTreeSet();
        System.err.println("Reading train corpus");
        readCorpus(corpus1File, words);
        System.err.println("Reading test corpus");
        readCorpus(corpus2File, words);
        System.err.println("Building downsampling index");
        Int2IntRBTreeMap old2new = buildIndex(words);
        System.err.println("Writing new word map");
            rewriteWordMap(wordMapFile, newWordMapFile, words, old2new);
        System.err.println("Writing new train corpus");
        rewriteCorpus(corpus1File, corpus1OutFile, old2new);
        System.err.println("Writing new test corpus");
        rewriteCorpus(corpus2File, corpus2OutFile, old2new);
        System.err.println("W=" + (words.size()+1));

    }

    private static void readCorpus(final File corpusFile, final IntSortedSet words) throws IOException {
        final DataInputStream dis = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpusFile));
        while (dis.available() > 0) {
            try {
                words.add(dis.readInt());
            } catch (EOFException x) {
                break;
            }

        }
        dis.close();
    }

    private static void rewriteCorpus(final File corpusFile, final File corpusOutFile, final Int2IntMap old2new) throws IOException {
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(corpusFile));
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(corpusOutFile));
        while (in.available() > 0) {
            try {
                int w = in.readInt();
                out.writeInt(old2new.get(w));
            } catch (EOFException x) {
                break;
            }
        }
        in.close();
        out.flush();
        out.close();
    }

    private static void rewriteWordMap(final File wordMapFile, final File newWordMapFile, final IntRBTreeSet words, final Int2IntRBTreeMap old2new) throws IOException {
        final DataInputStream in = new DataInputStream(CLIOpts.openInputAsMaybeZipped(wordMapFile));
        final DataOutputStream out = new DataOutputStream(CLIOpts.openOutputAsMaybeZipped(newWordMapFile));
        while (in.available() > 0) {
            try {
                String key = in.readUTF();
                int w = in.readInt();
                if (words.contains(w)) {
                    out.writeUTF(key);
                    out.writeInt(old2new.get(w));
                }
            } catch (EOFException x) {
                break;
            }
        }
        out.flush();
        out.close();
        in.close();
    }

    private static Int2IntRBTreeMap buildIndex(final IntRBTreeSet words) {
        final Int2IntRBTreeMap old2new = new Int2IntRBTreeMap();
        int i = 0;
        final IntBidirectionalIterator iter = words.iterator();
        while (iter.hasNext()) {
            int w = iter.nextInt();
            old2new.put(w, i++);
        }
        return old2new;
    }
}
