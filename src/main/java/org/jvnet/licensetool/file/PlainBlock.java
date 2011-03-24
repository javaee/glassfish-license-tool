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

import org.jvnet.licensetool.generic.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

/**
 * Represents a portion of file as a list of lines.
 */
public class PlainBlock extends Block {

    private String data;

    public PlainBlock(final String data, final Set<String> tags) {
        super(tags);
        this.data = data;
    }

    /**
     * Create a new PlainBlock from a list of strings.
     */
    public PlainBlock(final String data) {
        this.data = data;
    }

    /**
     * Return the contents of the text file as a PlainBlock.
     */
    public PlainBlock(final FileWrapper fw) throws IOException {
        fw.open(FileWrapper.OpenMode.READ);
        try {
            data = fw.readAsString();
        } finally {
            fw.close();
        }
    }

    public String contents() {
        return data;
    }

    public void write(FileWrapper fw) throws IOException {
        fw.write(data);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof PlainBlock))
            return false;

        PlainBlock block = (PlainBlock) obj;


        String objdata = block.data;
        return(data.equals(objdata));
    }

    public int hashCode() {
        return data.hashCode();
    }

    /**
     * replace all occurrences of @KEY@ with parameters.get( KEY ).
     * This is very simple: only one scan is made, so @...@ patterns
     * in the parameters values are ignored.
     */
    public PlainBlock instantiateTemplate(Map<String, String> parameters) {

        final StringBuilder sb = new StringBuilder();
        final StringTokenizer st = new StringTokenizer(data, "@");

        // Note that the pattern is always TEXT@KEY@TEXT@KEY@TEXT,
        // so the the first token is not a keyword, and then the tokens
        // alternate.
        boolean isKeyword = false;
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            final String replacement =
                    isKeyword ? parameters.get(token) : token;
            sb.append(replacement);
            isKeyword = !isKeyword;
        }
        return new PlainBlock(sb.toString());
    }

    /*
    public Block substitute(List<Pair<String, String>> substitutions) {
        List<String> result = new ArrayList<String>();
        for (String line : data) {
            String newLine = line;
            for (Pair<String, String> pair : substitutions) {
                String pattern = pair.first();
                String replacement = pair.second();
                newLine = newLine.replace(pattern, replacement);
            }
            result.add(newLine);
        }
        return new PlainBlock(result);
    }
    */
    /**
     * Split block into two blocks, with only the
     * first line of the original Block in result.first().
     */
    public Pair<Block, Block> splitFirst() {
        //String patternStr = "(?m)(.*)[\\r]?[\\n]?";
        String patternStr = "(.+?)^";

        Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            String fline = matcher.group();
            String rest = data.substring(matcher.end());
            return new Pair<Block, Block>(
                new PlainBlock(fline, tags), new PlainBlock(rest, tags));
        } else {
            return new Pair<Block, Block>(new PlainBlock(data, tags),null);
        }


    }
}