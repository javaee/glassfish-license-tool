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

import static org.jvnet.licensetool.Constants.*;

import java.io.IOException;

public class RecognizerFactory {
    public FileRecognizer getDefaultRecognizer() {
        CompositeRecognizer recognizer = new CompositeRecognizer();
        // Configure the recognizer

        // Java
        for (String suffix : JAVA_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix, createRecognizer(suffix, JAVA_COMMENT_START,
                    JAVA_COMMENT_END,JAVA_COMMENT_PREFIX, false, InsertActionFactory.JAVA_INSERT_COMMENT_ACTION));
        }

        // Java line
	    for (String suffix : JAVA_LINE_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix, createRecognizer(suffix, JAVA_LINE_PREFIX, false, ParsedFile.DEFAULT_INSERT_ACTION));
	    }

        // XML
        for (String suffix : XML_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix, createRecognizer(suffix, XML_COMMENT_START,
                    XML_COMMENT_END, XML_COMMENT_PREFIX, true, InsertActionFactory.XML_INSERT_COMMENT_ACTION));
        }

        // Scheme
	    for (String suffix : SCHEME_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SCHEME_PREFIX, false, ParsedFile.DEFAULT_INSERT_ACTION));
	    }

        // Shell
	    for (String suffix : SHELL_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SHELL_PREFIX, false, InsertActionFactory.SHELL_INSERT_COMMENT_ACTION));
	    }

        for (String suffix : MAKEFILE_NAMES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SHELL_PREFIX, false, InsertActionFactory.SHELL_INSERT_COMMENT_ACTION));
	    }

        for (String suffix : SHELL_SCRIPT_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SHELL_PREFIX, true, InsertActionFactory.SHELL_INSERT_COMMENT_ACTION));
	    }
	    
	    // Binary
	    for (String suffix : BINARY_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, new FileSuffixRecognizer(suffix, new FileParser.BinaryFileParser()));
	    }

	    for (String suffix : IGNORE_FILE_NAMES) {
		    recognizer.addRecognizer(suffix, new FileSuffixRecognizer(suffix, new FileParser.BinaryFileParser()));
	    }

        recognizer.addRecognizer(createShellContentRecognizer());
        return recognizer;
    }

    FileSuffixRecognizer createRecognizer(String suffix, String start, String end,
                                          String prefix, boolean commentAfterFirstBlock,
                                          ParsedFile.InsertCommentAction insertCommentAction) {
        return new FileSuffixRecognizer(suffix,
                new FileParser.BlockCommentFileParser(start, end, prefix,commentAfterFirstBlock,insertCommentAction));
    }

    FileSuffixRecognizer createRecognizer(String suffix, String prefix,
                                          boolean commentAfterFirstBlock,
                                          ParsedFile.InsertCommentAction insertCommentAction) {
        return new FileSuffixRecognizer(suffix,
                new FileParser.LineCommentFileParser(prefix,commentAfterFirstBlock,insertCommentAction));
    }

    FileContentRecognizer createShellContentRecognizer() {
        return new FileContentRecognizer() {

            public FileParser getParser(FileWrapper file) {
                if (isShellFile(file)) {
                    return new FileParser.LineCommentFileParser(SHELL_PREFIX,true, InsertActionFactory.SHELL_INSERT_COMMENT_ACTION);
                }
                return null;
            }

            private boolean isShellFile(FileWrapper file) {
                try {
                    // see if this is a shell script
                    file.open(FileWrapper.OpenMode.READ);
                    final String str = file.readLine();
                    if ((str != null) && str.startsWith("#!")) {
                        return true;
                    }

                } catch (IOException exc) {
                    // action is still null
                    System.out.println("Could not read file " + file + " to check for shell script");
                } finally {
                    file.close();
                }
                return false;
            }            
        };

    }


}
