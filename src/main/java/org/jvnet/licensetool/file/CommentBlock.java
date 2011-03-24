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
    public static final String TOP_COMMENT_BLOCK = "TopCommentBlock";

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
