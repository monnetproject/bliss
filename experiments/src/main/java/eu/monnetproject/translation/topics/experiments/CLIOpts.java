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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 *
 * @author John McCrae
 */
public class CLIOpts {

    private final List<String> args;
    private final List<Argument> argObjs = new ArrayList<Argument>();
    private boolean succeeded = true;

    public CLIOpts(String[] args) {
        this.args = new ArrayList<String>(Arrays.asList(args));
    }

    /**
     * Call this after calling all CLIOpts to verify the CLIOpts are valid
     *
     * @param cl The class of the script
     * @return {@code true} if the CLIOpts are valid
     */
    public boolean verify(Class<?> cl) {
        if (!succeeded || !args.isEmpty()) {
            if (!args.isEmpty()) {
                System.err.println("Too many arguments");
            }
            for (Argument argObj : argObjs) {
                if (argObj.message != null) {
                    System.err.println(argObj.message);
                }
            }
            System.err.print("\nUsage:\n"
                    + "\tmvn exec:java -Dexec.mainClass=\"" + cl.getName() + "\" -Dexec.args=\"");
            for (int i = 0; i < argObjs.size(); i++) {
                if (argObjs.get(i).optional) {
                    System.err.print("[");
                }
                if (argObjs.get(i).flag != null) {
                    System.err.print("-" + argObjs.get(i).flag + " ");
                }
                System.err.print(argObjs.get(i).name);
                if (argObjs.get(i).optional) {
                    System.err.print("]");
                }
                if (i + 1 != argObjs.size()) {
                    System.err.print(" ");
                }
            }
            System.err.println("\"");
            for (Argument argObj : argObjs) {
                System.err.println("\t  * " + argObj.name + ": " + argObj.description);
            }
            return false;
        } else {
            return true;
        }
    }

    public File roFile(String name, String description) {
        final Argument arg = new Argument(name, null, description, false);
        argObjs.add(arg);
        if(args.isEmpty()) {
            arg.message = "Too few arguments: expected " + name;
            succeeded = false;
            return null;
        } else {
            final File file = new File(args.get(0));
            if (!file.exists() || !file.canRead()) {
                arg.message = "Cannot access [" + file.getPath() + "] for " + name;
                succeeded = false;
                args.remove(0);
                return null;
            }
            args.remove(0);
            return file;
        }
    }

    public File woFile(String name, String description) {
        final Argument arg = new Argument(name, null, description, false);
        argObjs.add(arg);
        if (args.isEmpty()) {
            arg.message = "Too few arguments: expected " + name;
            succeeded = false;
            return null;
        } else {
            final File file = new File(args.get(0));
            if (file.exists() && !file.canWrite()) {
                arg.message = "Cannot access [" + file.getPath() + "] for " + name;
                succeeded = false;
                args.remove(0);
                return null;
            }
            args.remove(0);
            return file;
        }
    }
    
    public PrintStream outFileOrStdout()  {
        final Argument arg = new Argument("out", null, "The out file or nothing to use STDOUT", true);
        argObjs.add(arg);
        if (args.isEmpty()) {;
            return System.out;
        } else {
            final File file = new File(args.get(0));
            if (file.exists() && !file.canWrite()) {
                arg.message = "Cannot access [" + file.getPath() + "] for out";
                succeeded = false;
                args.remove(0);
                return null;
            }
            args.remove(0);
            try {
                return new PrintStream(file);
            } catch(FileNotFoundException x) {
                return null;
            }
        }
    }

    public int intValue(String name, String description) {
        final Argument arg = new Argument(name, null, description, false);
        argObjs.add(arg);
        if (args.isEmpty()) {
            arg.message = "Too few arguments: expected " + name;
            succeeded = false;
            return 0;
        } else {
            final int i;
            try {
                i = Integer.parseInt(args.get(0));
            } catch (NumberFormatException x) {
                arg.message = "Not an integer " + args.get(0) + " for " + name;
                succeeded = false;
                args.remove(0);
                return 0;
            }
            args.remove(0);
            return i;
        }
    }

    @SuppressWarnings("unchecked")
    public <C> Class<C> clazz(String name, Class<C> interfase, String description) {
        final Argument arg = new Argument(name, null, description, false);
        argObjs.add(arg);
        if (args.isEmpty()) {
            arg.message = "Too few arguments: expected " + name;
            succeeded = false;
            return null;
        } else {
            final Class<?> clazz;
            try {
                clazz = Class.forName(args.get(0));
            } catch (ClassNotFoundException x) {
                arg.message = "Class not found: " + args.get(0);
                succeeded = false;
                args.remove(0);
                return null;
            }
            if (!interfase.isAssignableFrom(clazz)) {
                arg.message = clazz.getName() + " must implement " + interfase.getName();
                succeeded = false;
                args.remove(0);
                return null;
            }
            args.remove(0);
            return (Class<C>)clazz;
        }
    }

    /**
     * Return a file as an input stream, that unzips if the file ends in .gz or
     * .bz2.
     *
     * @param file The file
     * @return File as an input stream
     * @throws IOException If the file is not found or is not a correct zipped
     * file or some other reason
     */
    public static InputStream openInputAsMaybeZipped(File file) throws IOException {
        if (file.getName().endsWith(".gz")) {
            return new GZIPInputStream(new FileInputStream(file));
        } else if (file.getName().endsWith(".bz2")) {
            return new BZip2CompressorInputStream(new FileInputStream(file));
        } else {
            return new FileInputStream(file);
        }
    }

    /**
     * Return a file as an output stream, that zips if the file ends in .gz or
     * .bz2.
     *
     * @param file The file
     * @return File as an output stream
     * @throws IOException If the file is not found or some other reason
     */
    public static OutputStream openOutputAsMaybeZipped(File file) throws IOException {
        if (file.getName().endsWith(".gz")) {
            return new GZIPOutputStream(new FileOutputStream(file));
        } else if (file.getName().endsWith(".bz2")) {
            return new BZip2CompressorOutputStream(new FileOutputStream(file));
        } else {
            return new FileOutputStream(file);
        }
    }

    private static final class Argument {

        final String name;
        final String flag;
        final String description;
        final boolean optional;
        String message;

        public Argument(String name, String flag, String description, boolean optional) {
            this.name = name;
            this.flag = flag;
            this.description = description;
            this.optional = optional;
        }
    }
}
