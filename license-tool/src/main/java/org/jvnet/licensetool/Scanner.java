/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.licensetool;

import org.jvnet.licensetool.generic.UnaryBooleanFunction;
import org.jvnet.licensetool.file.ParsedFile;
import org.jvnet.licensetool.file.FileParser;
import org.jvnet.licensetool.file.FileRecognizer;
import org.jvnet.licensetool.file.FileWrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Arrays;

/**
 * Recursively scan directories to process files.
 */
public class Scanner {
    private final List<File> roots;
    private final boolean verbose;
    //run with dryrun option to check if all the files are recognized.
    private final boolean dryrun;
    private final List<String> patternsToSkip;

    public Scanner(boolean verbose, boolean dryrun, final List<File> files) {
        this.roots = files;
        this.verbose = verbose;
        this.dryrun = dryrun;
        patternsToSkip = new ArrayList<String>();
    }

    public Scanner(final boolean verbose, final boolean dryrun, final File... files) {
        this(verbose, dryrun, Arrays.asList(files));
    }

    /**
     * Add a pattern that defines a directory to skip.  We only need really simple
     * patterns: just a single name that must match a component of a directory name
     * exactly.
     */
    public void addDirectoryToSkip(final String pattern) {
        patternsToSkip.add(pattern);
    }

    /**
     * Action interface passed to scan method to act on files.
     * Terminates scan if it returns false.
     */
    public interface Action extends UnaryBooleanFunction<ParsedFile> {
    }

    /**
     * Scan all files reachable from roots.  Does a depth-first search.
     * Ignores all directories (and their contents) that match an entry
     * in patternsToSkip.  Passes each file (not directories) to the action.
     * If action returns false, scan terminates.  The result of the scan is
     * the result of the last action call.
     */
    public boolean scan(final FileRecognizer recognizer, final Scanner.Action action) {
        boolean result = true;
        for (File file : roots) {
            result = doScan(file, recognizer, action);
            if (!result)
                break;
        }
        return result;
    }

    private boolean doScan(final File file, final FileRecognizer recognizer, final Scanner.Action action) {
        boolean result = true;
        if (file.isDirectory()) {
            if (!skipDirectory(file)) {
                for (File f : file.listFiles()) {
                    result = doScan(f, recognizer, action);
                    if (!result)
                        break;
                }
            }
        } else {
            final FileWrapper fw = new FileWrapper(file);
            try {
                FileParser parser = recognizer.getParser(fw);
                if(parser == null) {
                    System.out.println("Unrecognized file: " + fw);
                    return false;
                }
                if(!dryrun) {
                    ParsedFile pfile = parser.parseFile(fw);
                    if(pfile != null)
                        result = action.evaluate(pfile);
                }
            } catch (IOException exc) {
                System.out.println("Exception while processing file " + fw + ": " + exc);
                exc.printStackTrace();
                return false;
            } catch (Exception exc) {
                System.out.println("Exception while processing file " + fw + ": " + exc);
                exc.printStackTrace();
                return false;
            } finally {
                fw.close();
            }

        }
        return result;
    }


    private boolean skipDirectory(final File file) {
        for (String pattern : patternsToSkip) {
            String absPath = file.getAbsolutePath();
            if (match(pattern, absPath)) {
                if (verbose)
                    System.out.println("Scanner: Skipping directory "
                            + absPath + "(pattern " + pattern + ")");
                return true;
            }
        }

        if (verbose)
            System.out.println("Scanner: Not skipping directory " + file);
        return false;
    }

    // This where we could support more complex pattern matches, if desired.
    private boolean match(final String pattern, final String fname) {
        final String separator = File.separator;

        // Don't use String.split here because its argument is a regular expression,
        // and some file separator characters could be confused with regex meta-characters.
        final StringTokenizer st = new StringTokenizer(fname, separator);
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            if (pattern.equals(token)) {
                if (verbose)
                    System.out.println("fname " + fname
                            + " matched on pattern " + pattern);
                return true;
            }
        }

        return false;
    }
}
