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
import org.jvnet.licensetool.util.ToolUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * @author Rama Pulavarthi
 */
public class LineCommentFile {
    public static class LineCommentBlock extends CommentBlock {
        protected final List<Pair<String, String>> commentLines = new ArrayList<Pair<String, String>>();        
        final String prefix;

        public LineCommentBlock(String prefix, List<String> lineComment, Set<String> tags) {
            super(tags);
            this.prefix = prefix;
            parse(lineComment);
        }

        public LineCommentBlock(String prefix, String lineComment, Set<String> tags) {
            super(tags);
            this.prefix = prefix;
            parse(lineComment);
        }

        public static CommentBlock createCommentBlock(String prefix, final String commentText, String line_separator) {
            final List<String> commentTextBlock = new ArrayList<String>();
            List<String> dataAslines = ToolUtil.splitToLines(commentText);
            if (!dataAslines.get(0).trim().equals(""))
                commentTextBlock.add(prefix + line_separator);
            for (String str : dataAslines) {
                commentTextBlock.add(prefix + ToolUtil.covertLineBreak(str, line_separator));
            }
            if (!dataAslines.get(dataAslines.size() - 1).trim().equals(""))
                commentTextBlock.add(prefix + line_separator);
            return new LineCommentBlock(prefix, commentTextBlock, new HashSet<String>());
        }


        public Block replace(String content) {
            commentLines.clear();
            List<String> dataAslines = ToolUtil.splitToLines(content);
            for (String str : dataAslines) {
                commentLines.add(new Pair<String, String>(prefix, str));
            }
            return this;
        }

        public String contents() {
            StringBuilder sb = new StringBuilder();
            for(Pair<String,String> line: commentLines) {
                sb.append(line.first());
                sb.append(line.second());
            }
            return sb.toString();
        }

        public String comment() {
            StringBuilder sb = new StringBuilder();
            for(Pair<String,String> line: commentLines) {
                sb.append(line.second());
            }
            return sb.toString().trim();
        }

        private void parse(String data) {
            List<String> dataAsLines = ToolUtil.splitToLines(data);
            parse(dataAsLines);
        }

        private void parse(List<String> data) {
            for (String str : data) {
                String commmentPrefix = str.substring(0, str.indexOf(prefix) + prefix.length());
                String commentSuffix = str.substring(str.indexOf(prefix) + prefix.length());
                commentLines.add(new Pair<String, String>(commmentPrefix, commentSuffix));
            }
        }

        /**
         * Split block into two blocks, with only the
         * first line of the original Block in result.first().
         */
        public Pair<LineCommentBlock, LineCommentBlock> splitFirst() {
            List<String> fdata = new ArrayList<String>();
            List<String> rdata = new ArrayList<String>();
            boolean first = true;
            for (Pair<String, String> pair : commentLines) {
                if (first) {
                    fdata.add(pair.first() + pair.second());
                    first= false;
                } else {
                    fdata.add(pair.first() + pair.second());
                }    
            }

            return new Pair<LineCommentBlock, LineCommentBlock>(
                    new LineCommentBlock(prefix, fdata, tags), new LineCommentBlock(prefix, rdata, tags));

        }
    }

    public static class LineCommentFileParser extends FileParser {
        protected final String prefix;

        public LineCommentFileParser(String prefix) {
            this.prefix = prefix;
        }

        public class LineCommentParsedFile extends ParsedFile {
            protected List<Block> fileBlocks = null;
            final protected String line_separator;

            /**
             * calls postParse() after the file is parsed in to blocks.
             * @param originalFile
             * @throws IOException
             */
            protected LineCommentParsedFile(FileWrapper originalFile) throws IOException {
                super(originalFile);
                fileBlocks = new ArrayList(parseBlocks(originalFile));
                postParse();
                line_separator = sniffLineSeparator();
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
            protected String sniffLineSeparator(){
                String separator = null;
                String blockContent;
                for(Block b: fileBlocks){
                    if(b instanceof PlainBlock) {
                        blockContent = ((PlainBlock)b).contents();
                    } else {
                        blockContent = ((CommentBlock)b).contents();
                    }
                    separator = ToolUtil.sniffLineSeparator(blockContent);
                    if(separator != null)
                        break;
                }
                if(separator == null) {
                    separator = System.getProperty("line.separator");
                }
                return separator;
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
                adjustBlockAtIndex(1);
            }

            protected void adjustBlockAtIndex(int i) {
                Block b = fileBlocks.get(i);
                if(b instanceof PlainBlock) {
                    String content = ((PlainBlock)b).contents();
                    if(content.startsWith(line_separator)) {
                       return;
                    }
                }
                fileBlocks.add(i, new PlainBlock(line_separator));
            }

            public void remove(CommentBlock cb) {
                //TODO  take care of comments which have non-comment text before the comment.
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
                return LineCommentBlock.createCommentBlock(prefix, commentText, line_separator);
            }
        }

        @Override
        public ParsedFile parseFile(FileWrapper file) throws IOException {
            return new LineCommentParsedFile(file);
        }

        protected List<Block> parseBlocks(FileWrapper file) throws IOException {
            return LineCommentFile.parseBlocks(file, prefix);
        }
    }


    /**
     * Transform fw into a list of blocks.  There are two types of blocks in this
     * list, and they always alternate:
     * <ul>
     * <li>Blocks in which every line starts with prefix,
     * Such blocks are given the tag COMMENT_BLOCK_TAG.
     * <li>Blocks in which no line starts with prefix.
     * Such blocks are not tagged.
     * <ul>
     */
    public static List<Block> parseBlocks(final FileWrapper fw,
                                          final String... prefixes) throws IOException {

        boolean inComment = false;
        final List<Block> result = new ArrayList<Block>();
        StringBuilder sb = new StringBuilder();
        fw.open(FileWrapper.OpenMode.READ);
        try {
            List<String> fileAsLines = ToolUtil.splitToLines(fw.readAsString());

            for (String line : fileAsLines) {
                if (inComment) {
                    if (startsWith(line, prefixes)) {
                        //previous line is also comment, so append to block
                        sb.append(line);
                    } else {
                        result.add(new LineCommentBlock(prefixes[0], sb.toString(), new HashSet<String>()));
                        sb = new StringBuilder();
                        inComment = false;
                        sb.append(line);
                    }
                } else {
                    if (startsWith(line, prefixes)) {
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
                    result.add(new LineCommentBlock(prefixes[0], sb.toString(), new HashSet<String>()));
                else
                    result.add(new PlainBlock(sb.toString()));
            }
            return result;
        } finally {
            fw.close();
        }
    }

    private static boolean startsWith(String str, String[] prefixes) {
        for (String prefix : prefixes) {
            if (str.startsWith(prefix))
                return true;
        }
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(LineCommentFile.class.getName());
}
