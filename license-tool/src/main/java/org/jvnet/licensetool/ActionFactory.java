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

import static org.jvnet.licensetool.Tags.COPYRIGHT_BLOCK_TAG;
import static org.jvnet.licensetool.Tags.SUN_COPYRIGHT_TAG;
import static org.jvnet.licensetool.file.CommentBlock.COMMENT_BLOCK_TAG;
import org.jvnet.licensetool.file.Block;
import org.jvnet.licensetool.file.PlainBlock;
import org.jvnet.licensetool.file.CommentBlock;
import org.jvnet.licensetool.file.ParsedFile;

import java.io.IOException;
import java.util.List;

public class ActionFactory {
    private final boolean verbose;
    private final String COPYRIGHT = "Copyright";

    public ActionFactory() {
        this(false);
    }

    public ActionFactory(final boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * returns an action that returns true.  If verbose is true, the action
     * also displays the FileWrapper that was passed to it.
     */
    public Scanner.Action getSkipAction() {
        return new Scanner.Action() {
            public String toString() {
                return "SkipAction";
            }

            public boolean evaluate(ParsedFile arg) {
                if (verbose)
                    System.out.println(toString() + "called");

                return true;
            }

        };
    }

    /**
     * returns an action that returns false.  If verbose is true, the action
     * also displays the FileWrapper that was passed to it.
     */
    public Scanner.Action getStopAction() {
        return new Scanner.Action() {
            public String toString() {
                return "StopAction";
            }

            public boolean evaluate(ParsedFile arg) {
                if (verbose)
                    System.out.println(toString() + "called.");
                return false;
            }
        };
    }

    public Scanner.Action getValidateCopyrightAction(final PlainBlock copyrightBlock) {
        if (verbose) {
            trace("makeCopyrightBlockAction: copyrightText = " + copyrightBlock);
        }

        return new Scanner.Action() {
            public String toString() {
                return "CopyrightBlockAction[copyrightText=" + copyrightBlock + "]";
            }

            // Generally always return true, because we want to see ALL validation errors.
            public boolean evaluate(ParsedFile pfile) {
                //tag blocks
                boolean hadAnOldSunCopyright = tagBlocks(pfile);
                if(!hadAnOldSunCopyright) {
                    validationError(null,"No Sun Copyright header in ", pfile.getPath());
                }
                // There should be a Sun copyright block in the first block
                int countSunCopyright = 0;
                for (Block block : pfile.getFileBlocks()) {
                    if (block instanceof CommentBlock) {
                        if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG)) {
                            countSunCopyright++;
                            if (countSunCopyright > 1) {
                                validationError(block, "More than one Sun Copyright Block", pfile.getPath());
                                continue;
                            }
                            if (block.hasTag(CommentBlock.TOP_COMMENT_BLOCK)) {
                                if (!(copyrightBlock.contents().equals(((CommentBlock) block).contents()))) {
                                    // It should entirely match copyrightText
                                    validationError(block, "First block has incorrect copyright text", pfile.getPath());
                                }
                            } else {
                                validationError(block, "Sun Copyright Block is not the first comment block", pfile.getPath());
                            }
                        }
                    }
                }
                return true;
            }
        };
    }

    // Strip out old Sun copyright block.  Prepend new copyrightText.
    // copyrightText is a Block containing a copyright template in the correct comment format.
    // parseCall is the correct block parser for splitting the file into Blocks.
    // defaultStartYear is the default year to use in copyright comments if not
    // otherwise specified in an old copyright block.
    // afterFirstBlock is true if the copyright needs to start after the first block in the
    // file.

    public Scanner.Action getModifyCopyrightAction(final PlainBlock copyrightBlock1) {
        if (verbose) {
            trace("makeCopyrightBlockAction: copyrightText = " + copyrightBlock1);
        }

        return new Scanner.Action() {
            public String toString() {
                return "CopyrightBlockAction[copyrightText=" + copyrightBlock1 + "]";
            }

            public boolean evaluate(ParsedFile pfile) {
                //FileWrapper fw = pfile.getOriginalFile();
                //Block cb = pfile.insertCommentBlock(copyrightBlock);
                //TODO Check Block copyrightBlock = (Block) copyrightBlock1.clone();
                PlainBlock copyrightBlock = copyrightBlock1;
                //tag blocks
                boolean hadAnOldSunCopyright = tagBlocks(pfile);

                // Re-write file, replacing the first block tagged
                // SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, and commentBlock with
                // the copyrightText block.

                trace("Updating copyright/license header on file " + pfile.getPath());

                boolean firstMatch = true;
                boolean firstBlock = true;
                List<Block> fileBlocks = pfile.getFileBlocks();
                //List<Block> newFileBlocks =  new ArrayList<Block>();
                for (Block block : fileBlocks) {
                    if (!hadAnOldSunCopyright && firstBlock) {
                        pfile.insertCommentBlock(copyrightBlock.contents());
                        firstBlock = false;
                    } else if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG,
                            COMMENT_BLOCK_TAG) && firstMatch) {
                        firstMatch = false;
                        if (hadAnOldSunCopyright) {
                            ((CommentBlock)block).replace(copyrightBlock.contents());
                        }
                    } else {
                        //
                    }
                }
                //pfile.setFileBlocks(newFileBlocks);
                try {
                    pfile.write();
                } catch (IOException exc) {
                    trace("Exception while processing file " + pfile.getPath() + ": " + exc);
                    exc.printStackTrace();
                    return false;
                }
                return true;
            }
        };
    }

    //Just delete the original file and rewrite it to test if parsing and writing back works correctly.

    public Scanner.Action getReWriteCopyrightAction() {

        return new Scanner.Action() {
            public boolean evaluate(ParsedFile pfile) {
                try {
                    pfile.write();
                } catch (IOException exc) {
                    trace("Exception while processing file " + pfile.getPath() + ": " + exc);
                    exc.printStackTrace();
                    return false;
                }
                return true;
            }
        };
    }

    private boolean tagBlocks(ParsedFile pfile) {
        boolean hadAnOldSunCopyright = false;
        // Tag blocks
        for (Block block : pfile.getFileBlocks()) {
            if(block instanceof CommentBlock) {
                CommentBlock cb = (CommentBlock)block;
                String str = cb.find(COPYRIGHT);
                if (str != null) {
                    block.addTag(COPYRIGHT_BLOCK_TAG);
                    String cddl = cb.find("CDDL");
                    if (cddl != null) {
                        block.addTag("CDDL_TAG");
                    }
                    if (str.contains("Sun")) {
                        block.addTag(SUN_COPYRIGHT_TAG);
                        hadAnOldSunCopyright = true;
                    }
                }
            }
        }

        if (verbose) {
            trace("copyrightBlockAction: blocks in file " + pfile.getPath());
            for (Block block : pfile.getFileBlocks()) {
                traceBlock(block);
            }
        }
        return hadAnOldSunCopyright;
    }

    private void trace(String msg) {
        System.out.println(msg);
    }

    private void validationError(Block block, String msg, String fw) {
        trace("Copyright validation error: " + msg + " for " + fw);
        if ((verbose) && (block != null)) {
            traceBlock(block);
        }
    }

    private void traceBlock(Block block) {

        trace("Block=" + block);
        trace("Block contents:");
        if (block instanceof PlainBlock) {
            trace("\"" + ((PlainBlock) block).contents() + "\"");
        } else if (block instanceof CommentBlock) {
            trace("\"" + ((PlainBlock) block).contents() + "\"");
        }

    }
}