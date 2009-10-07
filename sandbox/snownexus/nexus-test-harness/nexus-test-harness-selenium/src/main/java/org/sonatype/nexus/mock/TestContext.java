package org.sonatype.nexus.mock;

import java.io.File;

import org.apache.log4j.Logger;

public class TestContext
{

    protected static Logger log = Logger.getLogger( TestContext.class );

    public static final File RESOURCES_DIR = new File( "target/resources" );

    public static final File RESOURCES_SOURCE_DIR = new File( "resources" );

    private static final ThreadLocal<String> testId = new ThreadLocal<String>();

    public static String getTestId()
    {
        if ( testId.get() == null )
        {
            throw new NullPointerException( "TestId undefined!" );
        }
        return testId.get();
    }

    public static void setTestId( String testId )
    {
        TestContext.testId.set( testId );
    }

    public static File getTestResourceAsFile( String relativePath )
    {
        String resource = getTestId() + "/" + relativePath;
        return getResource( resource );
    }

    public static File getResource( String resource )
    {
        log.debug( "Looking for resource: " + resource );

        File file = new File( RESOURCES_DIR, resource );

        if ( !file.exists() )
        {
            return null;
        }

        log.debug( "found: " + file );

        return file.getAbsoluteFile();
    }

    @Deprecated
    public static File getTestFile( String relativePath )
    {
        return getFile( relativePath );
    }

    public static File getFile( String relativePath )
    {
        return getTestResourceAsFile( "files/" + relativePath );
    }

}
