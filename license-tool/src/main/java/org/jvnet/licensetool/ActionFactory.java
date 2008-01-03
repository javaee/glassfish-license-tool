package org.jvnet.licensetool;

public class ActionFactory {
    private final int verbose ;
    private final boolean dryRun ;

    public ActionFactory() {
        this(0, false);
    }

    public ActionFactory(final int verbose) {
        this(verbose, false);
    }

    public ActionFactory(final int verbose, final boolean dryRun) {
        this.verbose = verbose;
        this.dryRun = dryRun;
    }

    /** returns an action that returns true.  If verbose is true, the action
     * also displays the FileWrapper that was passed to it.
     */
    public Scanner.Action getSkipAction() {
	return new Scanner.Action() {
	    public String toString() {
		return "SkipAction" ;
	    }

	    public boolean evaluate(ParsedFile arg ) {
		if (verbose > 0)
		    System.out.println( toString() + "called") ;

		return true ;
	    }

        } ;
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
                if (verbose > 0)
                    System.out.println(toString() + "called.");
                return false;
            }
        };
    }

}