package org.sonatype.appcontext;

import java.io.File;

import junit.framework.TestCase;

public class AppContextFactoryTest
    extends TestCase
{
    public void testC01()
        throws Exception
    {
        // 1st, set up some default-like basedir discoverer, but "redirect" it from default to some testy
        DefaultBasedirDiscoverer basedirDiscoverer = new DefaultBasedirDiscoverer();
        basedirDiscoverer.setBasedirKey( "c01.basedir" );

        AppContextFactory ctxFactory = new AppContextFactory();

        // set this property (is a must!)
        System.setProperty( basedirDiscoverer.getBasedirKey(), new File( "src/test/resources/c01" ).getAbsolutePath() );

        // add some "other" (like user defined) system properties
        System.setProperty( "c01.blah", "tooMuchTalk!" );

        AppContextRequest request = ctxFactory.getDefaultAppContextRequest();

        // customize request
        request.setName( "c01" );
        request.setBasedirDiscoverer( basedirDiscoverer );

        // create a properties filler for plexus.properties, that will fail if props file not found
        // note that the File passed in is relative, hence the basedir will be used to locate it!
        PropertiesFileContextFiller plexusPropertiesFiller =
            new PropertiesFileContextFiller( new File( "plexus.properties" ), true );

        // add it to fillers as very 1st resource, and leaving others in
        request.getContextFillers().add( 0, plexusPropertiesFiller );

        AppContext appContext = ctxFactory.getAppContext( request );

        assertEquals( 5, appContext.size() );
    }

}
