package org.sonatype.nexus.proxy.utils;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.registry.validation.FileTypeValidationUtil;

public class FileTypeValidationUtilTest extends PlexusTestCase
{
    private FileTypeValidationUtil getValidationUtil() throws Exception
    {
        return this.lookup( FileTypeValidationUtil.class );
    }

    public void testJar() throws Exception
    {        
        doTest( "something/else/myapp.jar", "test.jar", true );
        doTest( "something/else/myapp.zip", "test.jar", true );
        doTest( "something/else/myapp.war", "test.jar", true );
        doTest( "something/else/myapp.ear", "test.jar", true );
        doTest( "something/else/myapp.jar", "error.html", false );
    }
    
    public void testPom() throws Exception
    {
        doTest( "something/else/myapp.pom", "no-doctype-pom.xml", true );
        doTest( "something/else/myapp.pom", "simple.xml", false );
        doTest( "something/else/myapp.pom", "pom.xml", true );
        doTest( "something/else/myapp.xml", "pom.xml", true );
        doTest( "something/else/myapp.xml", "simple.xml", true );
        doTest( "something/else/myapp.xml", "error.html", false );
    }
    
    private void doTest( String expectedFileName, String testFileName, boolean expectedResult ) throws Exception
    {
        File testFile = new File( "target/test-classes/FileTypeValidationUtilTest", testFileName );
        
        FileInputStream fis = null;
        
        try
        {
            fis = new FileInputStream( testFile );
            boolean result = getValidationUtil().isExpectedFileType( fis, expectedFileName );
            Assert.assertEquals( "File name: "+ expectedFileName + " and file: "+ testFileName + " match result: " + result + " expected: "+ expectedResult , expectedResult, result );
        }
        finally
        {
            IOUtil.close( fis );
        }
    }
    
}
