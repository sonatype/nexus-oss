package org.sonatype.nexus.templates.repository.maven;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenShadowRepository;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;

public class Maven2Maven1ShadowRepositoryTemplate
    extends AbstractMavenRepositoryTemplate
{
    public Maven2Maven1ShadowRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id,
                                                 String description )
    {
        super( provider, id, description, new Maven2ContentClass(), MavenShadowRepository.class, null );
    }

    public M1LayoutedM2ShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M1LayoutedM2ShadowRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration()
            .getConfiguration( forWrite );
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( ShadowRepository.class.getName() );
        repo.setProviderHint( "m2-m1-shadow" );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        M1LayoutedM2ShadowRepositoryConfiguration exConf = new M1LayoutedM2ShadowRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );

        CRepositoryCoreConfiguration result =
            new CRepositoryCoreConfiguration(
                                              getTemplateProvider().getApplicationConfiguration(),
                                              repo,
                                              new CRepositoryExternalConfigurationHolderFactory<M1LayoutedM2ShadowRepositoryConfiguration>()
                                              {
                                                  public M1LayoutedM2ShadowRepositoryConfiguration createExternalConfigurationHolder(
                                                                                                                                      CRepository config )
                                                  {
                                                      return new M1LayoutedM2ShadowRepositoryConfiguration(
                                                                                                            (Xpp3Dom) config
                                                                                                                .getExternalConfiguration() );
                                                  }
                                              } );

        return result;
    }
}
