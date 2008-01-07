package org.jvnet.licensetool;

import org.jvnet.licensetool.generic.Pair;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Marker for CommentBlock
 */
public class CommentBlock extends Block{
    protected Pair<String,String> commentStart = null;
    protected List<Pair<String, String>> commentLines = new ArrayList<Pair<String,String>>();
    protected Pair<String,String> commentEnd = null;
    public CommentBlock(final List<String> data) {
        super(data);
    }
    public CommentBlock(final Block data) {
        super(data);
    }

    public static CommentBlock createCommentBlock(final List<String> commentText) {
            return new CommentBlock(commentText);            
    }

    public List<String> contents() {
        List<String> contents = new ArrayList<String>();
        if(commentStart != null && !commentStart.second().trim().equals(""))
            contents.add(commentStart.second());
        for(Pair<String,String> p : commentLines) {
            contents.add(p.second());
        }
        if(commentEnd!= null && !commentEnd.first().trim().equals(""))
            contents.add(commentEnd.first());
        return contents;
    }

    public Block replaceText(Block block) {
        return super.replace(block);
    }

    public boolean equals(Object obj) {
        if (obj == this)
	        return true ;

	    if (!(obj instanceof CommentBlock))
	        return false ;

	    CommentBlock block = (CommentBlock)obj ;

	    // Equal if contents are equal; we ignore the tags
        if(!commentStart.equals(block.commentStart)) {
            return false;
        }
        if(!commentEnd.equals(block.commentEnd)) {
            return false;
        }
        Iterator<Pair<String,String>> iter1 = commentLines.iterator() ;
	    Iterator<Pair<String,String>> iter2 = block.commentLines.iterator() ;
	    while (iter1.hasNext() && iter2.hasNext()) {
	        Pair<String,String> pair1 = iter1.next() ;
	        Pair<String,String> pair2 = iter2.next() ;
	        if (!pair1.equals( pair2 ))
		        return false ;
	    }
	    return iter1.hasNext() == iter2.hasNext() ;
    }

    public static class LineComment extends CommentBlock {
        String prefix;
        public LineComment(String prefix, final List<String> data) {
            super(data);
            this.prefix = prefix;
            parse(data);
        }

        public LineComment(String prefix, final Block dataBlock) {
            super(dataBlock);
            this.prefix = prefix;
            parse(dataBlock.data);
        }

        public static CommentBlock createCommentBlock(String prefix, final List<String> commentText) {
            Block commentTextBlock = new Block(commentText);
            commentTextBlock.addPrefixToAll(prefix);
            return new LineComment(prefix, commentTextBlock.data);            
        }

        public Block replace(Block blockText) {
            commentLines.clear();
            for(String str: blockText.data) {
                commentLines.add(new Pair<String, String>(prefix, str));
            }
            // update the block content-view
            blockText.addPrefixToAll(prefix);
            super.replace(blockText);
            
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

    public static class BlockComment extends CommentBlock {
        String prefix;
        String start;
        String end;
        public BlockComment(String start, String end, String prefix, final List<String> data) {
            super(data);
            this.start = start;
            this.end = end;
            this.prefix =  prefix;
            parse(data);
        }
        public BlockComment(String start, String end, String prefix, final Block dataBlock) {
            super(dataBlock);
            this.start = start;
            this.end = end;
            this.prefix =  prefix;
            parse(dataBlock.data);
        }
        public static CommentBlock createCommentBlock(String start, String end, String prefix,
                                                      final List<String> commentText) {
            Block commentTextBlock = new Block(commentText);
            commentTextBlock.addPrefixToAll(prefix);
            commentTextBlock.addBeforeFirst(start);
            commentTextBlock.addAfterLast(end);
            return new BlockComment(start, end, prefix, commentTextBlock.data);            
        }

        public Block replace(Block blockText) {
            commentStart = new Pair<String,String>(commentStart.first(), "");
            commentLines.clear();
            for(String str: blockText.data) {
                commentLines.add(new Pair<String, String>(prefix, str));
            }
            commentEnd = new Pair<String,String>("",commentEnd.second());

            // update the block content-view
            blockText.addPrefixToAll(prefix);
            blockText.addBeforeFirst(commentStart.first());
            blockText.addAfterLast(commentEnd.second());

            return super.replace(blockText);
        }

        private void parse(List<String> data) {
            for(int i=0; i<data.size();i++) {
                String str = data.get(i);
                if(i==0) {
                    int index = str.indexOf(start);
                    if (index < 0) {
//                        String startCommmentPrefix = start;
//                        String startCommentSuffix = str;
//                        commentStart = new Pair<String, String>(startCommmentPrefix, startCommentSuffix);
                        throw new RuntimeException("Cooment block does n't caontain start marker " + start);

                    } else {
                        String startCommmentPrefix = str.substring(0, index+start.length());
                        String startCommentSuffix = str.substring(index + start.length());
                        commentStart = new Pair<String, String>(startCommmentPrefix, startCommentSuffix);
                    }
                } else if(i== (data.size()-1)) {
                    int index = str.indexOf(end);
                    if(index < 0) {
//                        String endCommmentPrefix = str;
//                        String endCommentSuffix = end;
//                        commentEnd = new Pair<String,String>(endCommmentPrefix,endCommentSuffix);
                        throw new RuntimeException("Cooment block does n't contain end marker " + end);
                    } else {
                        String endCommmentPrefix = str.substring(0,index);
                        String endCommentSuffix = str.substring(index);
                        commentEnd = new Pair<String,String>(endCommmentPrefix,endCommentSuffix);
                    }
                } else {
                    int index = str.indexOf(prefix);
                    if(index < 0) {
                        String commmentPrefix = "";
                        String commentSuffix = str;
                        commentLines.add(new Pair<String,String>(commmentPrefix,commentSuffix));

                    } else {
                        String commmentPrefix = str.substring(0, index + prefix.length());
                        String commentSuffix = str.substring(index + prefix.length());
                        commentLines.add(new Pair<String,String>(commmentPrefix,commentSuffix));
                    }    
                }
            }
        }
    }
}
