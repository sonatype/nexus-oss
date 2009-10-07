package org.sonatype.nexus.security;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

@Component( role = StaticSecurityResource.class, hint = "NexusViewSecurityResource" )
public class NexusViewSecurityResource extends AbstractStaticSecurityResource implements EventListener, Initializable
{
    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private ApplicationEventMulticaster eventMulticaster;

    public String getResourcePath()
    {
        return null;
    }

    public Configuration getConfiguration()
    {
        Configuration configuration = new Configuration();

        configuration.addPrivilege( buildPrivilege(
            "All Repositories - (view)",
            "Privilege that gives view access to all repositories.",
            "*" ) );

        for ( Repository repo : repoRegistry.getRepositories() )
        {
            configuration.addPrivilege( buildPrivilege(
                repo.getName() + " - (view)",
                "Privilege that gives view access to the " + repo.getName() + " repository.",
                repo.getId() ) );
        }

        setDirty( false );

        return configuration;
    }
    
    protected CPrivilege buildPrivilege( String name, String description, String repoId )
    {
        CPrivilege priv = new CPrivilege();
        
        priv.setId( "repository-" + ( repoId.equals( "*" ) ? "all" : repoId ) );
        priv.setName( name );
        priv.setDescription( description );
        priv.setType( RepositoryViewPrivilegeDescriptor.TYPE );
        
        CProperty prop = new CProperty();
        prop.setKey( RepositoryPropertyDescriptor.ID );
        prop.setValue( repoId );        
        priv.addProperty( prop );
        
        return priv;
    }
    
    public void onEvent( Event<?> event )
    {
        if ( RepositoryRegistryEventAdd.class.isAssignableFrom( event.getClass() )
            || RepositoryRegistryEventRemove.class.isAssignableFrom( event.getClass() ) )
        {
            setDirty( true );
        }
    }

    public void initialize()
        throws InitializationException
    {
        this.eventMulticaster.addEventListener( this );
    }
}
