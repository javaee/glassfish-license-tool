/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * File wrapper for text files.  Makes it really easy to open, close, delete, read, and write
 * text files.
 */
public class FileWrapper implements Closeable {
    // java.io is a pain to deal with for text files.  We basically need to
    // create:
    // (for reading) File->FileInputStream->InputStreamReader->BufferedReader
    // (for writing) File->FileOutputStream->OutputStreamWriter->BufferedWriter
    private final File file;

    private FileInputStream fis;
    private InputStreamReader isr;
    private BufferedReader reader;

    private FileOutputStream fos;
    private OutputStreamWriter osw;
    private BufferedWriter writer;


    public enum FileState {
        CLOSED, OPEN_FOR_READ, OPEN_FOR_WRITE
    }

    private FileWrapper.FileState state;

    /**
     * Create a new FileWrapper for the given File.  Represents the same file in the
     * filesystem as the underlying File object.  getBase() return the FileWrapper
     * for the file system root.
     */
    public FileWrapper(final File file) {
        this.file = file;

        this.fis = null;
        this.isr = null;
        this.reader = null;

        this.fos = null;
        this.osw = null;
        this.writer = null;

        this.state = FileWrapper.FileState.CLOSED;
    }

    public FileWrapper(String str) {
        this(new File(str));
    }

    public FileWrapper(File root, String str) {
        this(new File(root, str));
    }

    public boolean canWrite() {
        return file.canWrite();
    }

    public String toString() {
        return file.toString();
    }

    /**
     * Returns true if either this FileWrapper does not exist,
     * or if the lastModificationTime of this FileWrapper is earlier
     * than that of fw.
     */
    boolean isYoungerThan(FileWrapper fw) {
        if (file.exists()) {
            return file.lastModified() < fw.file.lastModified();
        }

        return true;
    }

    public void delete() {
        file.delete();
    }

    public String getName() {
        return file.getName();
    }

    public String getAbsoluteName() {
        return file.getAbsolutePath();
    }

    /**
     * Read the next line from the text file.
     * File state must be FileState OPEN_FOR_READ.
     * Returns null when at the end of file.
     */
    public String readLine() throws IOException {
        if (state != FileWrapper.FileState.OPEN_FOR_READ)
            throw new IOException(file + " is not open for reading");

        return reader.readLine();
    }

    public String readAsString() throws IOException {
        if (state != FileWrapper.FileState.OPEN_FOR_READ)
            throw new IOException(file + " is not open for reading");

        int i;
        StringBuilder fileContents = new StringBuilder();
        // Read file into a String
        while ((i = reader.read()) != -1) {
            fileContents.append((char) i);

        }
        return fileContents.toString();
    }

    /**
     * Write the line to the end of the file, including a newline.
     * File state must be FileState OPEN_FOR_WRITE.
     */
    public void writeLine(final String line) throws IOException {
        if (state != FileWrapper.FileState.OPEN_FOR_WRITE)
            throw new IOException(file + " is not open for writing");

        writer.write(line, 0, line.length());
        writer.newLine();
    }

    /**
     * Write the line to the end of the file
     * File state must be FileState OPEN_FOR_WRITE.
     */
    public void write(final String line) throws IOException {
        if (state != FileWrapper.FileState.OPEN_FOR_WRITE)
            throw new IOException(file + " is not open for writing");

        writer.write(line, 0, line.length());

    }

    /**
     * Close the file, and set its state to CLOSED.
     * This method does not throw any exceptions.
     */
    public void close() {
        try {
            // Ignore if already closed
            if (state == FileWrapper.FileState.OPEN_FOR_READ) {
                reader.close();
                isr.close();
                fis.close();
            } else if (state == FileWrapper.FileState.OPEN_FOR_WRITE) {
                writer.close();
                osw.close();
                fos.close();
            }
            state = FileWrapper.FileState.CLOSED;
        } catch (IOException exc) {
            // Ignore stupid close IOException
        }
    }

    public enum OpenMode {
        READ, WRITE
    }

    /**
     * Open the (text) file for I/O.  There are three modes:
     * <ul>
     * <li>READ.  In this mode, the file is prepared for reading,
     * starting from the beginning.
     * end-of-file at the time the file is opened.
     * <li>WRITE.  In this mode, the file is prepared for writing,
     * starting at the end of the file.
     * </ul>
     */
    public void open(final FileWrapper.OpenMode mode) throws IOException {
        if (state == FileWrapper.FileState.CLOSED) {
            if (mode == FileWrapper.OpenMode.READ) {
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis);
                reader = new BufferedReader(isr);
                state = FileWrapper.FileState.OPEN_FOR_READ;
            } else {
                fos = new FileOutputStream(file, true);
                osw = new OutputStreamWriter(fos);
                writer = new BufferedWriter(osw);
                state = FileWrapper.FileState.OPEN_FOR_WRITE;
            }
        } else if (state == FileWrapper.FileState.OPEN_FOR_READ) {
            if (mode != FileWrapper.OpenMode.READ)
                throw new IOException(file + " is already open for reading, cannot open for writing");
        } else {
            // state is OPEN_FOR_WRITE
            if (mode != FileWrapper.OpenMode.WRITE)
                throw new IOException(file + " is already open for writing, cannot open for reading");
        }
    }

    public FileWrapper.FileState getFileState() {
        return state;
    }

    /**
     * Copy this file to target using buffer to hold data.
     * Does not assume we are using text files.
     */
    public void copyTo(FileWrapper target, byte[] buffer) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(this.file);
            fos = new FileOutputStream(target.file);
            int dataRead = fis.read(buffer);
            while (dataRead > 0) {
                fos.write(buffer, 0, dataRead);
                dataRead = fis.read(buffer);
            }
        } finally {
            if (fis != null)
                fis.close();
            if (fos != null)
                fos.close();
        }
    }

    public String getSuffix() {
        final int dotIndex = file.getName().lastIndexOf('.');
        if (dotIndex >= 0)
            return file.getName().substring(dotIndex + 1);
        return null;
    }

}
