package org.jvnet.licensetool.file;

import java.util.*;
import java.io.IOException;

/**
 * Represents a comment in a file. Comment may be a single line in a file
 * or span across multiple contiguos lines in a file.
 */
public abstract class CommentBlock extends Block {
    public CommentBlock(Set<String> tags) {
        super(tags);
        tags.add(COMMENT_BLOCK_TAG);
    }

    public CommentBlock() {
        tags.add(COMMENT_BLOCK_TAG);
    }
    //private String contents;
    public static final String COMMENT_BLOCK_TAG = "CommentBlock";

    public abstract Block replace(String content);

    /**
     *
     * @return returns the comment block as a list of strings
     */
    public abstract String contents();

    public void write(FileWrapper fw) throws IOException {
        fw.write(contents());
    }

    /**
     *
     * @return the comment content of the CommentBlock
     */
    public abstract String comment();

    /**
     * Return the first string in the block that contains the search string.
     */
    public String find(final String search) {
        if (contents().contains(search))
            return contents();
        return null;
    }
}
