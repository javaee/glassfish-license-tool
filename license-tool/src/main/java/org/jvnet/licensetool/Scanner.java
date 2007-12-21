package org.jvnet.licensetool;

import org.jvnet.licensetool.generic.UnaryBooleanFunction;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Arrays;

/** Recursively scan directories to process files.
 */
public class Scanner {
    private final List<File> roots ;
    private final int verbose ;
    private List<String> patternsToSkip ;

    public Scanner( int verbose, final List<File> files ) {
	this.roots = files ;
	this.verbose = verbose ;
	patternsToSkip = new ArrayList<String>() ;
    }

    public Scanner( final int verbose, final File... files ) {
	this( verbose, Arrays.asList( files ) ) ;
    }

    /** Add a pattern that defines a directory to skip.  We only need really simple
     * patterns: just a single name that must match a component of a directory name
     * exactly.
     */
    public void addDirectoryToSkip( final String pattern ) {
	patternsToSkip.add( pattern ) ;
    }

    /** Action interface passed to scan method to act on files.
     * Terminates scan if it returns false.
     */
    public interface Action extends UnaryBooleanFunction<List<Block>> {}

    /** Scan all files reachable from roots.  Does a depth-first search.
     * Ignores all directories (and their contents) that match an entry
     * in patternsToSkip.  Passes each file (not directories) to the action.
     * If action returns false, scan terminates.  The result of the scan is
     * the result of the last action call.
     */
    public boolean scan(final FileRecognizer recognizer, final Scanner.Action action) throws IOException {
        boolean result = true;
        for (File file : roots) {
            result = doScan(file, recognizer, action);
            if (!result)
                break;
        }
	    return result ;
    }

    private boolean doScan(final File file, final FileRecognizer recognizer, final Scanner.Action action)
            throws IOException {
        boolean result = true;
        if (file.isDirectory()) {
            if (!skipDirectory(file)) {
                for (File f : file.listFiles()) {
                    result = doScan(f, recognizer, action);
                    if (!result)
                        break;
                }
            }
        } else {
            final FileWrapper fw = new FileWrapper(file);
            List<Block> fileBlocks = recognizer.getParser(fw).parseFile(fw);
            result = action.evaluate(fileBlocks) ;
        }
        return result;
    }



    private boolean skipDirectory( final File file ) {
	for (String pattern : patternsToSkip) {
	    String absPath = file.getAbsolutePath() ;
	    if (match( pattern, absPath)) {
		if (verbose > 0)
		    System.out.println( "Scanner: Skipping directory "
			+ absPath + "(pattern " + pattern + ")" ) ;
		return true ;
	    }
	}

	if (verbose > 0)
	    System.out.println( "Scanner: Not skipping directory " + file ) ;
	return false ;
    }

    // This where we could support more complex pattern matches, if desired.
    private boolean match( final String pattern, final String fname ) {
	final String separator = File.separator ;

	// Don't use String.split here because its argument is a regular expression,
	// and some file separator characters could be confused with regex meta-characters.
	final StringTokenizer st = new StringTokenizer( fname, separator ) ;
	while (st.hasMoreTokens()) {
	    final String token = st.nextToken() ;
	    if (pattern.equals( token )) {
		if (verbose > 0)
		    System.out.println( "fname " + fname
			+ " matched on pattern " + pattern ) ;
		return true ;
	    }
	}

	return false ;
    }
}
