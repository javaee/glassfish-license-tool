package org.jvnet.licensetool;

/**
 * This class uses the file name suffix to identify the type of file. 
 */
public class FileSuffixRecognizer implements FileRecognizer{
    final String  suffix;
    final FileParser parser;
    
    public FileSuffixRecognizer(String suffix, FileParser parser) {
        this.suffix = suffix;
        this.parser = parser;
    }
    public FileParser getParser(FileWrapper file) {
        return parser;  
    }
}
