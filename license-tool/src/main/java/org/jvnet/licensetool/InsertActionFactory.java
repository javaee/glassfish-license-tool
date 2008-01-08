package org.jvnet.licensetool;
import static org.jvnet.licensetool.Tags.*;
import org.jvnet.licensetool.generic.Pair;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rama Pulavarthi
 */
public class InsertActionFactory {
    public static ParsedFile.InsertCommentAction JAVA_INSERT_COMMENT_ACTION = new ParsedFile.InsertCommentAction(){
        public Boolean evaluate(ParsedFile pfile, CommentBlock cb) {
            pfile.fileBlocks.addFirst(cb);
            return true;
        }
    };

    public static ParsedFile.InsertCommentAction XML_INSERT_COMMENT_ACTION = new ParsedFile.InsertCommentAction(){
        public Boolean evaluate(ParsedFile pfile, CommentBlock cb) {
            Block firstBlock = pfile.fileBlocks.getFirst();
            if(firstBlock.hasTag(COMMENT_BLOCK_TAG)) {
                pfile.fileBlocks.addFirst(cb);
            } else {
                List<String> contents = firstBlock.contents();
                String firstLine = contents.get(0);
                if(firstLine.trim().startsWith("<?xml")) {
                    if(!firstLine.trim().endsWith("?>")) {
                        throw new RuntimeException("Needs special handling");
                    }
                    Pair<Block,Block> splitBlocks = firstBlock.splitFirst();
                    Block xmlDeclaration = splitBlocks.first();
                    Block restOfXml = splitBlocks.second();
                    firstBlock.replace(new Block(new ArrayList<String>()));
                    pfile.fileBlocks.addFirst(restOfXml);
                    pfile.fileBlocks.addFirst(cb);
                    pfile.fileBlocks.addFirst(xmlDeclaration);
                } else {
                    pfile.fileBlocks.addFirst(cb);
                }
            }
            return true;
        }
    };

    public static ParsedFile.InsertCommentAction SHELL_INSERT_COMMENT_ACTION = new ParsedFile.InsertCommentAction(){
        public Boolean evaluate(ParsedFile pfile, CommentBlock cb) {
            Block firstBlock = pfile.fileBlocks.getFirst();
            if(firstBlock.hasTag(COMMENT_BLOCK_TAG)) {
              List<String> contents = firstBlock.contents();
              String firstLine = contents.get(0);
                if(firstLine.trim().startsWith("#!")) {
                    Pair<Block,Block> splitBlocks = firstBlock.splitFirst();
                    Block sheBangBlock = splitBlocks.first();
                    Block rest = splitBlocks.second();
                    firstBlock.replace(new Block(new ArrayList<String>()));
                    pfile.fileBlocks.addFirst(rest);
                    pfile.fileBlocks.addFirst(cb);
                    pfile.fileBlocks.addFirst(sheBangBlock);
                } else {
                    pfile.fileBlocks.addFirst(cb);                    
                }

            } else {
                pfile.fileBlocks.addFirst(cb);
            }
            return true;
        }
    };

    private boolean isWhiteSpace(String str) {
        //line.matches("\\s*")
        if(str.trim().length() == 0)
            return true;
        return false;

        
    }
}
