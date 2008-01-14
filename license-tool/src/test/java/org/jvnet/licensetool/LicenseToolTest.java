package org.jvnet.licensetool;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit test for LicenseTool * 
 */
public class LicenseToolTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public LicenseToolTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( LicenseToolTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {   List<String> args = new ArrayList<String>();
        args.add("-validate");
        args.add("true");
        args.add("-verbose");
        args.add("true");
        args.add("-dryrun");
        args.add("false");
        args.add("-roots");
        args.add(new File(getRoot(),"target/test-classes/testsrc").getPath());
        args.add("-skipdirs");
        args.add(".svn");
        args.add("-copyright");
        args.add(new File(getRoot(),"target/test-classes/copyright/copyright.txt").getPath());
        LicenseTool.main(args.toArray(new String[0]));
    }

    private File getRoot() {
        String classnameAsResource  = this.getClass().getName().replace('.', '/') + ".class";
        URL res = getClass().getClassLoader().getResource(classnameAsResource);
        File f = new File(res.getFile());
        while(!new File(f,"pom.xml").exists())
            f = f.getParentFile();
        return f;
    }
}
