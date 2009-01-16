package org.sonatype.nexus.proxy.router;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.util.ItemPathUtils;

/**
 * The simplest re-implementation for RepositoryRouter that does only routing.
 * 
 * @author cstamas
 */
@Component( role = RepositoryRouter.class )
public class DefaultRepositoryRouter
    extends LoggingComponent
    implements RepositoryRouter, Initializable
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    /** Should links be resolved? */
    private boolean followLinks;

    public boolean isFollowLinks()
    {
        return followLinks;
    }

    public void setFollowLinks( boolean followLinks )
    {
        this.followLinks = followLinks;
    }

    public StorageItem dereferenceLink( StorageLinkItem link )
        throws AccessDeniedException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Dereferencing link " + link.getTarget() );
        }

        ResourceStoreRequest req = new ResourceStoreRequest( link.getTarget(), false );

        req.getRequestContext().putAll( link.getItemContext() );

        return link.getTarget().getRepository().retrieveItem( req );
    }

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        RequestRoute route = getRequestRouteForRequest( request );

        if ( route.isRepositoryHit() )
        {
            // it hits a repository, mangle path and call it

            request.pushRequestPath( route.getRepositoryPath() );

            StorageItem item = route.getTargetedRepository().retrieveItem( request );

            request.popRequestPath();

            return mangle( false, request, route, item );
        }
        else
        {
            // this is "above" repositories
            return retrieveVirtualPath( request, route );
        }
    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        RequestRoute route = getRequestRouteForRequest( request );

        if ( route.isRepositoryHit() )
        {
            // it hits a repository, mangle path and call it

            request.pushRequestPath( route.getRepositoryPath() );

            route.getTargetedRepository().storeItem( request, is, userAttributes );

            request.popRequestPath();
        }
        else
        {
            // this is "above" repositories
            throw new IllegalRequestException( request, "The path '" + request.getRequestPath()
                + "' does not points to any repository!" );
        }
    }

    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        RequestRoute fromRoute = getRequestRouteForRequest( from );

        RequestRoute toRoute = getRequestRouteForRequest( to );

        if ( fromRoute.isRepositoryHit() && toRoute.isRepositoryHit() )
        {
            // it hits a repository, mangle path and call it

            from.pushRequestPath( fromRoute.getRepositoryPath() );
            to.pushRequestPath( toRoute.getRepositoryPath() );

            if ( fromRoute.getTargetedRepository() == toRoute.getTargetedRepository() )
            {
                fromRoute.getTargetedRepository().copyItem( from, to );
            }
            else
            {
                StorageItem item = fromRoute.getTargetedRepository().retrieveItem( from );

                if ( item instanceof StorageFileItem )
                {
                    try
                    {
                        toRoute.getTargetedRepository().storeItem(
                            to,
                            ( (StorageFileItem) item ).getInputStream(),
                            item.getAttributes() );
                    }
                    catch ( IOException e )
                    {
                        // XXX: this is nonsense, to box IOException into subclass of IOException!
                        throw new StorageException( e );
                    }
                }
                else if ( item instanceof StorageCollectionItem )
                {
                    toRoute.getTargetedRepository().createCollection( to, item.getAttributes() );
                }
                else
                {
                    throw new IllegalRequestException( from, "Cannot copy item of class='" + item.getClass().getName()
                        + "' over multiple repositories." );
                }

            }

            from.popRequestPath();
            to.popRequestPath();
        }
        else
        {
            // this is "above" repositories
            if ( !fromRoute.isRepositoryHit() )
            {
                throw new IllegalRequestException( from, "The path '" + from.getRequestPath()
                    + "' does not points to any repository!" );
            }
            else
            {
                throw new IllegalRequestException( to, "The path '" + to.getRequestPath()
                    + "' does not points to any repository!" );
            }
        }
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        copyItem( from, to );

        deleteItem( from );
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        RequestRoute route = getRequestRouteForRequest( request );

        if ( route.isRepositoryHit() )
        {
            // it hits a repository, mangle path and call it

            request.pushRequestPath( route.getRepositoryPath() );

            Collection<StorageItem> items = route.getTargetedRepository().list( request );

            request.popRequestPath();

            ArrayList<StorageItem> result = new ArrayList<StorageItem>( items.size() );

            for ( StorageItem item : items )
            {
                result.add( mangle( true, request, route, item ) );
            }

            return result;
        }
        else
        {
            // this is "above" repositories
            return listVirtualPath( request, route );
        }
    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        RequestRoute route = getRequestRouteForRequest( request );

        if ( route.isRepositoryHit() )
        {
            // it hits a repository, mangle path and call it

            request.pushRequestPath( route.getRepositoryPath() );

            route.getTargetedRepository().createCollection( request, userAttributes );

            request.popRequestPath();
        }
        else
        {
            // this is "above" repositories
            throw new IllegalRequestException( request, "The path '" + request.getRequestPath()
                + "' does not points to any repository!" );
        }
    }

    public void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        RequestRoute route = getRequestRouteForRequest( request );

        if ( route.isRepositoryHit() )
        {
            // it hits a repository, mangle path and call it

            request.pushRequestPath( route.getRepositoryPath() );

            route.getTargetedRepository().deleteItem( request );

            request.popRequestPath();
        }
        else
        {
            // this is "above" repositories
            throw new IllegalRequestException( request, "The path '" + request.getRequestPath()
                + "' does not points to any repository!" );
        }
    }

    public TargetSet getTargetsForRequest( ResourceStoreRequest request )
    {
        TargetSet result = new TargetSet();

        try
        {
            RequestRoute route = getRequestRouteForRequest( request );

            if ( route.isRepositoryHit() )
            {
                // it hits a repository, mangle path and call it

                request.pushRequestPath( route.getRepositoryPath() );

                result.addTargetSet( route.getTargetedRepository().getTargetsForRequest( request ) );

                request.popRequestPath();
            }
        }
        catch ( ItemNotFoundException e )
        {
            // nothing, empty set will be returned
        }

        return result;
    }

    public void initialize()
    {
        applicationConfiguration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            followLinks = applicationConfiguration.getConfiguration().getRouting().isFollowLinks();
        }
    }

    // ===
    // Private
    // ===

    protected StorageItem mangle( boolean isList, ResourceStoreRequest request, RequestRoute route, StorageItem item )
        throws AccessDeniedException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException
    {
        if ( isList )
        {
            ( (AbstractStorageItem) item ).setPath( ItemPathUtils.concatPaths( route.getOriginalRequestPath(), item
                .getName() ) );
        }
        else
        {
            ( (AbstractStorageItem) item ).setPath( route.getOriginalRequestPath() );
        }

        if ( isFollowLinks() && item instanceof StorageLinkItem )
        {
            return dereferenceLink( (StorageLinkItem) item );
        }
        else
        {
            return item;
        }
    }

    // XXX: a todo here is to make the "aliases" ("groups" for GroupRepository.class) dynamic,
    // and even think about new layout: every kind should have it's own "folder", you don't want to see
    // maven2 and P2 repositories along each other...
    protected RequestRoute getRequestRouteForRequest( ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        RequestRoute result = new RequestRoute();

        result.setOriginalRequestPath( request.getRequestPath() );

        result.setResourceStoreRequest( request );

        String correctedPath = request.getRequestPath().startsWith( RepositoryItemUid.PATH_SEPARATOR ) ? request
            .getRequestPath().substring( 1, request.getRequestPath().length() ) : request.getRequestPath();

        String[] explodedPath = correctedPath.split( RepositoryItemUid.PATH_SEPARATOR );

        Class<? extends Repository> kind = null;

        if ( explodedPath.length >= 1 )
        {
            // we have kind information ("repositories" vs "groups")
            if ( explodedPath[0].equals( "repositories" ) )
            {
                kind = Repository.class;
            }
            else if ( explodedPath[0].equals( "groups" ) )
            {
                kind = GroupRepository.class;
            }
            else if ( explodedPath[0].equals( "virtuals" ) )
            {
                kind = ShadowRepository.class;
            }
            else
            {
                // unknown explodedPath[0]
                throw new ItemNotFoundException( request.getRequestPath() );
            }

            result.setStrippedPrefix( ItemPathUtils.concatPaths( explodedPath[0] ) );
        }

        if ( explodedPath.length >= 2 )
        {
            // we have repoId information in path
            Repository repository = null;

            try
            {
                repository = repositoryRegistry.getRepositoryWithFacet( explodedPath[1], kind );
            }
            catch ( NoSuchRepositoryException e )
            {
                // obviously, the repoId (explodedPath[1]) points to some nonexistent repoID
                throw new ItemNotFoundException( request.getRequestPath() );
            }

            result.setStrippedPrefix( ItemPathUtils.concatPaths( explodedPath[0], explodedPath[1] ) );

            result.setTargetedRepository( repository );

            String repoPath = "";

            for ( int i = 2; i < explodedPath.length; i++ )
            {
                repoPath = ItemPathUtils.concatPaths( repoPath, explodedPath[i] );
            }

            if ( result.getOriginalRequestPath().endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
            {
                repoPath = repoPath + RepositoryItemUid.PATH_SEPARATOR;
            }

            result.setRepositoryPath( repoPath );
        }

        return result;
    }

    protected StorageItem retrieveVirtualPath( ResourceStoreRequest request, RequestRoute route )
        throws ItemNotFoundException
    {
        return null;
    }

    protected Collection<StorageItem> listVirtualPath( ResourceStoreRequest request, RequestRoute route )
        throws ItemNotFoundException
    {
        return null;
    }

}
