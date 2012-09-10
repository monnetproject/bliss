/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.translation.topics.wikipedia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author John McCrae
 */
public class AlignWikiFiles {

    public static void align(File inFile1, File inFile2, File outFile1, File outFile2) throws IOException{
        final BufferedReader in1 = new BufferedReader(new FileReader(inFile1));
        final BufferedReader in2 = new BufferedReader(new FileReader(inFile2));
        final PrintWriter out1 = new PrintWriter(outFile1);
        final PrintWriter out2 = new PrintWriter(outFile2);
        String s1 = in1.readLine(), s2 = in2.readLine();
        int i = 0;
        while(s1 != null && s2 != null) {
            if(s1.indexOf(":") != -1 && s2.indexOf(":") != -1) {
                final String title1 = s1.substring(0,s1.indexOf(":"));
                final String title2 = s2.substring(0,s2.indexOf(":"));
                if(title1.equals(title2)) {
                    out1.println(s1.substring(s1.indexOf(":")+1));
                    out2.println(s2.substring(s2.indexOf(":")+1));
                    s1 = in1.readLine();
                    s2 = in2.readLine();
                    continue;
                }
            }
            final int sgn = s1.compareTo(s2);
            if(sgn < 0) {
                s1 = in1.readLine();
            } else if(sgn > 0) {
                s2 = in2.readLine();
            } else {
                s1 = in1.readLine();
                s2 = in2.readLine();
            }
            if(++i % 1000 == 0) {
                System.err.print(".");
            }
        }
        in1.close();
        in2.close();
        out1.close();
        out2.close();
    }
    
    public static void main(String[] args) throws Exception {
        if(args.length != 4) {
            throw new IllegalArgumentException("Usage: inFile1 inFile2 outFile1 outFile2");
        }
        align(new File(args[0]), new File(args[1]), new File(args[2]), new File(args[3]));
    }
}
