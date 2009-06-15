package org.sonatype.nexus.templates.repository.maven;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

public class Maven2GroupRepositoryTemplate
    extends RepositoryTemplate
{
    public Maven2GroupRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description )
        throws ConfigurationException
    {
        super( provider, id, description, new DefaultCRepository(), new Maven2ContentClass(),
               MavenGroupRepository.class );

        initConfiguration();
    }

    @Override
    public M2GroupRepositoryConfiguration getExternalConfiguration()
    {
        return (M2GroupRepositoryConfiguration) getCoreConfiguration().getExternalConfiguration();
    }

    protected void initConfiguration()
    {
        CRepository repo = (CRepository) getCoreConfiguration().getConfiguration( true );
        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( GroupRepository.class.getName() );
        repo.setProviderHint( "maven2" );

        Xpp3Dom ex = new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME );
        repo.setExternalConfiguration( ex );

        M2GroupRepositoryConfiguration exConf = new M2GroupRepositoryConfiguration( ex );
        repo.externalConfigurationImple = exConf;

        repo.setAllowWrite( true );

        getCoreConfiguration().applyChanges();
    }
}
