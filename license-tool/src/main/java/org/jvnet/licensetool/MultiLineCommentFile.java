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
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * @author Rama Pulavarthi
 */
public class MultiLineCommentFile {
    public static class MultiLineCommentBlock extends CommentBlock {
        protected Pair<String, String> commentStart = null;
        protected List<Pair<String, String>> commentLines = new ArrayList<Pair<String, String>>();
        protected Pair<String, String> commentEnd = null;

        final String prefix;
        final String start;
        final String end;

        public MultiLineCommentBlock(String start, String end, String prefix, final String multiLineComment, Set<String> tags) {
            super(tags);
            this.start = start;
            this.end = end;
            this.prefix = prefix;
            parse(multiLineComment);
        }

        public MultiLineCommentBlock(String start, String end, String prefix, List<String> multiLineComment, Set<String> tags) {
            super(tags);
            this.start = start;
            this.end = end;
            this.prefix = prefix;
            parse(multiLineComment);
        }

        public static CommentBlock createCommentBlock(String start, String end, String prefix,
                                                      final String commentText, final String line_separator) {
            final List<String> commentTextBlock = new ArrayList<String>();
            List<String> dataAsLines = FileWrapper.splitToLines(commentText);
            for (String str : dataAsLines) {
                commentTextBlock.add(prefix + str);
            }
            commentTextBlock.add(0, start + line_separator);
            commentTextBlock.add(commentTextBlock.size(), end + line_separator);
            return  new MultiLineCommentBlock(start, end, prefix, commentTextBlock, new HashSet<String>());
        }

        public Block replace(String content) {
            commentStart = new Pair<String, String>(commentStart.first(), "");
            commentLines.clear();
            List<String> parsedData = FileWrapper.splitToLines(content);
            for (String str : parsedData) {
                commentLines.add(new Pair<String, String>(prefix, str));
            }
            commentEnd = new Pair<String, String>("", commentEnd.second());
            return this;
        }

        public String contents() {
            StringBuilder sb = new StringBuilder();
            sb.append(commentStart.first());
            sb.append(commentStart.second());
            for(Pair<String,String> line: commentLines) {
                sb.append(line.first());
                sb.append(line.second());
            }
            sb.append(commentEnd.first());
            sb.append(commentEnd.second());
            return sb.toString();
        }

        public String comment() {
            StringBuilder sb = new StringBuilder();
            if(!commentStart.second().trim().equals(""))
                sb.append(commentStart.second());
            for(Pair<String,String> line: commentLines) {
                sb.append(line.second());
            }
            if(!commentEnd.first().trim().equals(""))
                sb.append(commentEnd.first());            
            return sb.toString();
        }

        private void parse(List<String> data) {
            for (int i = 0; i < data.size(); i++) {
                String str = data.get(i);
                if (i == 0) {
                    int index = str.indexOf(start);
                    if (index < 0) {
//                        String startCommmentPrefix = start;
//                        String startCommentSuffix = str;
//                        commentStart = new Pair<String, String>(startCommmentPrefix, startCommentSuffix);
                        throw new RuntimeException("Cooment block does n't caontain start marker " + start);

                    } else {
                        String startCommmentPrefix = str.substring(0, index + start.length());
                        String startCommentSuffix;
                        String rest = str.substring(index + start.length());
                        int endindex = rest.indexOf(end);
                        if (endindex >= 0) {
                            startCommentSuffix = rest.substring(0, endindex);
                            commentEnd = new Pair<String, String>("", rest.substring(endindex));
                        } else {
                            startCommentSuffix = rest;
                        }
                        commentStart = new Pair<String, String>(startCommmentPrefix, startCommentSuffix);
                    }
                } else if (i == (data.size() - 1)) {
                    int index = str.indexOf(end);
                    if (index < 0) {
//                        String endCommmentPrefix = str;
//                        String endCommentSuffix = end;
//                        commentEnd = new Pair<String,String>(endCommmentPrefix,endCommentSuffix);
                        throw new RuntimeException("Cooment block does n't contain end marker " + end);
                    } else {
                        commentEnd = new Pair<String, String>(str.substring(0, index),
                                str.substring(index));
                    }
                } else {
                    int index = str.indexOf(prefix);
                    if (index < 0) {
                        commentLines.add(new Pair<String, String>("", str));

                    } else {
                        commentLines.add(new Pair<String, String>(str.substring(0, index + prefix.length()),
                                str.substring(index + prefix.length())));
                    }
                }
            }
        }

        private void parse(String data) {
            List<String> dataAsLines = FileWrapper.splitToLines(data);
            parse(dataAsLines);
        }
    }

    public static class MultiLineCommentFileParser extends FileParser {
        final String start;
        final String end;
        final String prefix;

        public MultiLineCommentFileParser(String start, String end, String prefix) {
            this.start = start;
            this.end = end;
            this.prefix = prefix;
        }

        public class BlockCommentParsedFile extends ParsedFile {
            protected List<Block> fileBlocks = null;
            protected String line_separator;
            /**
             * calls postParse() after the file is parsed in to blocks.
             * @param originalFile
             * @throws IOException
             */
            protected BlockCommentParsedFile(FileWrapper originalFile) throws IOException {
                super(originalFile);
                fileBlocks = new ArrayList(parseBlocks(originalFile));
                postParse();
                sniffLineSeparator();
            }

            /**
             * Sub classes should override based on the first possible
             * position of the CommentBlock in the file.
             */
            protected void postParse() {
                if (fileBlocks.get(0) instanceof CommentBlock) {
                    fileBlocks.get(0).addTag(CommentBlock.TOP_COMMENT_BLOCK);
                }
            }
            protected void sniffLineSeparator(){
                String blockContent;
                for(Block b: fileBlocks){
                    if(b instanceof PlainBlock) {
                        blockContent = ((PlainBlock)b).contents();
                    } else {
                        blockContent = ((CommentBlock)b).contents();
                    }
                    line_separator = FileWrapper.sniffLineSeparator(blockContent);
                    if(line_separator != null)
                        break;
                }
                if(line_separator == null) {
                    line_separator = System.getProperty("line.separator");
                }
            }
            public List<CommentBlock> getComments() {
                List<CommentBlock> blocks = new ArrayList<CommentBlock>();
                for (Block b : fileBlocks) {
                    if(b instanceof CommentBlock)
                        blocks.add((CommentBlock) b);
                }
                return blocks;
            }

            public void insertCommentBlock(String commentText) {
                CommentBlock cb = createCommentBlock(commentText);
                cb.addTag(CommentBlock.TOP_COMMENT_BLOCK);
                fileBlocks.add(0, cb);                
            }

            public void remove(CommentBlock cb) {
                fileBlocks.remove(cb);
            }

            public void writeTo(FileWrapper fw) throws IOException {
                   try {
                    if (fw.canWrite()) {
                        // TODO this is dangerous: a crash before close will destroy the file!
                        fw.delete();
                        fw.open(FileWrapper.OpenMode.WRITE);
                        for (Block block : fileBlocks) {
                            block.write(fw);
                        }

                    } else {
                        LOGGER.info("Skipping file " + fw + " because is is not writable");
                    }
                } finally {
                    fw.close();
                }
            }

            protected CommentBlock createCommentBlock(String commentText) {
                return MultiLineCommentBlock.createCommentBlock(start, end, prefix, commentText, line_separator);
            }

        }

        @Override
        public ParsedFile parseFile(final FileWrapper file) throws IOException {
            return new BlockCommentParsedFile(file);

        }

        private List<Block> parseBlocks(FileWrapper file) throws IOException {
            return MultiLineCommentFile.parseBlocks(file, start, end, prefix);
        }
    }

    /**
     * Transform fw into a list of blocks.  There are two types of blocks in this
     * list, and they always alternate:
     * <ul>
     * <li>Blocks that start with a String containing start,
     * and end with a String containing end.  Such blocks are given the
     * tag COMMENT_BLOCK_TAG.
     * <li>Blocks that do not contain start or end anywhere
     * <ul>
     */
    public static List<Block> parseBlocks(final FileWrapper fw,
                                          final String start, final String end, final String prefix) throws IOException {

        fw.open(FileWrapper.OpenMode.READ);

        try {
            String fileContents = fw.readAsString();

            int commentStart;
            int commentEnd;
            int curIndex = 0;
            List<Block> parsedBlocks = new ArrayList<Block>();
            String commentString;
            String plainString;

            while (true) {
                commentStart = fileContents.indexOf(start, curIndex);
                if (commentStart != -1) {
                    if (commentStart != curIndex) {
                        //capture until the start of the comment
                        plainString = fileContents.substring(curIndex, commentStart);
                        parsedBlocks.add(new PlainBlock(plainString));
                    }
                    curIndex = commentStart;
                    commentEnd = fileContents.indexOf(end, commentStart + start.length());
                    if (commentEnd != -1) {
                        commentString = fileContents.substring(commentStart, commentEnd + end.length());
                        parsedBlocks.add(new MultiLineCommentBlock(start, end, prefix, commentString, new HashSet<String>()));
                        curIndex = commentEnd + end.length();
                    } else {
                        // no end comment, though unusual
                        plainString = fileContents.substring(curIndex);
                        parsedBlocks.add(new PlainBlock(plainString));
                        break;
                    }
                } else if (curIndex == fileContents.length()) {
                    //reached end of file;
                    break;
                } else {
                    //no comment further
                    plainString = fileContents.substring(curIndex);
                    parsedBlocks.add(new PlainBlock(plainString));
                    break;
                }
            }
            return parsedBlocks;
        } finally {
            fw.close();
        }

    }

    private static final Logger LOGGER = Logger.getLogger(LineCommentFile.class.getName());
}
