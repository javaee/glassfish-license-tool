/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
    public static final String XML_COMMENT_PREFIX = "";
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
    public static final String SHELL_PREFIX = "#";

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
