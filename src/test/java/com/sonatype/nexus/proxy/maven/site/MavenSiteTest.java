package com.sonatype.nexus.proxy.maven.site;

import java.io.FileReader;

import junit.framework.Assert;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

public class MavenSiteTest
    extends AbstractNexusTestCase
{

    public void testCreateSiteRepoWithTemplate()
        throws Exception
    {
        Nexus nexus = this.lookup( Nexus.class );

        RepositoryTemplate template = nexus.getRepositoryTemplateById( "maven-site" );

        template.getConfigurableRepository().setId( "test-site-repo" );
        template.getConfigurableRepository().setName( "Maven Sites" );

        Repository repo = template.create();

        nexus.getNexusConfiguration().saveConfiguration();

        // now check the config to see what we have
        NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();
        FileReader fileReader = null;

        Configuration config = null;
        try
        {
            fileReader = new FileReader( this.getNexusConfiguration() );
            config = reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }

        CRepository siteRepo = null;

        for ( CRepository cRepo : config.getRepositories() )
        {
            if ( cRepo.getId().equals( "test-site-repo" ) )
            {
                siteRepo = cRepo;
            }
        }
        
        Assert.assertNotNull( "Could not find the site repo in: " + this.getNexusConfiguration(), siteRepo );

        Assert.assertEquals( "Maven Sites", siteRepo.getName() );
//        Assert.assertNull( siteRepo.getExternalConfiguration() );
        
        // there are too many places that check null, so for now, this cannot be null
        Assert.assertNotNull( siteRepo.getExternalConfiguration() );

    }

}
