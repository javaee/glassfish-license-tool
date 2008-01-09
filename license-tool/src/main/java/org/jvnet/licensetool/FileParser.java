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

import org.jvnet.licensetool.generic.BinaryFunction;
import org.jvnet.licensetool.generic.Pair;
import static org.jvnet.licensetool.Tags.*;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.IOException;

/**
 * This class parses FileWrappers into (lists of) Blocks.
 */
public abstract class FileParser {

    public abstract ParsedFile parseFile(FileWrapper file) throws IOException;

    public static class BlockCommentFileParser extends FileParser {
        final String start;
        final String end;
        final String prefix;
        public BlockCommentFileParser(String start, String end, String prefix) {
            this.start = start;
            this.end = end;
            this.prefix = prefix;
        }

        public class BlockCommentParsedFile extends ParsedFile {
            LinkedList<Block> fileBlocks = null;

            protected BlockCommentParsedFile(FileWrapper originalFile, FileParser parser) throws IOException {
                super(originalFile);
                fileBlocks = new LinkedList(parseBlocks(originalFile));
            }

            public List<Block> getFileBlocks() {
                List<Block> blocks = new ArrayList<Block>();
                for (Block b : fileBlocks) {
                    blocks.add(b);
                }
                return blocks;
            }

            public boolean insertCommentBlock(List<String> commentText) {
                CommentBlock cb = createCommentBlock(commentText);
                fileBlocks.addFirst(cb);
                return true;
            }

            public boolean remove(Block cb) {
                //TODO  take care of comments which have non-comment text before and after the comment.
                return fileBlocks.remove(cb); 
            }

            protected CommentBlock createCommentBlock(List<String> commentText) {
                return CommentBlock.BlockComment.createCommentBlock(start, end, prefix, commentText);
            }
        }

        @Override
        public ParsedFile parseFile(final FileWrapper file) throws IOException {
            return new BlockCommentParsedFile(file,this);

        }

        private List<Block> parseBlocks(FileWrapper file) throws IOException{
            return parseBlocks(file, start, end, prefix);
        }
    }

    public static class LineCommentFileParser extends FileParser {
        String prefix;

        public LineCommentFileParser(String prefix) {
            this.prefix = prefix;
        }

        public class LineCommentParsedFile extends ParsedFile {
            LinkedList<Block> fileBlocks = null;
            protected LineCommentParsedFile(FileWrapper originalFile, FileParser parser) throws IOException {
                super(originalFile);
                fileBlocks = new LinkedList(parseBlocks(originalFile));
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
                fileBlocks.addFirst(cb);
                return true;
            }

            public boolean remove(Block cb) {
                //TODO  take care of comments which have non-comment text before the comment.
                return fileBlocks.remove(cb);
            }

            protected CommentBlock createCommentBlock(List<String> commentText) {
                return CommentBlock.LineComment.createCommentBlock(prefix, commentText);
            }
        }
        
        @Override
        public ParsedFile parseFile(FileWrapper file) throws IOException {
            return new LineCommentParsedFile(file,this);
        }

        private List<Block> parseBlocks(FileWrapper file) throws IOException{
            return parseBlocks(file, prefix);
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

    public static class JavaFileParser extends FileParser.BlockCommentFileParser {
        public JavaFileParser(String start, String end, String prefix) {
            super(start, end, prefix);
        }

        @Override
        public ParsedFile parseFile(final FileWrapper file) throws IOException {
            return new BlockCommentParsedFile(file, this);
        }
    }

    public static class XMLFileParser extends FileParser.BlockCommentFileParser{
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
                        List<String> contents = firstBlock.contents();
                        String firstLine = contents.get(0);
                        if (firstLine.trim().startsWith("<?xml")) {
                            if (!firstLine.trim().endsWith("?>")) {
                                throw new RuntimeException("Needs special handling");
                            }
                            Pair<Block, Block> splitBlocks = firstBlock.splitFirst();
                            Block xmlDeclaration = splitBlocks.first();
                            Block restOfXml = splitBlocks.second();
                            this.remove(firstBlock);
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

    public static class ShellLikeFileParser extends FileParser.LineCommentFileParser {
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
                            Pair<Block, Block> splitBlocks = firstBlock.splitFirst();
                            Block sheBangBlock = splitBlocks.first();
                            Block rest = splitBlocks.second();
                            firstBlock.replace(new Block.PlainBlock(new ArrayList<String>()));
                            fileBlocks.addFirst(rest);
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

    /**
     * Return the contents of the text file as a Block.
     */
    public static Block getBlock(final FileWrapper fw) throws IOException {
        fw.open(FileWrapper.OpenMode.READ);

        try {
            final List<String> data = new ArrayList<String>();

            String line = fw.readLine();
            while (line != null) {
                data.add(line);
                line = fw.readLine();
            }

            return new Block.PlainBlock(data);
        } finally {
            fw.close();
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
                                bl = new CommentBlock.LineComment(prefix, data);
                                bl.addTag(tag);
                            } else {
                                bl = new Block.PlainBlock(data);
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
            Block bl = new Block.PlainBlock(data);
            if (inComment)
                bl.addTag(COMMENT_BLOCK_TAG);
            result.add(bl);

            return result;
        } finally {
            fw.close();
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
                                bl = new CommentBlock.BlockComment(start, end, prefix, data);
                                bl.addTag(tag);
                            } else {
                                bl = new Block.PlainBlock(data);
                            }
                            result.add(bl);
                            return new ArrayList<String>();
                        }
                    };

            String line = fw.readLine();
            while (line != null) {
                if (inComment) {
                    data.add(line);

                    if (line.contains(end)) {
                        inComment = false;
                        data = newBlock.evaluate(data, COMMENT_BLOCK_TAG);
                    }
                } else {
                    if (line.contains(start)) {
                        inComment = true;
                        data = newBlock.evaluate(data, null);
                    }

                    data.add(line);

                    if (line.contains(end)) {
                        // Comment was a single line!
                        inComment = false;
                        data = newBlock.evaluate(data, COMMENT_BLOCK_TAG);
                    }
                }
                line = fw.readLine();
            }

            // Create last block!
            Block bl = new Block.PlainBlock(data);
            if (inComment)
                bl.addTag(COMMENT_BLOCK_TAG);
            result.add(bl);

            return result;
        } finally {
           fw.close();
        }
    }

}
