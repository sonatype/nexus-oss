package org.sonatype.appcontext;

import java.io.File;

import junit.framework.TestCase;

public class AppContextFactoryTest
    extends TestCase
{
    public void testC01()
        throws Exception
    {
        AppContextFactory ctxFactory = new AppContextFactory();

        // redirect the default basedir key name, since maven uses the same!
        ctxFactory.getAppContextHelper().getConfiguration().setBasedirPropertyKey( "c01.basedir" );

        // set this property (is a must!)
        System.setProperty( ctxFactory.getAppContextHelper().getConfiguration().getBasedirPropertyKey(), new File(
            "src/test/resources/c01" ).getAbsolutePath() );

        // add some "other" (like user defined) system properties
        System.setProperty( "c01.blah", "tooMuchTalk!" );

        AppContextRequest request = ctxFactory.getDefaultAppContextRequest();

        request.setName( "c01" );

        // create a properties filler for plexus.properties, that will fail if props file not found
        PropertiesFileContextFiller plexusPropertiesFiller =
            new PropertiesFileContextFiller( new File( ctxFactory.getBasedir(), "plexus.properties" ), true );

        // add it to fillers as very 1st resource, and leaving others in
        request.getContextFillers().add( 0, plexusPropertiesFiller );

        AppContext appContext = ctxFactory.getAppContext( request );

        assertEquals( 5, appContext.size() );
    }

}
