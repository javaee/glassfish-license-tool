package org.jvnet.licensetool;

/**
 * This class marks as a tag and FileContentRecognizer identifies a file based ont he contents of the file.
 *
 */
public class FileContentRecognizer implements FileRecognizer{
    
    public FileParser getParser(FileWrapper file) {
        return null;
    }
}
