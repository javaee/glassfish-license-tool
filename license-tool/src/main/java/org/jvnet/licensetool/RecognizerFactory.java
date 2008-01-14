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
import org.jvnet.licensetool.file.FileParser;
import org.jvnet.licensetool.file.FileRecognizer;
import org.jvnet.licensetool.file.FileWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RecognizerFactory {
    public FileRecognizer getDefaultRecognizer() {
        CompositeRecognizer recognizer = new CompositeRecognizer();
        // Configure the recognizer

        // Java
        for (String suffix : JAVA_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix,
                    new FileSuffixRecognizer(suffix,
                            FileParserFactory.createJavaFileParser()));
        }

        // Java line
	    for (String suffix : JAVA_LINE_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix,
                    new FileSuffixRecognizer(suffix,
                            new LineCommentFile.LineCommentFileParser(JAVA_LINE_PREFIX)));
	    }

        // XML
        for (String suffix : XML_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix,
                    new FileSuffixRecognizer(suffix,
                            FileParserFactory.createXMLFileParser()));
        }

        // Scheme
	    for (String suffix : SCHEME_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, new FileSuffixRecognizer(suffix,
                            new LineCommentFile.LineCommentFileParser(SCHEME_PREFIX)));
	    }

        // Shell
	    for (String suffix : SHELL_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix,new FileSuffixRecognizer(suffix,
                            new LineCommentFile.LineCommentFileParser(SHELL_PREFIX)));
	    }

        for (String suffix : MAKEFILE_NAMES) {
		    recognizer.addRecognizer(suffix, new FileSuffixRecognizer(suffix,
                            new LineCommentFile.LineCommentFileParser(SHELL_PREFIX)));
	    }

        for (String suffix : SHELL_SCRIPT_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, new FileSuffixRecognizer(suffix,
                            new FileParserFactory.ShellLikeFileParser(SHELL_PREFIX)));
	    }
	    
	    // Binary
	    for (String suffix : BINARY_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix,
                    new FileSuffixRecognizer(suffix, new FileParserFactory.BinaryFileParser()));
	    }

	    for (String suffix : IGNORE_FILE_NAMES) {
		    recognizer.addRecognizer(suffix,
                    new FileSuffixRecognizer(suffix, new FileParserFactory.BinaryFileParser()));
	    }

        recognizer.addRecognizer(createShellContentRecognizer());
        return recognizer;
    }

    FileContentRecognizer createShellContentRecognizer() {
        return new FileContentRecognizer() {

            public FileParser getParser(FileWrapper file) {
                if (isShellFile(file)) {
                    return new FileParserFactory.ShellLikeFileParser(SHELL_PREFIX);
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
                    LOGGER.warning("Could not read file " + file + " to check for shell script");
                } finally {
                    file.close();
                }
                return false;
            }            
        };

    }


    /**
 * This class uses the file name suffix to identify the type of file.
     */
    public static class FileSuffixRecognizer implements FileRecognizer {
        final String  suffix;
        final FileParser parser;

        public FileSuffixRecognizer(String suffix, FileParser parser) {
            this.suffix = suffix;
            this.parser = parser;
        }
        public FileParser getParser(FileWrapper file) {
            return parser;
        }
    }

    /**
 * This class marks as a tag and FileContentRecognizer identifies a file based on the contents of the file.
     *
     */
    public static class FileContentRecognizer implements FileRecognizer {

        public FileParser getParser(FileWrapper file) {
            return null;
        }
    }

    public static class CompositeRecognizer implements FileRecognizer{
        final Map<String, FileRecognizer> suffixRecognizers = new HashMap<String, FileRecognizer>();
        final List<FileRecognizer> contentRecognizers = new ArrayList<FileRecognizer>();

        public FileParser getParser(FileWrapper file) {
            FileRecognizer recognizer = suffixRecognizers.get(file.getSuffix());
            if(recognizer != null) {
                return recognizer.getParser(file);
            }

            for (FileRecognizer r : contentRecognizers) {
                FileParser bp = r.getParser(file);
                if (bp != null) {
                    return bp;
                }
            }

            return null;
        }

        public void addRecognizer(FileRecognizer recognizer) {
            contentRecognizers.add(recognizer);
        }

        public void addRecognizer(String suffix, FileRecognizer recognizer) {
            suffixRecognizers.put(suffix, recognizer);
        }
    }

    private static Logger LOGGER = Logger.getLogger(RecognizerFactory.class.getName());

}
