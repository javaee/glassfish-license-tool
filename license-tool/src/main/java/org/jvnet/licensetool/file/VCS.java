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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
* @author Rama Pulavarthi
*/
public enum VCS {
    CVS {
        public String getLastModifiedYear(String f) {
            String workingRev = "Working revision:";
            String dateInfo = "date: ";
            String year = null;
            try {
                String output = executeExternalCommand(new File(f).getParentFile(), "cvs", "status", new File(f).getName());
                String[] lines = output.split("[\\r\\n]+");
                String rev = null;
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith(workingRev)) {
                        rev = line.substring(workingRev.length()).trim();
                        break;
                    }
                }
                if (rev != null && rev.matches("([0-9\\.]+)")) {
                    //valid revision
                    String output1 = executeExternalCommand(new File(f).getParentFile(), "cvs", "log", "-r" + rev, "-N", new File(f).getName());
                    String[] lines1 = output1.split("[\\r\\n]+");

                    for (String line : lines1) {
                        line = line.trim();
                        if (line.startsWith(dateInfo)) {
                            year = line.substring(dateInfo.length(), dateInfo.length() + 4);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return year;
        }
    },

    SVN {
        public String getLastModifiedYear(String f) {
            String lastChanged = "Last Changed Date: ";
            String output = "";
            try {
                output = executeExternalCommand(null, "svn", "info", f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] lines = output.split("[\\r\\n]+");
            String year = null;
            for (String line : lines) {
                if (line.startsWith(lastChanged)) {
                    year = line.substring(lastChanged.length(), lastChanged.length() + 4);
                    break;
                }
            }
            return year;
        }
    },

    HG {
        public String getLastModifiedYear(String f) {
            String output = "";
            try {
                output = executeExternalCommand(new File(f).getParentFile(), "hg", "log", "--limit", "1",
                        "--template", "{date|shortdate}", f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (output != null && !output.equals("")) {
                output = output.substring(0, 4);
                if(output.matches("[\\d]{4}")) {
                    return output;
                }
            }
            return null;
        }
    };

    public abstract String getLastModifiedYear(String f);

    private static String executeExternalCommand(File dir, String... args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        if (dir != null) {
            pb.directory(dir);
        }
        Process process = pb.start();
        process.getOutputStream().close();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line + "\n");
        }
        process.getInputStream().close();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
        }
        return sb.toString();
    }

    public static VCS sniffVCS(File f) {
        if(!f.isDirectory()) {
            sniffVCS(f.getParentFile());
        }
        if(checkVCSinDir(f, "CVS")) {
            return VCS.CVS;
        } else if(checkVCSinDir(f, ".svn")) {
            return VCS.SVN;
        } else if(checkVCSinRoot(f,".hg")) {
            return VCS.HG;
        }
        return null;
    }

    private static boolean checkVCSinDir(File dir, final String vcs)  {
        File vcsDir = new File(dir,vcs);
        if(vcsDir.exists()) {
            return true;
        }
        return false;
    }

    private static boolean checkVCSinRoot(File dir, final String vcs) {
        File vcsDir = new File(dir,vcs);
        if(vcsDir.exists()) {
            return true;
        }
        File parent = dir.getParentFile();
        if(parent != null) {
            return checkVCSinRoot(parent,vcs);
        }
        return false;
    }
}
