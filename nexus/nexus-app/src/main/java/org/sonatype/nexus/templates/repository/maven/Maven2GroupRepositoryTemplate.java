package org.sonatype.nexus.templates.repository.maven;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;

public class Maven2GroupRepositoryTemplate
    extends AbstractMavenRepositoryTemplate
{
    public Maven2GroupRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description )
    {
        super( provider, id, description, new Maven2ContentClass(), MavenGroupRepository.class, null );
    }

    public M2GroupRepositoryConfiguration getExternalConfiguration()
    {
        return (M2GroupRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration();
    }

    protected CoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( GroupRepository.class.getName() );
        repo.setProviderHint( "maven2" );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        M2GroupRepositoryConfiguration exConf = new M2GroupRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setAllowWrite( true );

        CRepositoryCoreConfiguration result = new CRepositoryCoreConfiguration( repo );

        return result;
    }
}
