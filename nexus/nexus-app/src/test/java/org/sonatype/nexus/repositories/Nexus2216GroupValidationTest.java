package org.sonatype.nexus.repositories;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

public class Nexus2216GroupValidationTest
    extends AbstractNexusTestCase
{
    // we need some stuff to prepare
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    // ==

    public void testInvertedOrdering()
        throws Exception
    {
        // mangle config
        mangleConfiguration();

        try
        {
            // lookup nexus, this will do all sort of things, amongst them validate the config
            lookup( Nexus.class );

            RepositoryRegistry repositoryRegistry = lookup( RepositoryRegistry.class );

            MavenGroupRepository publicGroup =
                repositoryRegistry.getRepositoryWithFacet( "public", MavenGroupRepository.class );

            assertEquals( "The config should be correct", 4, publicGroup.getMemberRepositories().size() );
        }
        catch ( Exception e )
        {
            fail( "Should succeed!" );
        }
    }

    // ==

    protected void mangleConfiguration()
        throws IOException, XmlPullParserException
    {
        // copy the defaults
        copyDefaultConfigToPlace();

        File configFile = new File( getNexusConfiguration() );

        // raw load the config file in place
        FileReader fileReader = new FileReader( configFile );

        NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

        Configuration config = reader.read( fileReader );

        fileReader.close();

        CRepository publicGroup = null;

        // simple put the "public" group (that reference other reposes) as 1st!
        for ( CRepository repository : config.getRepositories() )
        {
            if ( "public".equals( repository.getId() ) )
            {
                publicGroup = repository;

                break;
            }
        }

        if ( publicGroup == null )
        {
            fail( "Public group not found in default configuration?" );
        }

        config.getRepositories().remove( publicGroup );

        config.getRepositories().add( 0, publicGroup );

        // raw save the modified config
        FileWriter fileWriter = new FileWriter( configFile );

        NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();

        writer.write( fileWriter, config );

        fileWriter.flush();

        fileWriter.close();
    }
}
