package org.jvnet.licensetool;

public class Constants {
    public static final String[] JAVA_LIKE_SUFFIXES = {
            "c", "h", "java", "sjava", "idl"};
    public static final String JAVA_COMMENT_START = "/*";
    public static final String JAVA_COMMENT_PREFIX = " *";
    public static final String JAVA_COMMENT_END = "*/";

    public static final String[] XML_LIKE_SUFFIXES = {
            "htm", "html", "xml", "dtd", "rng", "xsd", "sxd", "vsd"};
    public static final String XML_COMMENT_START = "<!--";
    public static final String XML_COMMENT_PREFIX = " ";
    public static final String XML_COMMENT_END = "-->";

    public static final String[] JAVA_LINE_LIKE_SUFFIXES = {
            "tdesc", "policy", "secure"};
    public static final String JAVA_LINE_PREFIX = "// ";

    public static final String[] SCHEME_LIKE_SUFFIXES = {
            "mc", "mcd", "scm", "vthought"};
    public static final String SCHEME_PREFIX = "; ";

    // Shell scripts must always start with #! ..., others need not.
    public static final String[] SHELL_SCRIPT_LIKE_SUFFIXES = {
            "ksh", "sh"};
    public static final String[] SHELL_LIKE_SUFFIXES = {
            "classlist", "config", "jmk", "properties", "prp", "xjmk", "set",
            "data", "txt", "text"};
    public static final String SHELL_PREFIX = "# ";

    // Files whose names match these also use the SHELL_PREFIX style line comment.
    public static final String[] MAKEFILE_NAMES = {
            "Makefile.corba", "Makefile.example", "ExampleMakefile", "Makefile"};

    public static final String[] BINARY_LIKE_SUFFIXES = {
            "sxc", "sxi", "sxw", "odp", "gif", "png", "jar", "zip", "jpg", "pom",
            "pdf", "doc", "mif", "fm", "book", "zargo", "zuml", "cvsignore",
            "hgignore", "list", "old", "orig", "rej", "swp", "swo", "class", "o",
            "javaref", "idlref", "css", "DS_Store", "jj"};

    // Special file names to ignore
    public static final String[] IGNORE_FILE_NAMES = {
            "NORENAME", "errorfile", "sed_pattern_file.version"
    };
}
