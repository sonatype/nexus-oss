package org.sonatype.sample.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.plexus.util.FileUtils;

public class WrapperEditorTest
    extends TestCase
{
    protected WrapperEditor prepareCase( String caseNum )
        throws IOException
    {
        File source = new File( "target/test-classes/c01/wrapper.conf" );

        FileUtils.copyFile( new File( "src/test/resources/c01/wrapper.conf" ), source );

        return WrapperHelper.getWrapperEditor( source );
    }

    protected void validateCase( String caseNum )
        throws IOException
    {
        File source = new File( "target/test-classes/" + caseNum + "/wrapper.conf" );

        File expectedResult = new File( "target/test-classes/" + caseNum + "/wrapper.conf.result" );

        assertEquals( "wrapper.conf does not match!", FileUtils.loadFile( expectedResult ), FileUtils.loadFile( source ) );
    }

    public void testCase01()
        throws Exception
    {
        WrapperEditor editor = prepareCase( "c01" );

        assertEquals( "The startup timeout does not match!", 90, editor.getWrapperStartupTimeout() );

        Map<String, String> allKeyValuePairs = editor.getWrapperConfWrapper().getAllKeyValuePairs();

        assertEquals( "Not all entries are read!", 20, allKeyValuePairs.size() );

        editor.setWrapperStartupTimeout( 130 );

        String mainClass = editor.getWrapperJavaMainclass();

        assertEquals( "Main class does not match!", "org.sonatype.sample.wrapper.ApplicationA", mainClass );

        editor.setWrapperJavaMainclass( mainClass.substring( 0, mainClass.length() - 1 ) + "B" );

        editor.save();

        validateCase( "c01" );
    }
}
