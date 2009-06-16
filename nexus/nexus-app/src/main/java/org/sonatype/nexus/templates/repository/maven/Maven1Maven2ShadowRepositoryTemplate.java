package org.sonatype.nexus.templates.repository.maven;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenShadowRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

public class Maven1Maven2ShadowRepositoryTemplate
    extends RepositoryTemplate
{
    public Maven1Maven2ShadowRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id,
                                                 String description )
        throws ConfigurationException
    {
        super( provider, id, description, new DefaultCRepository(), new Maven2ContentClass(),
               MavenShadowRepository.class );

        initConfiguration();
    }

    @Override
    public M2LayoutedM1ShadowRepositoryConfiguration getExternalConfiguration()
    {
        return (M2LayoutedM1ShadowRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration();
    }

    protected void initConfiguration()
    {
        CRepository repo = (CRepository) getCoreConfiguration().getConfiguration( true );
        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( ShadowRepository.class.getName() );
        repo.setProviderHint( "m1-m2-shadow" );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        M2LayoutedM1ShadowRepositoryConfiguration exConf = new M2LayoutedM1ShadowRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setAllowWrite( false );

        getCoreConfiguration().applyChanges();
    }
}
