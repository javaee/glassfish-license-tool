package org.jvnet.licensetool;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class CompositeRecognizer implements FileRecognizer{
    Map<String, FileRecognizer> suffixRecognizers = new HashMap<String, FileRecognizer>();
    List<FileRecognizer> contentRecognizers = new ArrayList<FileRecognizer>();

    public FileParser getParser(FileWrapper file) {
        FileRecognizer recognizer = suffixRecognizers.get(file.getSuffix());
        if(recognizer != null) {
            recognizer.getParser(file);
        }
        for(FileRecognizer r: contentRecognizers ) {
            FileParser bp = r.getParser(file);
            if( bp!= null) {
                return bp;
            }
        }
        return null;
    }

    public void addRecognizer(FileRecognizer recognizer) {
        contentRecognizers.add(recognizer);
    }

    public void addRecognizer(String suffix, FileRecognizer recognizer) {
        suffixRecognizers.put(suffix, recognizer);
    }
}
