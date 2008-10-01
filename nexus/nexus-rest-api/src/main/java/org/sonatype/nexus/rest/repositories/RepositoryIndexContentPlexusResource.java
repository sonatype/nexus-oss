/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.repositories;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;

/**
 * Index content resource.
 * 
 * @author dip
 * @plexus.component role-hint="repoIndexResource"
 */
public class RepositoryIndexContentPlexusResource extends AbstractNexusPlexusResource {

    public static final String REPOSITORY_ID_KEY = "repositoryId";

    /**
     * @plexus.requirement
     */
    private IndexerManager indexerManager;
    
    @Override
    public Object getPayloadInstance() {
        return null;
    }

    @Override
    public String getResourceUri() {
        return "/repositories/{" + REPOSITORY_ID_KEY + "}/index_content";
    }
    
    @Override
    public Object get(Context context, Request request, Response response, Variant variant)
            throws ResourceException {
        String path = parsePathFromUri( request.getResourceRef().toString() );
        if ( ! path.endsWith( "/" ) ) {
            response.redirectPermanent( path + "/" );
            return null;
        }

        String repositoryId = String.valueOf( request.getAttributes().get( REPOSITORY_ID_KEY ) );
        try {
            IndexingContext indexingContext =
                indexerManager.getRepositoryRemoteIndexContext( repositoryId );
            
            return createResponse( request, indexingContext );
        }
        catch ( NoSuchRepositoryException e ) {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
        }
    }
    
    protected Object createResponse( Request request, IndexingContext indexingContext )
            throws ResourceException {
        String path = parsePathFromUri( request.getResourceRef().getRemainingPart() );
        Collection<String> groups = getGroups( path, indexingContext ); 
        
        ContentListResourceResponse response = new ContentListResourceResponse();

        for ( String group : groups )
        {
            ContentListResource resource = new ContentListResource();

            resource.setText( group );
            resource.setLeaf( false );
            resource.setResourceURI( createChildReference( request, group ).toString() + "/" );
            resource.setRelativePath( path + group + "/" );
            resource.setLastModified( new Date() );
            resource.setSizeOnDisk( -1 );

            response.addData( resource );
        }
        
        return response;
    }
    
    protected Collection<String> getGroups( String path, IndexingContext indexingContext )
            throws ResourceException {
        NexusIndexer indexer = indexerManager.getNexusIndexer();
        path = path.substring( 1 ); // strip the leading slash

        try {
            if ( path.length() == 0 ) {
              return indexer.getRootGroups( indexingContext );
            }
            else {
                path = path.replace( '/', '.' );
                int n = path.length();
                Set<String> result = new HashSet<String>();
                Set<String> groups = indexer.getAllGroups( indexingContext );
                for ( String group : groups ) {
                    if ( group.startsWith( path ) ) {
                        group = group.substring( n );
                        int nextDot = group.indexOf( '.' );
                        if ( nextDot > -1 ) {
                            group = group.substring( 0, nextDot );
                        }
                        if ( ! result.contains( group ) ) {
                            result.add( group );
                        }
                    }
                }
                return result;
            }
        }
        catch ( IOException e ) {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }
    
    protected String parsePathFromUri(String parsedPath) {

        // get rid of query part
        if (parsedPath.contains("?")) {
            parsedPath = parsedPath.substring(0, parsedPath.indexOf('?'));
        }

        // get rid of reference part
        if (parsedPath.contains("#")) {
            parsedPath = parsedPath.substring(0, parsedPath.indexOf('#'));
        }

        if (StringUtils.isEmpty(parsedPath)) {
            parsedPath = "/";
        }

        return parsedPath;
    }
}
