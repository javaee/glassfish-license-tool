package org.jvnet.licensetool;

/**
 * Recognizes files according to patterns, and gives a suitable parser.
 */

public interface FileRecognizer {
    FileParser getParser(FileWrapper file);    
}
