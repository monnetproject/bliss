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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 *
 * @author jmccrae
 */
public class Uniq {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File file = opts.roFile("file", "The file to count uniqueness among");
        final PrintStream out = opts.outFileOrStdout();
        if (!opts.verify(Uniq.class)) {
            return;
        }
        uniq(file, out);
    }

    public static void uniq(final File file, final PrintStream out) throws FileNotFoundException, NumberFormatException {
        final Scanner in = new Scanner(file);
        double s = 0.0;
        int n = 0;
        String lastValue = null;
        int read = 0;
        while (in.hasNextLine()) {
            if(++read % 100000 == 0) {
                System.err.print(".");
            }
            final String line = in.nextLine();
            final int splitAt = line.lastIndexOf(' ');
            if (splitAt > 0) {
                final String value = line.substring(0, splitAt);
                final double score = Double.parseDouble(line.substring(splitAt));
                if(lastValue != null && !value.equals(lastValue)) {
                    out.println(lastValue + " " + s + " " + n);
                    lastValue = value;
                    s = score;
                    n = 1;
                } else if(lastValue == null) {
                    lastValue = value;
                    s += score;
                    n++;
                } else {
                    s += score;
                    n++;
                }
            }
        }
        System.err.println();
        if(lastValue != null) {
            out.println(lastValue + " " + s + " " + n);
        }
        out.flush();
        out.close();
    }
}
