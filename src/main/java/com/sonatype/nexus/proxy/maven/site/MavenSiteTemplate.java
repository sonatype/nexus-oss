package com.sonatype.nexus.proxy.maven.site;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;

public class MavenSiteTemplate
    extends AbstractRepositoryTemplate
{

    public MavenSiteTemplate( MavenSiteTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new MavenSiteContentClass(), MavenSiteRepository.class );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );

        repo.setProviderRole( WebSiteRepository.class.getName() );
        repo.setProviderHint( "maven-site" );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        repo.setIndexable( false );

        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repo.setNotFoundCacheTTL( 1440 );

        CRepositoryCoreConfiguration result = new CRepositoryCoreConfiguration(
            getTemplateProvider().getApplicationConfiguration(),
            repo,
            new CRepositoryExternalConfigurationHolderFactory<DefaultMavenSiteRepositoryConfiguration>()
            {
                public DefaultMavenSiteRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
                {
                    return new DefaultMavenSiteRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
                }
            } );

        return result;
    }
}
