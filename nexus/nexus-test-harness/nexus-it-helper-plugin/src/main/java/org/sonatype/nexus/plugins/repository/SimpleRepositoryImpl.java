package org.sonatype.nexus.plugins.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

@Component( role = SimpleRepository.class, hint="default" )
public class SimpleRepositoryImpl
    extends AbstractRepository
    implements SimpleRepository
{
    @Requirement( hint = SimpleContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private SimpleRepositoryConfigurator simpleRepositoryConfigurator;

    private final RepositoryKind repositoryKind = new DefaultRepositoryKind( SimpleRepository.class, null );

    @Override
    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    @Override
    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return simpleRepositoryConfigurator;
    }

    @Override
    protected SimpleRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (SimpleRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<SimpleRepositoryConfiguration> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<SimpleRepositoryConfiguration>()
        {
            public SimpleRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new SimpleRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    @Override
    public synchronized String sayHello()
    {
        int cnt = getExternalConfiguration( false ).getSaidHelloCount();

        getExternalConfiguration( true ).setSaidHelloCount( cnt++ );

        getLogger().info( String.format( "Saying \"Hello\" for %s time.", cnt ) );

        return "hello";
    }
}
