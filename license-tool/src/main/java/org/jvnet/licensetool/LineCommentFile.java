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
import org.jvnet.licensetool.generic.BinaryFunction;
import static org.jvnet.licensetool.Tags.COMMENT_BLOCK_TAG;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * @author Rama Pulavarthi
 */
public class LineCommentFile {
    public static class LineCommentBlock extends CommentBlock {
            String prefix;

            public LineCommentBlock(String prefix, final List<String> data) {
                this.prefix = prefix;
                parse(data);
            }
            /*
            public LineComment(String prefix, final Block dataBlock) {
                super(dataBlock);
                this.prefix = prefix;
                parse(dataBlock.contents());
            }
            */
            public static CommentBlock createCommentBlock(String prefix, final List<String> commentText) {
                final List<String> commentTextBlock = new ArrayList<String>();
                for (String str : commentText) {
                    commentTextBlock.add( prefix + str ) ;
                }
                return new LineCommentBlock(prefix, commentTextBlock);
            }

            public Block replace(PlainBlock block) {
                commentLines.clear();
                for (String str : block.contents()) {
                    commentLines.add(new Pair<String, String>(prefix, str));
                }
                return this;
            }

            private void parse(List<String> data) {
                for (String str : data) {
                    String commmentPrefix = str.substring(0, str.indexOf(prefix) + prefix.length());
                    String commentSuffix = str.substring(str.indexOf(prefix) + prefix.length());
                    commentLines.add(new Pair<String, String>(commmentPrefix, commentSuffix));
                }
            }

        }
    public static class LineCommentFileParser extends FileParser {
            protected String prefix;

            public LineCommentFileParser(String prefix) {
                this.prefix = prefix;
            }

            public class LineCommentParsedFile extends ParsedFile {
                protected List<Block> fileBlocks = null;
                protected LineCommentParsedFile(FileWrapper originalFile, FileParser parser) throws IOException {
                    super(originalFile);
                    fileBlocks = new ArrayList(parseBlocks(originalFile));
                }

                public List<Block> getFileBlocks() {
                    List<Block> blocks = new ArrayList<Block>();
                    for (Block b : fileBlocks) {
                        blocks.add(b);
                    }
                    return blocks;
                }
                public boolean insertCommentBlock(List<String> commentText) {
                    CommentBlock cb  = createCommentBlock(commentText);
                    fileBlocks.add(0,cb);
                    return true;
                }

                public boolean remove(Block cb) {
                    //TODO  take care of comments which have non-comment text before the comment.
                    return fileBlocks.remove(cb);
                }

                protected CommentBlock createCommentBlock(List<String> commentText) {
                    return LineCommentBlock.createCommentBlock(prefix, commentText);
                }
            }

            @Override
            public ParsedFile parseFile(FileWrapper file) throws IOException {
                return new LineCommentParsedFile(file,this);
            }

            private List<Block> parseBlocks(FileWrapper file) throws IOException{
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
                                          final String prefix) throws IOException {

        boolean inComment = false;
        final List<Block> result = new ArrayList<Block>();
        fw.open(FileWrapper.OpenMode.READ);

        try {
            List<String> data = new ArrayList<String>();

            BinaryFunction<List<String>, String, List<String>> newBlock =
                    new BinaryFunction<List<String>, String, List<String>>() {
                        public List<String> evaluate(List<String> data, String tag) {
                            if (data.size() == 0)
                                return data;
                            final Block bl;
                            if(tag != null && tag.equals(COMMENT_BLOCK_TAG)) {
                                bl = new LineCommentFile.LineCommentBlock(prefix, data);
                                bl.addTag(tag);
                            } else {
                                bl = new PlainBlock(data);
                            }
                            result.add(bl);
                            return new ArrayList<String>();
                        }
                    };

            String line = fw.readLine();
            while (line != null) {
                if (inComment) {
                    if (!line.startsWith(prefix)) {
                        inComment = false;
                        data = newBlock.evaluate(data, COMMENT_BLOCK_TAG);
                    }
                } else {
                    if (line.startsWith(prefix)) {
                        inComment = true;
                        data = newBlock.evaluate(data, null);
                    }
                }
                data.add(line);

                line = fw.readLine();
            }

            // Create last block!
            Block bl = new PlainBlock(data);
            if (inComment)
                bl.addTag(COMMENT_BLOCK_TAG);
            result.add(bl);

            return result;
        } finally {
            fw.close();
        }
    }


}
