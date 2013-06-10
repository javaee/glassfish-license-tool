/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import org.jvnet.licensetool.file.*;
import org.jvnet.licensetool.generic.UnaryBooleanFunction;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Recursively scan directories to process files.
 */
public class Scanner {
    private final List<File> roots;
    //run with dryrun option to check if all the files are recognized.
    private final boolean dryrun;
    private final List<String> patternsToSkip;

    private VCS vcs;

    public Scanner(LicenseTool.Arguments args, final List<File> files) {
        this.roots = files;
        this.dryrun = args.dryrun();
        if (!args.vcs().equals("")) {
            vcs = VCS.valueOf(args.vcs());
        } else {
            List<File> roots1 = args.roots();
            vcs = roots1.size() > 0 ? VCS.sniffVCS(roots1.get(0)) : null;
        }

        patternsToSkip = new ArrayList<String>();
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
                if (parser == null) {
                    LOGGER.warning("Unrecognized file: " + fw);
                    if (!dryrun) {
                        return false;
                    }
                }
                if (!dryrun) {
                    ParsedFile pfile = parser.parseFile(fw);
                    if (pfile != null) {
                        pfile.setVCS(vcs);
                        result = action.evaluate(pfile);
                    }
                }
            } catch (IOException exc) {
                LOGGER.warning("Exception while processing file " + fw + ": " + exc);
                exc.printStackTrace();
                return false;
            } catch (Exception exc) {
                LOGGER.warning("Exception while processing file " + fw + ": " + exc);
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
                LOGGER.fine("Scanner: Skipping directory "
                        + absPath + "(pattern " + pattern + ")");
                return true;
            }
        }
        LOGGER.fine("Scanner: Not skipping directory " + file);
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
                LOGGER.fine("fname " + fname
                        + " matched on pattern " + pattern);
                return true;
            }
        }

        return false;
    }

    private static Logger LOGGER = Logger.getLogger(Scanner.class.getName());
}
