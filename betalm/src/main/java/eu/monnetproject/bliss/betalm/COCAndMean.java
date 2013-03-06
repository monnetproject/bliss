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

import eu.monnetproject.bliss.CLIOpts;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author John McCrae
 */
public class COCAndMean {
    public static Data calculate(Scanner in, int H) {
        final Data data = new Data(H);
        int n = 0;
        while(in.hasNextLine()) {
            final String line = in.nextLine();
            final String[] parts = line.split(" ");
            if(parts.length < 3) {
                System.err.println(line);
                continue;
            }
            final int count = Integer.parseInt(parts[parts.length-1]);
            if(count <= H) {
                data.CoC[count-1]++;
            }
            final double value = Double.parseDouble(parts[parts.length-2]);
            n++;
            data.mean = value / n + data.mean * ((double)(n - 1) / (double)n);
        }
        return data;
    }
    
    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File countedFile = opts.roFile("countFile", "The file with counts");
        final int H = opts.nonNegIntValue("H", "The maximum count of count to store");
        final PrintStream out = opts.outFileOrStdout();
        if(!opts.verify(COCAndMean.class)) {
            return;
        }
        final Scanner in = new Scanner(countedFile);
        final Data data = calculate(in, H);
        out.println(Arrays.toString(data.CoC));
        out.println(data.mean);
    }
    
    public static final class Data {
        public final int[] CoC;
        public double mean;

        public Data(int H ) {
            CoC = new int[H];
        }
    }
}
