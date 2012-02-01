package org.sonatype.nexus.repositories.nexus4807;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

@Component( role = Repository.class, hint = Nexus4807RepositoryImpl.ID, instantiationStrategy = "per-lookup", description = "NEXUS4807 Repository" )
public class Nexus4807RepositoryImpl
    extends AbstractRepository
    implements Nexus4807Repository, Disposable
{
    public static final String ID = "nexus4807";

    @Requirement( hint = Nexus4807ContentClass.ID )
    private ContentClass contentClass;

    @Requirement
    private Nexus4807RepositoryConfigurator configurator;

    private final RepositoryKind repositoryKind;

    private boolean disposeInvoked;

    public Nexus4807RepositoryImpl()
    {
        this.repositoryKind = new DefaultRepositoryKind( Nexus4807Repository.class, null );
        this.disposeInvoked = false;
    }

    // repo peculiarities

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
        return configurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<AbstractRepositoryConfiguration>()
        {
            public AbstractRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new AbstractRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() )
                {
                };
            }
        };
    }

    // Disposable

    @Override
    public void dispose()
    {
        super.dispose();
        this.disposeInvoked = true;
    }

    // Nexus4807Repository type specific

    @Override
    public boolean isDisposeInvoked()
    {
        return disposeInvoked;
    }
}
