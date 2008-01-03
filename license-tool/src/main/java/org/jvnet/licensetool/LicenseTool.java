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

    private static void validationError(Block block, String msg, FileWrapper fw) {
        trace("Copyright validation error: " + msg + " for " + fw);
        if ((verbose > 0) && (block != null)) {
            trace("Block=" + block);
            trace("Block contents:");
            for (String str : block.contents()) {
                trace("\"" + str + "\"");
            }
        }
    }

    // Strip out old Sun copyright block.  Prepend new copyrightText.
    // copyrightText is a Block containing a copyright template in the correct comment format.
    // parseCall is the correct block parser for splitting the file into Blocks.
    // defaultStartYear is the default year to use in copyright comments if not
    // otherwise specified in an old copyright block.
    // afterFirstBlock is true if the copyright needs to start after the first block in the
    // file.

    private static Scanner.Action makeCopyrightBlockAction(final Block copyrightText,
                                                           final String defaultStartYear) {
        if (verbose > 0) {
            trace("makeCopyrightBlockAction: copyrightText = " + copyrightText);
            trace("makeCopyrightBlockAction: defaultStartYear = " + defaultStartYear);
        }

        return new Scanner.Action() {
            public String toString() {
                return "CopyrightBlockAction[copyrightText=" + copyrightText
                        + ",defaultStartYear=" + defaultStartYear + "]";
            }

            public boolean evaluate(ParsedFile pfile) {
                FileWrapper fw = pfile.getOriginalFile();
                try {
                    String startYear = defaultStartYear;
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
                                startYear = getSunCopyrightStart(str);
                                block.addTag(SUN_COPYRIGHT_TAG);
                                hadAnOldSunCopyright = true;
                            }
                        }
                    }

                    if (verbose > 1) {
                        trace("copyrightBlockAction: blocks in file " + fw);
                        for (Block block : pfile.getFileBlocks()) {
                            trace("\t" + block);
                            for (String str : block.contents()) {
                                trace("\t\t" + str);
                            }
                        }
                    }

                    Block cb = makeCopyrightBlock(startYear, copyrightText);

                    if (validate) {
                        // There should be a Sun copyright block in the first block
                        // (if afterFirstBlock is false), otherwise in the second block.
                        // It should entirely match copyrightText
                        int count = 0;
                        for (Block block : pfile.getFileBlocks()) {

                            // Generally always return true, because we want to see ALL validation errors.
                            if (!pfile.commentAfterFirstBlock() && (count == 0)) {
                                if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG,
                                        COMMENT_BLOCK_TAG)) {
                                    if (!cb.equals(block)) {
                                        validationError(block, "First block has incorrect copyright text", fw);
                                    }
                                } else {
                                    validationError(block, "First block should be copyright but isn't", fw);
                                }

                                return true;
                            } else if (pfile.commentAfterFirstBlock() && (count == 1)) {
                                if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, COMMENT_BLOCK_TAG)) {
                                    if (!cb.equals(block)) {
                                        validationError(block, "Second block has incorrect copyright text", fw);
                                    }
                                } else {
                                    validationError(block, "Second block should be copyright but isn't", fw);
                                }
                                return true;
                            }

                            if (count > 1) {
                                // should not get here!  Return false only in this case, because this is
                                // an internal error in the validator.
                                validationError(null, "Neither first nor second block checked", fw);
                                return false;
                            }

                            count++;

                        }
                    } else {
                        // Re-write file, replacing the first block tagged
                        // SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, and commentBlock with
                        // the copyrightText block.

                        if (fw.canWrite()) {
                            trace("Updating copyright/license header on file " + fw);

                            // XXX this is dangerous: a crash before close will destroy the file!
                            fw.delete();
                            fw.open(FileWrapper.OpenMode.WRITE);

                            boolean firstMatch = true;
                            boolean firstBlock = true;
                            for (Block block : pfile.getFileBlocks()) {
                                if (!hadAnOldSunCopyright && firstBlock) {
                                    if (pfile.commentAfterFirstBlock()) {
                                        block.write(fw);
                                        cb.write(fw);
                                    } else {
                                        cb.write(fw);
                                        block.write(fw);
                                    }
                                    firstBlock = false;
                                } else if (block.hasTags(SUN_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG,
                                        COMMENT_BLOCK_TAG) && firstMatch) {
                                    firstMatch = false;
                                    if (hadAnOldSunCopyright) {
                                        cb.write(fw);
                                    }
                                } else {
                                    block.write(fw);
                                }
                            }
                        } else {
                            if (verbose > 1) {
                                trace("Skipping file " + fw + " because is is not writable");
                            }
                        }
                    }
                } catch (IOException exc) {
                    trace("Exception while processing file " + fw + ": " + exc);
                    exc.printStackTrace();
                    return false;
                } finally {
                    fw.close();
                }


                return true;
            }
        };
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

            Scanner scanner = new Scanner(verbose, args.roots());
            for (String str : args.skipdirs())
                scanner.addDirectoryToSkip(str);

            Scanner.Action action = makeCopyrightBlockAction(copyrightText, startYear);

            // Finally, we process all files
            scanner.scan(new RecognizerFactory().getDefaultRecognizer(), action);
        } catch (IOException exc) {
            System.out.println("Exception while processing: " + exc);
            exc.printStackTrace();
        }
    }
}