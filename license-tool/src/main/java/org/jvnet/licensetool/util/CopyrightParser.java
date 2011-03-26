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

package org.jvnet.licensetool.util;

import org.jvnet.licensetool.file.CommentBlock;
import org.jvnet.licensetool.file.ParsedFile;

import static org.jvnet.licensetool.Tags.*;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rama Pulavarthi
 */
public class CopyrightParser {

    private static Pattern copyright_pattern =
            Pattern.compile("(\\b[Cc]opyright[,]?\\b|\\([Cc]\\))", Pattern.MULTILINE);

    private static Pattern copyright_year_pattern =
            Pattern.compile("[Cc]opyright[,]? (?:\\([Cc]\\) )?([-0-9, ]+) [A-Z]",Pattern.MULTILINE);

    //private static Pattern year_pattern = Pattern.compile("([0-9]{4})((, |-)([0-9]{4}))?(,)?");
    private static Pattern year_pattern =
            Pattern.compile("([0-9]*)(?:, |-)*([0-9]*)(?:[, ])?");

    private static final Pattern reservedRights =
            Pattern.compile("All rights reserved[.]?", Pattern.CASE_INSENSITIVE);

    private static final String copyright_tag = "Copyright";
    
    public static void parseCopyright(CommentBlock commentBlock, ParsedFile pfile) {
        for (String line : ToolUtil.splitToLines(commentBlock.comment())) {
            Matcher cp1 = copyright_pattern.matcher(line);
            if (cp1.find()) {
                commentBlock.addTag(copyright_tag);
                commentBlock.addTag(COPYRIGHT_BLOCK_TAG);
                Matcher m = copyright_year_pattern.matcher(line);
                if (m.find()) {
                    CommentBlock.Copyright copyright = new CommentBlock.Copyright();
                    String year = m.group(1);
                    Matcher year_matcher = year_pattern.matcher(year);
                    if (year_matcher.matches()) {
                        String startYear = year_matcher.group(1);
                        copyright.setStartYear(startYear);
                        String endYear = year_matcher.group(2);
                        if (endYear != null) {
                            copyright.setEndYear(endYear);
                        }
                    } else {
                        trace("Error: Year pattern not recognized in \"" + line + "\" in file:" + pfile.getPath());
                    }

                    String remaining = line.substring(m.end(1));
                    Matcher rightsMatcher = reservedRights.matcher(remaining);
                    String licensor;
                    if (rightsMatcher.find()) {
                        licensor = remaining.substring(0, rightsMatcher.start() - 1);
                    } else {
                        //treat rest of the line as the licensor.
                        licensor = remaining;                                                                
                    }
                    copyright.setLicensor(licensor.trim());
                    commentBlock.setCopyright(copyright);
                    break;
                }                         
            }
        }
    }

    private static void trace(String msg) {
        LOGGER.fine(msg);
    }

    private static final Logger LOGGER = Logger.getLogger(CopyrightParser.class.getName());
}
