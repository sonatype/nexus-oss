/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.cache;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.cache.CacheStatistics;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.NFCRepositoryResource;
import org.sonatype.nexus.rest.model.NFCResource;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.rest.model.NFCStats;
import org.sonatype.nexus.rest.restore.AbstractRestorePlexusResource;
import org.sonatype.nexus.tasks.ExpireCacheTask;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "CachePlexusResource" )
@Path( CachePlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class CachePlexusResource
    extends AbstractRestorePlexusResource
{
    public static final String RESOURCE_URI = "/data_cache/{" + DOMAIN + "}/{" + TARGET_ID + "}/content"; 

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/data_cache/*/*/content**", "authcBasic,perms[nexus:cache]" );
    }

    /**
     * Retrieve the contents of the Not Found Cache at the specified domain (repository or group).
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRestorePlexusResource.DOMAIN ), @PathParam( AbstractRestorePlexusResource.TARGET_ID ) }, 
                              output = NFCResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        try
        {
            NFCResource resource = new NFCResource();

            // check reposes
            if ( getRepositoryGroupId( request ) != null )
            {
                for ( Repository repository : getRepositoryRegistry()
                    .getRepositoryWithFacet( getRepositoryGroupId( request ), GroupRepository.class )
                    .getMemberRepositories() )
                {
                    NFCRepositoryResource repoNfc = createNFCRepositoryResource( repository );

                    resource.addNfcContent( repoNfc );
                }
            }
            else if ( getRepositoryId( request ) != null )
            {
                Repository repository = getRepositoryRegistry().getRepository( getRepositoryId( request ) );

                NFCRepositoryResource repoNfc = createNFCRepositoryResource( repository );

                resource.addNfcContent( repoNfc );
            }

            NFCResourceResponse result = new NFCResourceResponse();

            result.setData( resource );

            return result;
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

    protected NFCRepositoryResource createNFCRepositoryResource( Repository repository )
    {
        NFCRepositoryResource repoNfc = new NFCRepositoryResource();

        repoNfc.setRepositoryId( repository.getId() );

        CacheStatistics stats = repository.getNotFoundCache().getStatistics();

        NFCStats restStats = new NFCStats();

        restStats.setSize( stats.getSize() );

        restStats.setHits( stats.getHits() );

        restStats.setMisses( stats.getMisses() );

        repoNfc.setNfcStats( restStats );

        repoNfc.getNfcPaths().addAll( repository.getNotFoundCache().listKeysInCache() );

        return repoNfc;
    }
    
    /**
     * Expire the cache of the selected domain (repository or group).  This includes expiring the cache of items in a proxy repository
     * so the remote will be rechecked on next access, along with clearning the Not Found Cache.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRestorePlexusResource.DOMAIN ), @PathParam( AbstractRestorePlexusResource.TARGET_ID ) } )
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        ExpireCacheTask task = getNexusScheduler().createTaskInstance( ExpireCacheTask.class );

        task.setRepositoryId( getRepositoryId( request ) );

        task.setRepositoryGroupId( getRepositoryGroupId( request ) );

        task.setResourceStorePath( getResourceStorePath( request ) );

        handleDelete( task, request );
    }

}
