package org.jvnet.licensetool.file;

import org.jvnet.licensetool.generic.Pair;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

/**
 * Represents a portion of file as a list of lines.
 */
public class PlainBlock extends Block {

    private String data;

    public PlainBlock(final String data, final Set<String> tags) {
        super(tags);
        this.data = data;
    }

    /**
     * Create a new PlainBlock from a list of strings.
     */
    public PlainBlock(final String data) {
        this.data = data;
    }

    /**
     * Return the contents of the text file as a PlainBlock.
     */
    public PlainBlock(final FileWrapper fw) throws IOException {
        fw.open(FileWrapper.OpenMode.READ);
        try {
            data = fw.readAsString();
        } finally {
            fw.close();
        }
    }

    public String contents() {
        return data;
    }

    public void write(FileWrapper fw) throws IOException {
        fw.write(data);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof PlainBlock))
            return false;

        PlainBlock block = (PlainBlock) obj;


        String objdata = block.data;
        return(data.equals(objdata));
    }

    public int hashCode() {
        return data.hashCode();
    }

    /**
     * replace all occurrences of @KEY@ with parameters.get( KEY ).
     * This is very simple: only one scan is made, so @...@ patterns
     * in the parameters values are ignored.
     */
    public PlainBlock instantiateTemplate(Map<String, String> parameters) {

        final StringBuilder sb = new StringBuilder();
        final StringTokenizer st = new StringTokenizer(data, "@");

        // Note that the pattern is always TEXT@KEY@TEXT@KEY@TEXT,
        // so the the first token is not a keyword, and then the tokens
        // alternate.
        boolean isKeyword = false;
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            final String replacement =
                    isKeyword ? parameters.get(token) : token;
            sb.append(replacement);
            isKeyword = !isKeyword;
        }
        return new PlainBlock(sb.toString());
    }

    /*
    public Block substitute(List<Pair<String, String>> substitutions) {
        List<String> result = new ArrayList<String>();
        for (String line : data) {
            String newLine = line;
            for (Pair<String, String> pair : substitutions) {
                String pattern = pair.first();
                String replacement = pair.second();
                newLine = newLine.replace(pattern, replacement);
            }
            result.add(newLine);
        }
        return new PlainBlock(result);
    }
    */
    /**
     * Split block into two blocks, with only the
     * first line of the original Block in result.first().
     */
    public Pair<Block, Block> splitFirst() {
        //String patternStr = "(?m)(.*)[\\r]?[\\n]?";
        String patternStr = "(.+?)^";

        Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);
        String fline;
        String rest = "";
        if (matcher.find()) {
            fline = matcher.group();
            rest = data.substring(matcher.end());
        } else {
            fline = data;
        }
        return new Pair<Block, Block>(
                new PlainBlock(fline, tags), new PlainBlock(rest, tags));

    }
}