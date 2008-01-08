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
import static org.jvnet.licensetool.Tags.COMMENT_BLOCK_TAG;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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

    public Scanner.Action getValidateCopyrightAction(final Block copyrightBlock1) {
        if (verbose) {
            trace("makeCopyrightBlockAction: copyrightText = " + copyrightBlock1);
        }

        return new Scanner.Action() {
            public String toString() {
                return "CopyrightBlockAction[copyrightText=" + copyrightBlock1 + "]";
            }

            public boolean evaluate(ParsedFile pfile) {
               /*
                //Block cb = pfile.createCommentBlock(copyrightBlock);
                Block copyrightBlock = (Block) copyrightBlock1.clone();
                //tag blocks
                boolean hadAnOldSunCopyright = tagBlocks(pfile);

                // There should be a Sun copyright block in the first block
                // (if afterFirstBlock is false), otherwise in the second block.
                // It should entirely match copyrightText
                int count = 0;
                for (Block block : pfile.getFileBlocks()) {

                    // Generally always return true, because we want to see ALL validation errors.
                    if (!pfile.commentAfterFirstBlock() && (count == 0)) {
                        if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG,
                                COMMENT_BLOCK_TAG)) {
                            if (!copyrightBlock.equals(new Block(block.contents()))) {
                                validationError(block, "First block has incorrect copyright text", pfile.toString());
                            }
                        } else {
                            validationError(block, "First block should be copyright but isn't", pfile.toString());
                        }

                        return true;
                    } else if (pfile.commentAfterFirstBlock() && (count == 1)) {
                        if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, COMMENT_BLOCK_TAG)) {
                            if (!copyrightBlock.equals(new Block(block.contents()))) {
                                validationError(block, "Second block has incorrect copyright text", pfile.toString());
                            }
                        } else {
                            validationError(block, "Second block should be copyright but isn't", pfile.toString());
                        }
                        return true;
                    }

                    if (count > 1) {
                        // should not get here!  Return false only in this case, because this is
                        // an internal error in the validator.
                        validationError(null, "Neither first nor second block checked", pfile.toString());
                        return false;
                    }

                    count++;

                }
                */
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

    public Scanner.Action getModifyCopyrightAction(final Block copyrightBlock1) {
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
                Block copyrightBlock = (Block) copyrightBlock1.clone();
                //tag blocks
                boolean hadAnOldSunCopyright = tagBlocks(pfile);

                // Re-write file, replacing the first block tagged
                // SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, and commentBlock with
                // the copyrightText block.

                trace("Updating copyright/license header on file " + pfile.toString());

                boolean firstMatch = true;
                boolean firstBlock = true;
                List<Block> fileBlocks = pfile.getFileBlocks();
                //List<Block> newFileBlocks =  new ArrayList<Block>();
                for (Block block : fileBlocks) {
                    if (!hadAnOldSunCopyright && firstBlock) {
                        pfile.insertCommentBlock(pfile.createCommentBlock(copyrightBlock));
                        firstBlock = false;
                    } else if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG,
                            COMMENT_BLOCK_TAG) && firstMatch) {
                        firstMatch = false;
                        if (hadAnOldSunCopyright) {
                            block.replace(copyrightBlock);
                        }
                    } else {
                        //
                    }
                }
                //pfile.setFileBlocks(newFileBlocks);
                try {
                    pfile.write();
                } catch (IOException exc) {
                    trace("Exception while processing file " + pfile.toString() + ": " + exc);
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
                List<Block> fileBlocks = pfile.getFileBlocks();
                List<Block> newFileBlocks =  new ArrayList<Block>();
                for (Block block : fileBlocks) {
                        newFileBlocks.add(block);
                }
                pfile.setFileBlocks(newFileBlocks);
                try {
                    pfile.write();
                } catch (IOException exc) {
                    trace("Exception while processing file " + pfile.toString() + ": " + exc);
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
            String str = block.find(COPYRIGHT);
            if (str != null) {
                block.addTag(COPYRIGHT_BLOCK_TAG);
                String cddl = block.find("CDDL");
                if (cddl != null) {
                    block.addTag("CDDL_TAG");
                }
                if (str.contains("Sun")) {
                    block.addTag(SUN_COPYRIGHT_TAG);
                    hadAnOldSunCopyright = true;
                }
            }
        }

        if (verbose) {
            trace("copyrightBlockAction: blocks in file " + pfile.toString());
            for (Block block : pfile.getFileBlocks()) {
                trace("\t" + block);
                for (String str : block.contents()) {
                    trace("\t\t" + str);
                }
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
            trace("Block=" + block);
            trace("Block contents:");
            for (String str : block.contents()) {
                trace("\"" + str + "\"");
            }
        }
    }
}