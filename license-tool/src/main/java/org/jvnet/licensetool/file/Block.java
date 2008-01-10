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

import org.jvnet.licensetool.file.FileWrapper;

import java.util.*;
import java.io.IOException;

/**
 * Represents a range of Strings, typically read from a file, that are in some sense
 * related and contiguous.  Blocks may also be tagged as an aid in transforming
 * a series of blocks.
 */
public abstract class Block {
    protected Set<String> tags;

    public Block(Set<String> tags) {
        this.tags = tags;
    }

    public Block() {
        this.tags = new HashSet<String>();
    }
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Block[");
        boolean first = true;
        for (String tag : tags) {
            if (first) {
                first = false;
            } else {
                sb.append(" ");
            }

            sb.append(tag);
        }
        sb.append("]");
        return sb.toString();
    }

    

    /** Create a new Block which is a copy of block.
     */
    /*
    public Block( final Block block ) {
    this.data = new ArrayList<String>( block.data ) ;
    this.tags = new HashSet<String>( block.tags ) ;
    }
    */


    /**
     * Add a tag to the block.  Useful for classifying blocks.
     */
    public void addTag(final String tag) {
        tags.add(tag);
    }

    /**
     * Return whether or not a block has a particular tag.
     */
    public boolean hasTag(final String tag) {
        return tags.contains(tag);
    }

    /**
     * Return whether or not a block has ALL of the listed tags.
     */
    public boolean hasTags(final String... tags) {
        for (String tag : tags) {
            if (!hasTag(tag))
                return false;
        }
        return true;
    }

    /**
     * Get the contents of the block.
     */
    //TODO TODO REMOVE
    //public abstract List<String> contents();


    /**
     * Return the first string in the block that contains the search string.
     */
    //TODO TODO REMOVE
    public String find(final String search) {
      /*  for (String str : contents()) {
            if (str.contains(search))
                return str;
        }
        */
        return null;
    }

    /**
     * Write block to FileWrapper.  FileWrapper must be open for writing.
     */

    public abstract void write(final FileWrapper fw) throws IOException;



}
