package org.jvnet.licensetool;

import org.jvnet.licensetool.generic.Pair;

import java.util.*;
import java.io.IOException;

/**
 * Marker for CommentBlock
 */
public abstract class CommentBlock extends Block {
    protected Pair<String, String> commentStart = null;
    protected List<Pair<String, String>> commentLines = new ArrayList<Pair<String, String>>();
    protected Pair<String, String> commentEnd = null;

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

    public int hashCode() {
        int hash = 0 ;
	    if(commentStart != null)
            hash ^= commentStart.hashCode() ;
        for (Pair<String,String> commentline : commentLines)
	        hash ^= commentline.hashCode() ;
        if(commentEnd != null)
            hash ^= commentEnd.hashCode() ;
        return hash ;
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

    public static class LineComment extends CommentBlock {
        String prefix;

        private LineComment(String prefix, final List<String> data, Set<String> tags) {
            this.tags = tags;
            this.prefix = prefix;
            parse(data);

        }
        public LineComment(String prefix, final List<String> data) {
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
            return new LineComment(prefix, commentTextBlock);
        }

        public Block replace(Block block) {
            commentLines.clear();
            for (String str : block.contents()) {
                commentLines.add(new Pair<String, String>(prefix, str));
            }
            return this;
        }

        protected Object clone() {
            return new LineComment(prefix, contents(), tags);
        }

        private void parse(List<String> data) {
            for (String str : data) {
                String commmentPrefix = str.substring(0, str.indexOf(prefix) + prefix.length());
                String commentSuffix = str.substring(str.indexOf(prefix) + prefix.length());
                commentLines.add(new Pair<String, String>(commmentPrefix, commentSuffix));
            }
        }
    }

    public static class BlockComment extends CommentBlock {
        String prefix;
        String start;
        String end;

        public BlockComment(String start, String end, String prefix, final List<String> data) {
            this.start = start;
            this.end = end;
            this.prefix = prefix;
            parse(data);
        }
        private BlockComment(String start, String end, String prefix, final List<String> data, Set<String> tags) {
            this.tags = tags;
            this.start = start;
            this.end = end;
            this.prefix = prefix;
            parse(data);
        }
        /*
        public BlockComment(String start, String end, String prefix, final Block dataBlock) {
            super(dataBlock);
            this.start = start;
            this.end = end;
            this.prefix = prefix;
            parse(dataBlock.contents());
        }
        */

        protected Object clone() {
            return new BlockComment(start,end,prefix,contents(),tags);
        }

        public static CommentBlock createCommentBlock(String start, String end, String prefix,
                                                      final List<String> commentText) {
            final List<String> commentTextBlock = new ArrayList<String>();
            for (String str : commentText) {
	            commentTextBlock.add( prefix + str ) ;
	        }
	        commentTextBlock.add(0,start);
            commentTextBlock.add(commentTextBlock.size(), end);
            return new BlockComment(start, end, prefix, commentTextBlock);
        }

        public Block replace(Block block) {
            commentStart = new Pair<String, String>(commentStart.first(), "");
            commentLines.clear();
            for (String str : block.contents()) {
                commentLines.add(new Pair<String, String>(prefix, str));
            }
            commentEnd = new Pair<String, String>("", commentEnd.second());

            // update the block content-view
            return this;
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
                            String endCommentSuffix = rest.substring(endindex);
                            commentEnd = new Pair<String, String>("", endCommentSuffix);
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
                        String endCommmentPrefix = str.substring(0, index);
                        String endCommentSuffix = str.substring(index);
                        commentEnd = new Pair<String, String>(endCommmentPrefix, endCommentSuffix);
                    }
                } else {
                    int index = str.indexOf(prefix);
                    if (index < 0) {
                        String commmentPrefix = "";
                        String commentSuffix = str;
                        commentLines.add(new Pair<String, String>(commmentPrefix, commentSuffix));

                    } else {
                        String commmentPrefix = str.substring(0, index + prefix.length());
                        String commentSuffix = str.substring(index + prefix.length());
                        commentLines.add(new Pair<String, String>(commmentPrefix, commentSuffix));
                    }
                }
            }
        }
    }
}
