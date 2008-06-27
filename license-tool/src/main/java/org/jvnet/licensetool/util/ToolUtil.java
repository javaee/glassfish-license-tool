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

package org.jvnet.licensetool.util;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Collect all the utility methods used by the Tool here.
 * @author Rama Pulavarthi
 */
public class ToolUtil {
    public static List<String> splitToLines(String data) {
        List<String> lines = new ArrayList<String>();
        String patternStr = "(.+?)^";
        Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);
        int index = 0;// to store last match
        while (matcher.find()) {
            lines.add(matcher.group());
            index = matcher.end();

        }
        //get the rest
        lines.add(data.substring(index));
        return lines;
    }

    public static String sniffLineSeparator(String data) {
        List<String> lines = splitToLines(data);
        String fline = lines.get(0);
        int flineLength = fline.length();
        if (flineLength > 1) {
            if ((fline.charAt(flineLength - 2) == '\r') && (fline.charAt(flineLength - 1) == '\n')) {
                return "\r\n";
            }
        }
        if(fline.charAt(flineLength-1)=='\n'){
                return "\n";
        } else if(fline.charAt(flineLength-1)=='\r'){
                return "\r";
        }
        return null;
    }

    public static String covertLineBreak(String inputStr, String line_separator) {
        String patternStr = "(.*)$";
        Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.find()) {
            return matcher.group() + line_separator;
        } else {
            return inputStr;
        }
    }

    public static boolean areCommentsEqual(String exp, String got) {
        List<String> expLines = splitToLines(exp);
        List<String> gotLines = splitToLines(got);
        if(expLines.size() != gotLines.size())
            return false;
        for(int i=0;i<expLines.size();i++) {
            if(!(expLines.get(i).trim().equals(gotLines.get(i).trim())))
                return false;
        }
        return true;
    }

    public static boolean areCommentsSimilar(String exp, String got) {
        if(EditDistance.editDistance(exp,got) <=  10) {
            return true;
        }
        System.out.println("Expected: " + exp);
        System.out.println("Got     : " + got);
        return false;

    }

}
