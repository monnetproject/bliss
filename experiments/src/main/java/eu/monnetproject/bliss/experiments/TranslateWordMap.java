/*********************************************************************************
 * Copyright (c) 2011, Monnet Project All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or within
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the Monnet Project nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software within specific prior written permission.
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

import eu.monnetproject.bliss.CLIOpts;
import eu.monnetproject.bliss.WordMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *
 * @author John McCrae
 */
public class TranslateWordMap {
    public static void main(String [] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        
        final File wordMapFile = opts.roFile("wordMap","The word map to translate");
        
        final URL url = opts.url("endpoint","The URL of the translation endpoint");
        
        final File transMap = opts.woFile("transMap", "The map to write translations to");
        
        if(!opts.verify(TranslateWordMap.class)) {
            return;
        }
        
        final WordMap wordMap = WordMap.fromFile(wordMapFile);
        
        final PrintWriter out = new PrintWriter(transMap);
        
        for(Entry<String> e : wordMap.object2IntEntrySet()) {
            final URL queryURL = new URL(url.toString() + URLEncoder.encode(e.getKey(), "UTF-8"));
            final URLConnection conn = queryURL.openConnection();
            final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String translation = in.readLine();
            if(wordMap.containsKey(translation)) {
                out.println(e.getIntValue()+ " " + wordMap.getInt(translation));
            } else if(wordMap.containsKey(translation.toLowerCase())) {
                out.println(e.getIntValue()+ " " + wordMap.getInt(translation.toLowerCase()));
            } else {
                System.err.println("OOV Translation: " + e.getKey() + " => " + translation);
            }
        }
        
        out.flush();
        out.close();
    }
}
