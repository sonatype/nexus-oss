package org.sonatype.appcontext;

import java.io.File;

import junit.framework.TestCase;

import org.sonatype.appcontext.source.LegacyBasedirEntrySource;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;

public class SimpleTest
    extends TestCase
{
    public void testC01()
        throws Exception
    {
        // Set this to have it "catched"
        System.setProperty( "c01.blah", "tooMuchTalk!" );
        System.setProperty( "c01.basedir", new File( "src/test/resources/c01" ).getAbsolutePath() );

        AppContextRequest request = Factory.getDefaultRequest( "c01" );
        request.getSources().add( new LegacyBasedirEntrySource( "c01.basedir", true ) );
        request.getSources().add(
            new PropertiesFileEntrySource( new File( "src/test/resources/c01/plexus.properties" ) ) );

        AppContext appContext = Factory.create( request );

        assertEquals( 5, appContext.size() );
    }

}
