package org.sonatype.nexus.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.util.StringUtils;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

@Component( role = StaticSecurityResource.class, hint = "NexusViewSecurityResource" )
public class NexusViewSecurityResource
    extends AbstractStaticSecurityResource
    implements EventListener, Initializable
{
    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private ApplicationEventMulticaster eventMulticaster;

    @Requirement
    private RepositoryTypeRegistry repoTypeRegistry;

    @Override
    public String getResourcePath()
    {
        return null;
    }

    @Override
    public Configuration getConfiguration()
    {
        Configuration configuration = new Configuration();

        configuration.addPrivilege( buildPrivilege( "All Repositories - (view)",
                                                    "Privilege that gives view access to all repositories.", "*" ) );

        for ( Repository repo : repoRegistry.getRepositories() )
        {
            configuration.addPrivilege( buildPrivilege( repo.getName() + " - (view)",
                                                        "Privilege that gives view access to the " + repo.getName()
                                                            + " repository.", repo.getId() ) );
        }

        Set<Entry<String, ContentClass>> contents = repoTypeRegistry.getContentClasses().entrySet();

        for ( Entry<String, ContentClass> entry : contents )
        {
            // TODO in the future we can create CRUD privs / roles here
            configuration.addRole( buildRole( entry, "view" ) );
        }

        setDirty( false );

        return configuration;
    }

    private CRole buildRole( Entry<String, ContentClass> entry, String method )
    {
        String content = entry.getKey();
        CRole view = new CRole();
        view.setId( content + "-all-" + method );

        content = StringUtils.capitalizeFirstLetter( content );
        view.setDescription( "Gives access to " + method + " ALL " + content + " Repositories in Nexus." );

        method = StringUtils.capitalizeFirstLetter( method );
        view.setName( "Repo: All Eclipse " + content + " Repositories (" + method + ")" );
        view.setSessionTimeout( 60 );

        List<? extends Repository> repos = getRepositoriesWithContentClass( entry.getValue() );
        for ( Repository repo : repos )
        {
            view.addPrivilege( "repository-" + repo.getId() );
        }
        return view;
    }

    private List<? extends Repository> getRepositoriesWithContentClass( ContentClass content )
    {
        List<Repository> filtered = new ArrayList<Repository>();
        Collection<Repository> repos = repoRegistry.getRepositories();
        for ( Repository repository : repos )
        {
            if ( content.equals( repository.getRepositoryContentClass() ) )
            {
                filtered.add( repository );
            }
        }
        return filtered;
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
