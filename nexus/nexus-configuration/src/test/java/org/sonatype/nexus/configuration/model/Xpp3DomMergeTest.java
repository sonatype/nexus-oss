package org.sonatype.nexus.configuration.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.util.StringDigester;

public class Xpp3DomMergeTest
    extends PlexusTestCase
{
    private static final String XML_BASE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><externalConfiguration></externalConfiguration>";
    
    public void testMergeOfCollection()
        throws Exception
    {
        List<String> empty = Collections.emptyList();
        
        SimpleXpp3ConfigHolder aHolder = new SimpleXpp3ConfigHolder( XML_BASE );
        SimpleXpp3ConfigHolder bHolder = new SimpleXpp3ConfigHolder( XML_BASE );       
        
        aHolder.setCollection( bHolder.getRootNode(), "memberRepositories", empty );
        aHolder.addToCollection( aHolder.getRootNode(), "memberRepositories", "central-m1", true );
        aHolder.addToCollection( aHolder.getRootNode(), "memberRepositories", "m1h", true );
        aHolder.addToCollection( aHolder.getRootNode(), "memberRepositories", "m1p", true );
        
        bHolder.setCollection( bHolder.getRootNode(), "memberRepositories", empty );
        bHolder.addToCollection( bHolder.getRootNode(), "memberRepositories", "central-m1", true );
        bHolder.addToCollection( bHolder.getRootNode(), "memberRepositories", "m1h", true );
        bHolder.addToCollection( bHolder.getRootNode(), "memberRepositories", "m1p", true );
        
        bHolder.removeFromCollection( bHolder.getRootNode(), "memberRepositories", "m1p" );
        
        aHolder.apply( bHolder );
        
        SimpleXpp3ConfigHolder resultHolder = new SimpleXpp3ConfigHolder( XML_BASE );
        resultHolder.setCollection( resultHolder.getRootNode(), "memberRepositories", empty );
        resultHolder.addToCollection( resultHolder.getRootNode(), "memberRepositories", "central-m1", true );
        resultHolder.addToCollection( resultHolder.getRootNode(), "memberRepositories", "m1h", true );
        
        assertTrue( resultHolder.getRootNode().equals( aHolder.getRootNode() ) ); 
    }
    
    private static class SimpleXpp3ConfigHolder
        extends AbstractXpp3DomExternalConfigurationHolder
    {
        public SimpleXpp3ConfigHolder( String xml ) 
            throws XmlPullParserException, 
                IOException
        {
            super( Xpp3DomBuilder.build( new StringReader( xml ) ) );
        }
        
        @Override
        public void validate( ApplicationConfiguration applicationConfiguration, CoreConfiguration owner )
            throws ConfigurationException
        {   
        }
    }
}
