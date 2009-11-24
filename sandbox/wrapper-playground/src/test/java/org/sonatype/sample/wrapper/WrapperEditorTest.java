package org.sonatype.sample.wrapper;

import java.io.File;

import junit.framework.TestCase;

import org.codehaus.plexus.util.FileUtils;

public class WrapperEditorTest
    extends TestCase
{
    public void testCase01()
        throws Exception
    {
        File source = new File( "target/test-classes/c01/wrapper.conf" );
        File expectedResult = new File( "target/test-classes/c01/wrapper.conf.result" );

        FileUtils.copyFile( new File("src/test/resources/c01/wrapper.conf"), source );
        
        WrapperEditor editor = new DefaultWrapperEditor( WrapperHelper.getWrapperConfWrapper( source ) );

        editor.setWrapperStartupTimeout( 130 );

        String mainClass = editor.getWrapperJavaMainclass();
        assertEquals( "Main class does not match!", "org.sonatype.sample.wrapper.ApplicationA", mainClass );

        editor.setWrapperJavaMainclass( mainClass.substring( 0, mainClass.length() - 1 ) + "B" );

        editor.save();

        assertEquals( "wrapper.conf does not match!", FileUtils.loadFile( expectedResult ), FileUtils.loadFile( source ) );
    }

}
