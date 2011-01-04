/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
        return new PathProtectionDescriptor( "/data_cache/*/*/content/**", "authcBasic,perms[nexus:cache]" );
    }

    /**
     * Retrieve the contents of the Not Found Cache at the specified domain (repository or group). Note that
     * appended to the end of the url should be the path that you want cache cleared from.  i.e.
     * /content/org/blah will clear cache of everything under the org/blah directory.
     * 
     * @param domain The domain that will be used, valid options are 'repositories' or 'repo_groups' (Required).
     * @param target The unique id in the domain to use (i.e. repository or group id) (Required).
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
