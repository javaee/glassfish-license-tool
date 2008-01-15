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
import org.jvnet.licensetool.generic.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * @author Rama Pulavarthi
 */
public class FileParserFactory {
    public static class ShellLikeFileParser extends
            LineCommentFile.LineCommentFileParser {
        public ShellLikeFileParser(String prefix) {
            super(prefix);
        }

        @Override
        public ParsedFile parseFile(FileWrapper file) throws IOException {
            return new LineCommentParsedFile(file) {
                @Override
                public void insertCommentBlock(String commentText) {
                    CommentBlock cb = createCommentBlock(commentText);
                    cb.addTag(CommentBlock.TOP_COMMENT_BLOCK);
                    Block fBlock = fileBlocks.get(0);
                    if (fBlock instanceof CommentBlock) {
                        LineCommentFile.LineCommentBlock  firstBlock = (LineCommentFile.LineCommentBlock) fBlock;
                        if (firstBlock.contents().startsWith("#!")) {

                            Pair<LineCommentFile.LineCommentBlock,LineCommentFile.LineCommentBlock> splitBlocks =
                                    firstBlock.splitFirst();
                            Block sheBangBlock = splitBlocks.first();
                            Block restBlock = splitBlocks.second();
                            fileBlocks.remove(firstBlock);
                            fileBlocks.add(0,restBlock);
                            fileBlocks.add(0,cb);
                            fileBlocks.add(0,sheBangBlock);
                        } else {
                            fileBlocks.add(0,cb);
                        }

                    } else {
                        fileBlocks.add(0,cb);
                    }
                }

                @Override
                protected void postParse() {
                    if(fileBlocks.get(0) instanceof CommentBlock) {
                        //check if first block is shebang block
                        if(((CommentBlock)fileBlocks.get(0)).contents().trim().startsWith("#!")) {
                            //check id next block is comment block.
                            if((fileBlocks.size() > 1) && fileBlocks.get(1) instanceof CommentBlock) {
                                fileBlocks.get(1).addTag(CommentBlock.TOP_COMMENT_BLOCK);
                            }
                        } else {
                            fileBlocks.get(0).addTag(CommentBlock.TOP_COMMENT_BLOCK);
                        }
                    }
                }
            };
        }

        @Override
        protected List<Block> parseBlocks(FileWrapper fw) throws IOException {
            boolean inComment = false;
            final List<Block> result = new ArrayList<Block>();
            StringBuilder sb = new StringBuilder();
            fw.open(FileWrapper.OpenMode.READ);
            try {
                List<String> fileAsLines = FileWrapper.splitToLines(fw.readAsString());
                int count = 0;
                for (String line : fileAsLines) {
                    if (count == 0) {
                        if (line.startsWith("#!")) {
                            result.add(new LineCommentFile.LineCommentBlock(prefix, line, new HashSet<String>()));
                            count++;
                            continue;
                        }
                    }
                    if (inComment) {
                        if (line.startsWith(prefix)) {
                            //previous line is also comment, so append to block
                            sb.append(line);
                        } else {
                            result.add(new LineCommentFile.LineCommentBlock(prefix, sb.toString(), new HashSet<String>()));
                            sb = new StringBuilder();
                            inComment = false;
                            sb.append(line);
                        }
                    } else {
                        if (line.startsWith(prefix)) {
                            if (sb.length() != 0)
                                result.add(new PlainBlock(sb.toString()));
                            inComment = true;
                            sb = new StringBuilder();
                            sb.append(line);

                        } else {
                            //previous line is also not a comment
                            sb.append(line);
                        }
                    }
                }
                //add the last block
                if (sb.length() != 0) {
                    if (inComment)
                        result.add(new LineCommentFile.LineCommentBlock(prefix, sb.toString(), new HashSet<String>()));
                    else
                        result.add(new PlainBlock(sb.toString()));
                }
                return result;
            } finally {
                fw.close();
            }
        }

    }

    //BinaryFiles have no comment blocks and not parsed.
    // this is just to convince the Tool that the file is
    // recognized and ignored.
    public static class BinaryFileParser extends FileParser {
        public ParsedFile parseFile(FileWrapper file) throws IOException {
            LOGGER.fine("Skipped: " + file);
            return null;
        }
    }

    public static FileParser createJavaFileParser() {
        final String JAVA_COMMENT_START = "/*";
        final String JAVA_COMMENT_PREFIX = " *";
        final String JAVA_COMMENT_END = "*/";
        return new MultiLineCommentFile.MultiLineCommentFileParser(JAVA_COMMENT_START, JAVA_COMMENT_END, JAVA_COMMENT_PREFIX) {
            @Override
            public ParsedFile parseFile(final FileWrapper file) throws IOException {
                return new BlockCommentParsedFile(file) {

                    @Override //Hack to put " " before end prefix "*/"
                    protected CommentBlock createCommentBlock(String commentText) {

                        final List<String> commentTextBlock = new ArrayList<String>();
                        List<String> dataAsLines = FileWrapper.splitToLines(commentText);
                        for (String str : dataAsLines) {
                            commentTextBlock.add(prefix + FileWrapper.covertLineBreak(str, line_separator));
                        }
                        commentTextBlock.add(0, start + line_separator);
                        //Hack to put " " before end prefix "*/"
                        commentTextBlock.add(commentTextBlock.size(), " "+end);

                        return new MultiLineCommentFile.MultiLineCommentBlock(start, end, prefix, commentTextBlock, new HashSet<String>());
                    }
                };
            }
        };
    }

    public static FileParser createXMLFileParser() {
        final String XML_COMMENT_START = "<!--";
        final String XML_COMMENT_PREFIX = "";
        final String XML_COMMENT_END = "-->";

        return new MultiLineCommentFile.MultiLineCommentFileParser(
                XML_COMMENT_START, XML_COMMENT_END, XML_COMMENT_PREFIX) {

            @Override
            public ParsedFile parseFile(final FileWrapper file) throws IOException {
                return new BlockCommentParsedFile(file) {
                    @Override
                    public void insertCommentBlock(String commentText) {
                        CommentBlock cb = createCommentBlock(commentText);
                        cb.addTag(CommentBlock.TOP_COMMENT_BLOCK);
                        Block firstBlock = fileBlocks.get(0);
                        if (firstBlock instanceof CommentBlock) {
                            fileBlocks.add(0,cb);
                            adjustBlockAtIndex(1);
                        } else {
                            PlainBlock plainBlock = (PlainBlock) firstBlock;
                            List<String> lines = FileWrapper.splitToLines(plainBlock.contents());
                            String firstLine = lines.get(0);
                            if (firstLine.trim().startsWith("<?xml")) {
                                if (!firstLine.trim().endsWith("?>")) {
                                    throw new RuntimeException("Needs special handling");
                                }
                                Pair<Block, Block> splitBlocks = plainBlock.splitFirst();
                                Block xmlDeclaration = splitBlocks.first();
                                Block restOfXml = splitBlocks.second();
                                fileBlocks.remove(plainBlock);
                                if(restOfXml != null)
                                    fileBlocks.add(0,restOfXml);
                                fileBlocks.add(0,cb);
                                fileBlocks.add(0,xmlDeclaration);
                                adjustBlockAtIndex(2);
                            } else {
                                fileBlocks.add(0,cb);
                                adjustBlockAtIndex(1);
                            }
                        }                        
                    }
                    @Override
                    protected void postParse() {
                        //TODO TODO
                        Block b = fileBlocks.get(0);
                        if(b instanceof CommentBlock)
                            b.addTag(CommentBlock.TOP_COMMENT_BLOCK);
                        else if(b instanceof PlainBlock) {
                            List<String> content = FileWrapper.splitToLines(((PlainBlock)b).contents());
                            String firstLine =content.get(0);
                            if(firstLine.trim().startsWith("<?xml") && firstLine.trim().endsWith("?>")) {
                                if(content.size() > 1) {
                                    for(int i=1; i<content.size();i++) {
                                        // after first line, there is non-empty content
                                        if(!(content.get(i).trim().equals("")))
                                            return;
                                    }
                                }
                                b = fileBlocks.get(1);
                                if(b instanceof CommentBlock) {
                                    b.addTag(CommentBlock.TOP_COMMENT_BLOCK);
                                }
                            }

                        }
                        super.postParse();
                    }
                };
            }
        };
    }

    private static Logger LOGGER = Logger.getLogger(FileParserFactory.class.getName());

}
