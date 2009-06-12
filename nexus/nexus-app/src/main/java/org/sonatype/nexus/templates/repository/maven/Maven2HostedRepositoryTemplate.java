package org.sonatype.nexus.templates.repository.maven;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;

public class Maven2HostedRepositoryTemplate
    extends AbstractMavenRepositoryTemplateHolder
{
    public Maven2HostedRepositoryTemplate( Nexus nexus, RepositoryPolicy repositoryPolicy )
    {
        super( nexus, new Maven2ContentClass(), MavenHostedRepository.class, repositoryPolicy );
    }

    @Override
    protected CoreConfiguration initConfiguration()
    {
        CRepository repo = new DefaultCRepository();
        repo.setId( "" );
        repo.setName( "" );

        repo.setProviderRole( Repository.class.getName() );
        repo.setProviderHint( "maven2" );

        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        repo.setExternalConfiguration( ex );

        M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );
        exConf.setRepositoryPolicy( getRepositoryPolicy() );
        exConf.applyChanges();
        repo.externalConfigurationImple = exConf;

        repo.setAllowWrite( true );

        return new CRepositoryCoreConfiguration( repo );
    }
}
