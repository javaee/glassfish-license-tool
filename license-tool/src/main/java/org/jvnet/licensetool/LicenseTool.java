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

import org.jvnet.licensetool.argparser.ArgParser;
import org.jvnet.licensetool.argparser.DefaultValue;
import org.jvnet.licensetool.argparser.Help;
import org.jvnet.licensetool.file.PlainBlock;
import org.jvnet.licensetool.file.FileWrapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LicenseTool {
    private LicenseTool() {
    }

    public interface Arguments {
        @DefaultValue("true")
        @Help("Set to true to validate copyright header; if false, generate/update/insert copyright headers as needed")
        boolean validate();

        @DefaultValue("false")
        @Help("Set to true to get information about actions taken for every file.")
        boolean verbose();

        @DefaultValue("true")
        @Help("Set to true to avoid modifying any files")
        boolean dryrun();

        @Help("List of directories to process")
        @DefaultValue("")
        List<File> roots();

        @Help("List of directory names that should be skipped")
        @DefaultValue("")
        List<String> skipdirs();

        @Help("File containing text of copyright header.  This must not include any comment characters")
        @DefaultValue("")
        FileWrapper copyright();

        @DefaultValue("1997")
        @Help("Default copyright start year, if not otherwise specified")
        String startyear();
    }

    private static boolean validate;
    private static boolean verbose;


    private static void trace(String msg) {
        System.out.println(msg);
    }

    private static final String COPYRIGHT = "Copyright";

    // Copyright year is first non-blank after COPYRIGHT
    private static String getSunCopyrightStart(String str) {
        int index = str.indexOf(COPYRIGHT);
        if (index == -1)
            return null;

        int pos = index + COPYRIGHT.length();
        char ch = str.charAt(pos);
        while (Character.isWhitespace(ch) && (pos < str.length())) {
            ch = str.charAt(++pos);
        }

        int start = pos;
        ch = str.charAt(pos);
        while (Character.isDigit(ch) && (pos < str.length())) {
            ch = str.charAt(++pos);
        }

        if (pos == start)
            return null;

        return str.substring(start, pos);
    }

    private static final String START_YEAR = "StartYear";

    private static PlainBlock makeCopyrightBlock(String startYear,
                                            PlainBlock copyrightText) {

        if (verbose) {
            trace("makeCopyrightBlock: startYear = " + startYear);
            trace("makeCopyrightBlock: copyrightText = " + copyrightText);

            trace("Contents of copyrightText block:");
            trace(copyrightText.contents());

        }

        Map<String, String> map = new HashMap<String, String>();
        map.put(START_YEAR, startYear);
        PlainBlock withStart = copyrightText.instantiateTemplate(map);

        if (verbose) {
            trace("Contents of copyrightText block withStart date:");
            trace(withStart.contents());
        }

        return withStart;
    }

    public static void process(Arguments args) {
        String startYear = args.startyear();
        verbose = args.verbose();
        validate = args.validate();

        if (verbose) {
            trace("Main: args:\n" + args);
        }

        try {
            // Create the blocks needed for different forms of the
            // copyright comment template
            final PlainBlock copyrightText = new PlainBlock(args.copyright());
            PlainBlock copyrightBlock = makeCopyrightBlock(startYear, copyrightText);
            Scanner scanner = new Scanner(verbose, args.dryrun(), args.roots());
            for (String str : args.skipdirs())
                scanner.addDirectoryToSkip(str);

            Scanner.Action action;
            if(validate) {
                action = new ActionFactory(verbose).getValidateCopyrightAction(copyrightBlock);
            } else {
                action = new ActionFactory(verbose).getModifyCopyrightAction(copyrightBlock);
                //action = new ActionFactory(verbose).getReWriteCopyrightAction();
            }
            // Finally, we process all files
            scanner.scan(new RecognizerFactory().getDefaultRecognizer(), action);
        } catch (IOException exc) {
            System.out.println("Exception while processing: " + exc);
            exc.printStackTrace();
        }
    }

    public static void main(String[] strs) {
        ArgParser<Arguments> ap = new ArgParser(Arguments.class);
        Arguments args = ap.parse(strs);
        LicenseTool.process(args);        
    }

}