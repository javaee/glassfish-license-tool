package org.jvnet.licensetool.file;

import java.util.*;
import java.io.IOException;

/**
 * Represents a comment in a file. Comment may be a single line in a file
 * or span across multiple contiguos lines in a file.
 */
public abstract class CommentBlock extends Block {
    // Marker tag to idenitfy as a COMMENTBLOCK
    public static final String COMMENT_BLOCK_TAG = "CommentBlock";

    // Marker to indicate that the commentblock is in a position where a first CommentBlock can appear in a File.
    // It depends on the file type and its contents and this tag should be added by the corresponding FileParser.
    // for ex:
    // In a java file,
    // CommentBlock can be in the beginning and a CommentBlock in the beginning of the file gets the Tag.
    //
    // In a XMl file,
    // the first CommentBlock should occur after XML declaration (<?xml version="1.0"?>) if it exists,
    // otherwise in the beginning of the file.  
    public static final String TOP_COMMENT_BLOCK = "TopCommentBloack";

    public CommentBlock(Set<String> tags) {
        super(tags);
        tags.add(COMMENT_BLOCK_TAG);
    }

    public CommentBlock() {
        tags.add(COMMENT_BLOCK_TAG);
    }

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
