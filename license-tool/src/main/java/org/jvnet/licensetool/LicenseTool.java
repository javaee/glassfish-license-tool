package org.jvnet.licensetool;

import org.jvnet.licensetool.argparser.ArgParser;
import org.jvnet.licensetool.argparser.DefaultValue;
import org.jvnet.licensetool.argparser.Help;
import static org.jvnet.licensetool.Tags.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LicenseTool {
    private LicenseTool() {
    }

    private interface Arguments {
        @DefaultValue("true")
        @Help("Set to true to validate copyright header; if false, generate/update/insert copyright headers as needed")
        boolean validate();

        @DefaultValue("0")
        @Help("Set to >0 to get information about actions taken for every file.  Larger values give more detail.")
        int verbose();

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
    private static int verbose;


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

    private static Block makeCopyrightBlock(String startYear,
                                            Block copyrightText) {

        if (verbose > 1) {
            trace("makeCopyrightBlock: startYear = " + startYear);
            trace("makeCopyrightBlock: copyrightText = " + copyrightText);

            trace("Contents of copyrightText block:");
            for (String str : copyrightText.contents()) {
                trace("\t" + str);
            }
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put(START_YEAR, startYear);
        Block withStart = copyrightText.instantiateTemplate(map);

        if (verbose > 1) {
            trace("Contents of copyrightText block withStart date:");
            for (String str : withStart.contents()) {
                trace("\t" + str);
            }
        }

        return withStart;
    }
   
    public static void main(String[] strs) {
        ArgParser<Arguments> ap = new ArgParser(Arguments.class);
        Arguments args = ap.parse(strs);

        String startYear = args.startyear();
        verbose = args.verbose();
        validate = args.validate();

        if (verbose > 0) {
            trace("Main: args:\n" + args);
        }

        try {
            // Create the blocks needed for different forms of the
            // copyright comment template
            final Block copyrightText = FileParser.getBlock(args.copyright());
            Block copyrightBlock = makeCopyrightBlock(startYear, copyrightText);
            Scanner scanner = new Scanner(verbose, args.roots());
            for (String str : args.skipdirs())
                scanner.addDirectoryToSkip(str);

            Scanner.Action action;
            if(validate) {
                action = new ActionFactory(true,true).getValidateCopyrightAction(copyrightBlock);
            } else {
                action = new ActionFactory(true,true).getModifyCopyrightAction(copyrightBlock);
            }
            // Finally, we process all files
            scanner.scan(new RecognizerFactory().getDefaultRecognizer(), action);
        } catch (IOException exc) {
            System.out.println("Exception while processing: " + exc);
            exc.printStackTrace();
        }
    }
}