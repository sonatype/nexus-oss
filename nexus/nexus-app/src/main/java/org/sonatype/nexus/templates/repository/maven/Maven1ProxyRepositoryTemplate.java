package org.sonatype.nexus.templates.repository.maven;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven1.M1RepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;

public class Maven1ProxyRepositoryTemplate
    extends AbstractMavenRepositoryTemplate
{
    public Maven1ProxyRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description,
                                          RepositoryPolicy repositoryPolicy )
    {
        super( provider, id, description, new Maven1ContentClass(), MavenProxyRepository.class, repositoryPolicy );
    }

    public M1RepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M1RepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( Repository.class.getName() );
        repo.setProviderHint( "maven1" );

        repo.setRemoteStorage( new CRemoteStorage() );
        repo.getRemoteStorage().setProvider( CommonsHttpClientRemoteStorage.PROVIDER_STRING );
        repo.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        M1RepositoryConfiguration exConf = new M1RepositoryConfiguration( ex );
        // huh? see initConfig classes
        if ( getRepositoryPolicy() != null )
        {
            exConf.setRepositoryPolicy( getRepositoryPolicy() );
        }

        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
        repo.setNotFoundCacheTTL( 1440 );
        
        if ( exConf.getRepositoryPolicy() != null && exConf.getRepositoryPolicy() == RepositoryPolicy.SNAPSHOT )
        {
            exConf.setArtifactMaxAge( 1440 );
        }
        else
        {
            exConf.setArtifactMaxAge( -1 );
        }

        CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration(
                                              getTemplateProvider().getApplicationConfiguration(),
                                              repo,
                                              new CRepositoryExternalConfigurationHolderFactory<M1RepositoryConfiguration>()
                                              {
                                                  public M1RepositoryConfiguration createExternalConfigurationHolder(
                                                                                                                      CRepository config )
                                                  {
                                                      return new M1RepositoryConfiguration( (Xpp3Dom) config
                                                          .getExternalConfiguration() );
                                                  }
                                              } );

        return result;
    }
}
