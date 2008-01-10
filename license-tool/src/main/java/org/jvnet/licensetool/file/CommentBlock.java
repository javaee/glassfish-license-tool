package org.jvnet.licensetool.file;

import org.jvnet.licensetool.generic.Pair;
import org.jvnet.licensetool.file.FileWrapper;

import java.util.*;
import java.io.IOException;

/**
 * Marker for CommentBlock
 */
public abstract class CommentBlock extends Block {
    protected Pair<String, String> commentStart = null;
    protected List<Pair<String, String>> commentLines = new ArrayList<Pair<String, String>>();
    protected Pair<String, String> commentEnd = null;

    public abstract Block replace(PlainBlock block);

    public List<String> contents() {
        List<String> contents = new ArrayList<String>();
        if (commentStart != null)
            contents.add(commentStart.first() + commentStart.second());
        for (Pair<String, String> p : commentLines) {
            contents.add(p.first()+ p.second());
        }
        if (commentEnd != null)
            contents.add(commentEnd.first()+commentEnd.second());
        return contents;
    }

    public void write(FileWrapper fw) throws IOException {
        if (commentStart != null)
            fw.writeLine(commentStart.first() + commentStart.second());
        for (Pair<String, String> p : commentLines) {
            fw.writeLine(p.first()+ p.second());
        }
        if (commentEnd != null)
            fw.writeLine(commentEnd.first()+commentEnd.second());
    }
    
    public List<String> comment() {
        List<String> contents = new ArrayList<String>();
        if (commentStart != null && !commentStart.second().trim().equals(""))
            contents.add(commentStart.second());
        for (Pair<String, String> p : commentLines) {
            contents.add(p.second());
        }
        if (commentEnd != null && !commentEnd.first().trim().equals(""))
            contents.add(commentEnd.first());
        return contents;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof CommentBlock))
            return false;

        CommentBlock block = (CommentBlock) obj;

        // Equal if contents are equal; we ignore the tags
        if (!commentStart.equals(block.commentStart)) {
            return false;
        }
        if (!commentEnd.equals(block.commentEnd)) {
            return false;
        }
        Iterator<Pair<String, String>> iter1 = commentLines.iterator();
        Iterator<Pair<String, String>> iter2 = block.commentLines.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            Pair<String, String> pair1 = iter1.next();
            Pair<String, String> pair2 = iter2.next();
            if (!pair1.equals(pair2))
                return false;
        }
        return iter1.hasNext() == iter2.hasNext();
    }

}
