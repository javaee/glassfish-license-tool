package org.jvnet.licensetool;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a parsed file.
 */
public class ParsedFile {
    FileWrapper originalFile;
    List<Block> fileBlocks = new ArrayList<Block>();
    boolean commentAfterFirstBlock;
    public ParsedFile(FileWrapper fw, List blocks, boolean commentAfterFirstBlock) {
        originalFile = fw;
        fileBlocks = blocks;
        this.commentAfterFirstBlock = commentAfterFirstBlock;
    }

    public List<Block> getFileBlocks() {
        return fileBlocks;
    }

    public FileWrapper getOriginalFile() {
        return originalFile;
    }

    public boolean commentAfterFirstBlock() {
        return commentAfterFirstBlock;
    }
}
