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
package eu.monnetproject.translation.topics;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * An index of words used to integerize language data. Note, if you add only
 * using {@code offer()} the largest value in the map will be exactly the {@code size()}
 * of the map.
 *
 * @author John McCrae
 */
@SuppressWarnings("serial")
public class WordMap extends Object2IntOpenHashMap<String> {

    /**
     * Load a word map from a file
     *
     * @param file The file containing the word map
     * @return A new word map instance
     * @throws IOException If the file cannot be read
     */
    public static WordMap fromFile(File file) throws IOException {
        final WordMap wordMap = new WordMap();
        final DataInputStream in = new DataInputStream(new FileInputStream(file));
        try {
            while (in.available() > 0) {
                final String key = in.readUTF();
                final int idx = in.readInt();
                wordMap.put(key, idx);
            }
        } finally {
            in.close();
        }
        return wordMap;
    }

    /**
     * Write a word map to a file
     *
     * @param file The file to write the map to
     * @throws IOException If the file cannot be written
     */
    public void write(File file) throws IOException {
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        final ObjectIterator<Entry<String>> iter = super.object2IntEntrySet().fastIterator();
        try {
            while (iter.hasNext()) {
                final Entry<String> entry = iter.next();
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getIntValue());
            }
        } finally {
            out.close();
        }
    }
    
    /**
     * Add a (potentially new) element to the map
     * @param token The element
     * @return The index assigned to it
     */
    public int offer(String token) {
        if(super.containsKey(token)) {
            return super.getInt(token);
        } else {
            int w = super.size() + 1;
            super.put(token, w);
            return w;
        }
    }

    /**
     * Verify that all entry values are less than or equal to size()
     * @return True if the word map still has integrity
     */
    public boolean verifyIntegrity() {
        final ObjectIterator<Entry<String>> iter = super.object2IntEntrySet().fastIterator();
        final int N = this.size();
        while (iter.hasNext()) {
            final Entry<String> entry = iter.next();
            if(entry.getIntValue() > N) {
                return false;
            }
        }
        return true;
    }
    
    public String[] invert() {
        int max = 0;
        for(int i : super.values) {
            max = Math.max(max, i);
        }
        final String[] inverseMap = new String[max+1];
        final ObjectIterator<Entry<String>> iter = super.object2IntEntrySet().fastIterator();
        while(iter.hasNext()) {
            final Entry<String> entry = iter.next();
            inverseMap[entry.getIntValue()] = entry.getKey();
        }
        return inverseMap;
    }

    /**
     * @deprecated use offer instead
     */
    @Deprecated
    @Override
    public int put(String k, int v) {
        return super.put(k, v);
    }

    /**
     * @deprecated use offer instead
     */
    @Deprecated
    @Override
    public Integer put(String ok, Integer ov) {
        return super.put(ok, ov);
    }
}
