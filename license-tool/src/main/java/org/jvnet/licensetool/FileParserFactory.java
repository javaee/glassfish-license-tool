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

import org.jvnet.licensetool.file.*;
import static org.jvnet.licensetool.Tags.COMMENT_BLOCK_TAG;
import org.jvnet.licensetool.generic.Pair;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rama Pulavarthi
 */
public class FileParserFactory {
    

    public static class XMLFileParser extends MultiLineCommentFile.MultiLineCommentFileParser {
        public XMLFileParser(String start, String end, String prefix) {
            super(start, end, prefix);
        }

        @Override
        public ParsedFile parseFile(final FileWrapper file) throws IOException {
            return new BlockCommentParsedFile(file, this) {
                @Override
                public boolean insertCommentBlock(List<String> commentText) {
                    CommentBlock cb  = createCommentBlock(commentText);
                    Block firstBlock = fileBlocks.getFirst();
                    if (firstBlock.hasTag(COMMENT_BLOCK_TAG)) {
                        fileBlocks.addFirst(cb);
                    } else {
                        PlainBlock plainBlock = (PlainBlock) firstBlock;
                        List<String> contents = plainBlock.contents();
                        String firstLine = contents.get(0);
                        if (firstLine.trim().startsWith("<?xml")) {
                            if (!firstLine.trim().endsWith("?>")) {
                                throw new RuntimeException("Needs special handling");
                            }
                            Pair<Block, Block> splitBlocks = plainBlock.splitFirst();
                            Block xmlDeclaration = splitBlocks.first();
                            Block restOfXml = splitBlocks.second();
                            this.remove(plainBlock);
                            fileBlocks.addFirst(restOfXml);
                            fileBlocks.addFirst(cb);
                            fileBlocks.addFirst(xmlDeclaration);
                        } else {
                            fileBlocks.addFirst(cb);
                        }
                    }
                    return true;
                }
            };
        }
    }

    public static class ShellLikeFileParser extends
            LineCommentFile.LineCommentFileParser {
        public ShellLikeFileParser(String prefix) {
            super(prefix);
        }

        @Override
        public ParsedFile parseFile(FileWrapper file) throws IOException {
            return new LineCommentParsedFile(file, this) {
                @Override
                public boolean insertCommentBlock(List<String> commentText) {
                    CommentBlock cb  = createCommentBlock(commentText);
                    Block fBlock = fileBlocks.getFirst();
                    if (fBlock.hasTag(COMMENT_BLOCK_TAG)) {
                        CommentBlock firstBlock = (CommentBlock)fBlock;
                        List<String> contents = firstBlock.contents();
                        String firstLine = contents.get(0);

                        if (firstLine.trim().startsWith("#!")) {
                            List<String> blContents = firstBlock.contents();
                            List<String> first = new ArrayList<String>();
                            List<String> rest = new ArrayList<String>();
                            for(String str: blContents) {
                                if (first.size() == 0) {
                                    first.add(str);
                                } else {
                                    rest.add(str);
                                }
                            }

                            Block sheBangBlock = new LineCommentFile.LineCommentBlock(prefix,first);
                            Block restBlock = new LineCommentFile.LineCommentBlock(prefix,rest);
                            firstBlock.replace(new PlainBlock(new ArrayList<String>()));
                            fileBlocks.addFirst(restBlock);
                            fileBlocks.addFirst(cb);
                            fileBlocks.addFirst(sheBangBlock);
                        } else {
                            fileBlocks.addFirst(cb);
                        }

                    } else {
                        fileBlocks.addFirst(cb);
                    }
                    return true;
                }
            };
        }
    }


    //BinaryFiles have no comment blocks and not parsed.
    // this is just to convince the Tool that the file is
    // recognized and ignored.
    public static class BinaryFileParser extends FileParser {
        public ParsedFile parseFile(FileWrapper file) throws IOException {
           System.out.println("Skipped: " + file);
           return null;
        }
    }

    public static class JavaFileParser extends MultiLineCommentFile.MultiLineCommentFileParser {
        public JavaFileParser(String start, String end, String prefix) {
            super(start, end, prefix);
        }

        @Override
        public ParsedFile parseFile(final FileWrapper file) throws IOException {
            return new BlockCommentParsedFile(file, this);
        }
    }



}
