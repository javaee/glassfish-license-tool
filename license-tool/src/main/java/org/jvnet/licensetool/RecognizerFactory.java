package org.jvnet.licensetool;

import static org.jvnet.licensetool.Constants.*;

import java.io.IOException;

public class RecognizerFactory {
    public FileRecognizer getDefaultRecognizer() {
        CompositeRecognizer recognizer = new CompositeRecognizer();
        // Configure the recognizer

        // Java
        for (String suffix : JAVA_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix, createRecognizer(suffix, JAVA_COMMENT_START, JAVA_COMMENT_END,JAVA_COMMENT_PREFIX));
        }

        // Java line
	    for (String suffix : JAVA_LINE_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix, createRecognizer(suffix, JAVA_LINE_PREFIX));
	    }

        // XML
        for (String suffix : XML_LIKE_SUFFIXES) {
            recognizer.addRecognizer(suffix, createRecognizer(suffix, XML_COMMENT_START, XML_COMMENT_END, XML_COMMENT_PREFIX));
        }

        // Scheme
	    for (String suffix : SCHEME_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SCHEME_PREFIX));
	    }

        // Shell
	    for (String suffix : SHELL_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SHELL_PREFIX));
	    }

        for (String suffix : MAKEFILE_NAMES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SHELL_PREFIX));
	    }

        for (String suffix : SHELL_SCRIPT_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, createRecognizer(suffix, SHELL_PREFIX));
	    }
	    
	    // Binary
	    for (String suffix : BINARY_LIKE_SUFFIXES) {
		    recognizer.addRecognizer(suffix, null);
	    }

	    for (String suffix : IGNORE_FILE_NAMES) {
		    recognizer.addRecognizer(suffix, null);
	    }

        recognizer.addRecognizer(createShellContentRecognizer());
        return recognizer;
    }

    FileSuffixRecognizer createRecognizer(String suffix, String start, String end, String prefix) {
        return new FileSuffixRecognizer(suffix, new FileParser.BlockCommentFileParser(start, end, prefix));
    }

    FileSuffixRecognizer createRecognizer(String suffix, String prefix) {
        return new FileSuffixRecognizer(suffix, new FileParser.LineCommentFileParser(prefix));
    }

    FileContentRecognizer createShellContentRecognizer() {
        return new FileContentRecognizer() {

            public FileParser getParser(FileWrapper file) {
                if (isShellFile(file)) {
                    return new FileParser.LineCommentFileParser(SHELL_PREFIX);
                }
                return null;
            }

            private boolean isShellFile(FileWrapper file) {
                try {
                    // see if this is a shell script
                    file.open(FileWrapper.OpenMode.READ);
                    final String str = file.readLine();
                    if ((str != null) && str.startsWith("#!")) {
                        return true;
                    }
                    file.close();
                } catch (IOException exc) {
                    // action is still null
                    System.out.println("Could not read file " + file + " to check for shell script");
                }
                return false;
            }
        };

    }


}
