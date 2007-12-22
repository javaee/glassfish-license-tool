package org.jvnet.licensetool;

import org.jvnet.licensetool.generic.BinaryFunction;
import static org.jvnet.licensetool.Tags.*;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * This class parses FileWrappers into (lists of) Blocks.
 */
public class FileParser {
    public List<Block> parseFile(FileWrapper file) throws IOException {
        List<Block> fileAsBlocks = new ArrayList<Block>();
        fileAsBlocks.add(getBlock(file));
        return fileAsBlocks;
    }

    public Block createCommentBlock(Block commentText) {
        final Block result = new Block(commentText);
        result.addTag(COMMENT_BLOCK_TAG);
        return result;

    }

    public static class BlockCommentFileParser extends FileParser {
        String start;
        String end;
        String prefix;

        public BlockCommentFileParser(String start, String end, String prefix) {
            this.start = start;
            this.end = end;
            this.prefix = prefix;
        }

        public List<Block> parseFile(FileWrapper file) throws IOException {
            return parseBlocks(file, start, end);
        }

        public Block createCommentBlock(Block commentText) {
            final Block result = new Block(commentText);
            result.addPrefixToAll(prefix);
            result.addBeforeFirst(start);
            result.addAfterLast(end);
            result.addTag(COMMENT_BLOCK_TAG);
            return result;

        }
    }

    public static class LineCommentFileParser extends FileParser {
        String prefix;

        public LineCommentFileParser(String prefix) {
            this.prefix = prefix;
        }

        public List<Block> parseFile(FileWrapper file) throws IOException {
            return parseBlocks(file, prefix);
        }

        public Block createCommentBlock(Block commentText) {
            final Block result = new Block(commentText);
            result.addPrefixToAll(prefix);
            result.addTag(COMMENT_BLOCK_TAG);
            return result;
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

            return new Block(data);
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

                            final Block bl = new Block(data);
                            if (tag != null)
                                bl.addTag(tag);
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
            Block bl = new Block(data);
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
                                          final String start, final String end) throws IOException {

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

                            final Block bl = new Block(data);
                            if (tag != null)
                                bl.addTag(tag);
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
            Block bl = new Block(data);
            if (inComment)
                bl.addTag(COMMENT_BLOCK_TAG);
            result.add(bl);

            return result;
        } finally {
            fw.close();
        }
    }

}
