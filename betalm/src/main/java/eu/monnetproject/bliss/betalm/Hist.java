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
public class Hist {

    public static void main(String[] args) throws Exception {
        final CLIOpts opts = new CLIOpts(args);
        final File histFile = opts.roFile("history", "The history list");
        final boolean future = opts.flag("future", "Future history format");
        final PrintStream out = opts.outFileOrStdout();
        if(!opts.verify(Hist.class)) {
            return;
        }
        hist(histFile, future, out);
    }

    private static String futureValue(String line) {
        return line.substring(line.indexOf("["),line.indexOf("]")+1);
    }

    private static String historyValue(String line) {
        if(line.lastIndexOf(",") < 0) {
            throw new IllegalArgumentException("Don't do this on one-grams silly!");
        } else {
            return line.substring(0,line.lastIndexOf(","))+"]";
        }
    }

    public static void hist(final File histFile, final boolean future, final PrintStream out) throws FileNotFoundException {
        final Scanner in = new Scanner(histFile);
        String lastValue = null;
        int histCount = 0;
        double s = 0.0;
        int read = 0;
        while(in.hasNextLine()) {
            if(++read % 100000 == 0) {
                System.err.print(".");
            }
            final String line = in.nextLine();
            final String value;
            if(future) {
                value = futureValue(line);
            } else {
                value = historyValue(line);
            }
            final String[] ss = line.split(" ");
            final double score = Double.parseDouble(ss[ss.length-2]);
            if(lastValue == null) {
                lastValue = value;
                histCount++;
                s += score;
            } else if(!value.equals(lastValue)) {
                out.println(lastValue + " " + histCount + " " + s);
                histCount = 1;
                s = score;
                lastValue = value;
            } else {
                s += score;
                histCount++;
            }
        }
        System.err.println();
        if(lastValue != null) {
                out.println(lastValue + " " + histCount + " " + s);
        }
        out.flush();
        out.close();
        in.close();
    }
}
