package org.jvnet.licensetool;

import java.io.*;

/** File wrapper for text files.  Makes it really easy to open, close, delete, read, and write
 * text files.
 */
public class FileWrapper implements Closeable {
    // java.io is a pain to deal with for text files.  We basically need to
    // create:
    // (for reading) File->FileInputStream->InputStreamReader->BufferedReader
    // (for writing) File->FileOutputStream->OutputStreamWriter->BufferedWriter
    private final File file ;

    private FileInputStream fis ;
    private InputStreamReader isr ;
    private BufferedReader reader ;

    private FileOutputStream fos ;
    private OutputStreamWriter osw ;
    private BufferedWriter writer ;


    public enum FileState { CLOSED, OPEN_FOR_READ, OPEN_FOR_WRITE } ;

    private FileWrapper.FileState state ;

    /** Create a new FileWrapper for the given File.  Represents the same file in the
     * filesystem as the underlying File object.  getBase() return the FileWrapper
     * for the file system root.
     */
    public FileWrapper( final File file ) {
	this.file = file ;

	this.fis = null ;
	this.isr = null ;
	this.reader = null ;

	this.fos = null ;
	this.osw = null ;
	this.writer = null ;

	this.state = FileWrapper.FileState.CLOSED ;
    }

    public FileWrapper( String str ) {
	this( new File( str ) ) ;
    }

    public FileWrapper( File root, String str ) {
	this( new File( root, str ) ) ;
    }

    public boolean canWrite() {
	return file.canWrite() ;
    }

    public String toString() {
	return file.toString() ;
    }

    /** Returns true if either this FileWrapper does not exist,
     * or if the lastModificationTime of this FileWrapper is earlier
     * than that of fw.
     */
    boolean isYoungerThan( FileWrapper fw ) {
	if (file.exists()) {
	    return file.lastModified() < fw.file.lastModified() ;
	}

	return true ;
    }

    public void delete() {
	file.delete() ;
    }

    public String getName() {
	return file.getName() ;
    }

    public String getAbsoluteName() {
	return file.getAbsolutePath() ;
    }

    /** Read the next line from the text file.
     * File state must be FileState OPEN_FOR_READ.
     * Returns null when at the end of file.
     */
    public String readLine() throws IOException {
	if (state != FileWrapper.FileState.OPEN_FOR_READ)
	    throw new IOException( file + " is not open for reading" ) ;

	return reader.readLine() ;
    }

    /** Write the line to the end of the file, including a newline.
     * File state must be FileState OPEN_FOR_WRITE.
     */
    public void writeLine( final String line ) throws IOException {
	if (state != FileWrapper.FileState.OPEN_FOR_WRITE)
	    throw new IOException( file + " is not open for writing" ) ;

	writer.write( line, 0, line.length() ) ;
	writer.newLine() ;
    }

    /** Close the file, and set its state to CLOSED.
     * This method does not throw any exceptions.
     */
    public void close() {
	try {
	    // Ignore if already closed
	    if (state == FileWrapper.FileState.OPEN_FOR_READ) {
		reader.close() ;
		isr.close() ;
		fis.close() ;
	    } else if (state == FileWrapper.FileState.OPEN_FOR_WRITE) {
		writer.close() ;
		osw.close() ;
		fos.close() ;
	    }
	    state = FileWrapper.FileState.CLOSED ;
	} catch (IOException exc) {
	    // Ignore stupid close IOException
	}
    }

    public enum OpenMode { READ, WRITE } ;

    /** Open the (text) file for I/O.  There are three modes:
     * <ul>
     * <li>READ.  In this mode, the file is prepared for reading,
     * starting from the beginning.
     * end-of-file at the time the file is opened.
     * <li>WRITE.  In this mode, the file is prepared for writing,
     * starting at the end of the file.
     * </ul>
     */
    public void open( final FileWrapper.OpenMode mode ) throws IOException {
	if (state== FileWrapper.FileState.CLOSED) {
	    if (mode == FileWrapper.OpenMode.READ) {
		fis = new FileInputStream( file ) ;
		isr = new InputStreamReader( fis ) ;
		reader = new BufferedReader( isr ) ;
		state = FileWrapper.FileState.OPEN_FOR_READ ;
	    } else {
		fos = new FileOutputStream( file, true ) ;
		osw = new OutputStreamWriter( fos ) ;
		writer = new BufferedWriter( osw ) ;
		state = FileWrapper.FileState.OPEN_FOR_WRITE ;
	    }
	} else if (state== FileWrapper.FileState.OPEN_FOR_READ) {
	    if (mode != FileWrapper.OpenMode.READ)
		throw new IOException( file + " is already open for reading, cannot open for writing" ) ;
	} else {
	    // state is OPEN_FOR_WRITE
	    if (mode != FileWrapper.OpenMode.WRITE)
		throw new IOException( file + " is already open for writing, cannot open for reading" ) ;
	}
    }

    public FileWrapper.FileState getFileState() {
	return state ;
    }

    /** Copy this file to target using buffer to hold data.
     * Does not assume we are using text files.
     */
    public void copyTo( FileWrapper target, byte[] buffer ) throws IOException {
	FileInputStream fis = null ;
	FileOutputStream fos = null ;

	try {
	    fis = new FileInputStream( this.file ) ;
	    fos = new FileOutputStream( target.file ) ;
	    int dataRead = fis.read( buffer ) ;
	    while (dataRead > 0) {
		fos.write( buffer, 0, dataRead ) ;
		dataRead = fis.read( buffer ) ;
	    }
	} finally {
	    if (fis != null)
		fis.close() ;
	    if (fos != null)
		fos.close() ;
	}
    }

    public String getSuffix() {
        final int dotIndex = file.getName().lastIndexOf('.');
        if (dotIndex >= 0)
            return file.getName().substring(dotIndex + 1);
        return null;
    }

}
