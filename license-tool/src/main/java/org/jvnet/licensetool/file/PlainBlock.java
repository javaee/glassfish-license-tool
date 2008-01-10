package org.jvnet.licensetool.file;

import org.jvnet.licensetool.generic.Pair;
import java.util.*;
import java.io.IOException;

/**
 * Represents a portion of file as a list of lines.
 *
 */
public class PlainBlock extends Block {

        private List<String> data;

        public PlainBlock(final List<String> data, final Set<String> tags) {
            super(tags);
            this.data = data;
        }

        /**
         * Create a new PlainBlock from a list of strings.
         */
        public PlainBlock(final List<String> data) {
            this.data = data;
        }

        public List<String> contents() {
            return data;
        }

        public void write(FileWrapper fw) throws IOException {
            for (String str : data) {
                fw.writeLine(str);
            }
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof Block))
                return false;

            PlainBlock block = (PlainBlock) obj;

            // Equal if contents are equal; we ignore the tags
            Iterator<String> iter1 = data.iterator();
            Iterator<String> iter2 = block.data.iterator();
            while (iter1.hasNext() && iter2.hasNext()) {
                String str1 = iter1.next();
                String str2 = iter2.next();
                if (!str1.equals(str2))
                    return false;
            }

            return iter1.hasNext() == iter2.hasNext();
        }

        public int hashCode() {
            int hash = 0;
            for (String str : data)
                hash ^= data.hashCode();
            return hash;
        }

        /**
         * replace all occurrences of @KEY@ with parameters.get( KEY ).
         * This is very simple: only one scan is made, so @...@ patterns
         * in the parameters values are ignored.
         */
        public PlainBlock instantiateTemplate(Map<String, String> parameters) {
            final List<String> result = new ArrayList<String>(data.size());
            for (String str : data) {
                final StringBuilder sb = new StringBuilder();
                final StringTokenizer st = new StringTokenizer(str, "@");

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

                result.add(sb.toString());
            }

            return new PlainBlock(result);
        }

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

        /**
         * Split block into two blocks, with only the
         * first line of the original Block in result.first().
         */
        public Pair<Block, Block> splitFirst() {
            List<String> first = new ArrayList<String>();
            List<String> rest = new ArrayList<String>();
            for (String str : contents()) {
                if (first.size() == 0) {
                    first.add(str);
                } else {
                    rest.add(str);
                }
            }

            Block block1 = new PlainBlock(first, new HashSet<String>(tags));
            Block block2 = new PlainBlock(rest, new HashSet<String>(tags));
            Pair<Block, Block> result = new Pair<Block, Block>(block1, block2);

            return result;
        }
    }