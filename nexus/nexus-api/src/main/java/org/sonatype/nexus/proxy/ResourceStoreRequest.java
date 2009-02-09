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
package org.sonatype.nexus.proxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * Request for a resource. It drives many aspects of the request itself.
 * 
 * @author cstamas
 */
public class ResourceStoreRequest
{
    /** Context URL of the app root on the incoming connector. */
    public static final String CTX_REQUEST_APP_ROOT_URL = "request.appRootUrl";

    /** Context URL of the original resource requested on the incoming connector. */
    public static final String CTX_REQUEST_URL = "request.url";

    /** Context flag to mark a request local only. */
    public static final String CTX_LOCAL_ONLY_FLAG = "request.localOnly";

    /** Context flag to mark a request local only. */
    public static final String CTX_REMOTE_ONLY_FLAG = "request.remoteOnly";

    /** Context key for set of processed repositories. */
    public static final String CTX_PROCESSED_REPOSITORIES = "request.processedRepositories";

    /** The path we want to retrieve. */
    private String requestPath;

    /** Extra data associated with this request. */
    private Map<String, Object> requestContext;

    /** Explicitly targets a repository (only if accessed over Routers!). */
    private String requestRepositoryId;

    /** Used internally by Routers. */
    private Stack<String> pathStack;

    public ResourceStoreRequest( String requestPath, boolean localOnly, boolean remoteOnly, String repositoryId )
    {
        super();
        this.requestPath = requestPath;
        this.requestRepositoryId = repositoryId;
        this.pathStack = new Stack<String>();
        this.requestContext = new HashMap<String, Object>();
        this.requestContext.put( CTX_LOCAL_ONLY_FLAG, localOnly );
        this.requestContext.put( CTX_REMOTE_ONLY_FLAG, remoteOnly );
        this.requestContext.put( CTX_PROCESSED_REPOSITORIES, new HashSet<String>() );
    }

    public ResourceStoreRequest( String requestPath, boolean localOnly, String repositoryId )
    {
        this( requestPath, localOnly, false, repositoryId );
    }

    /**
     * Creates a request aimed at given path. You are free to set some other attributes of this created default request.
     * 
     * @param requestPath the path.
     */
    public ResourceStoreRequest( String requestPath, boolean localOnly )
    {
        this( requestPath, localOnly, null );
    }

    /**
     * Creates a request aimed at given path denoted by RepositoryItemUid.
     * 
     * @param uid the uid
     */
    public ResourceStoreRequest( RepositoryItemUid uid, boolean localOnly )
    {
        this( uid.getPath(), localOnly, uid.getRepository().getId() );
    }

    /**
     * Gets the request path.
     * 
     * @return the request path
     */
    public String getRequestPath()
    {
        return requestPath;
    }

    /**
     * Sets the request path.
     * 
     * @param requestPath the new request path
     */
    public void setRequestPath( String requestPath )
    {
        this.requestPath = requestPath;
    }

    /**
     * Push request path. Used internally by Router.
     * 
     * @param requestPath the request path
     */
    public void pushRequestPath( String requestPath )
    {
        pathStack.push( this.requestPath );
        this.requestPath = requestPath;
    }

    /**
     * Pop request path. Used internally by Router.
     * 
     * @return the string
     */
    public String popRequestPath()
    {
        this.requestPath = pathStack.pop();
        return getRequestPath();
    }

    /**
     * Checks if is request local only.
     * 
     * @return true, if is request local only
     */
    public boolean isRequestLocalOnly()
    {
        return (Boolean) getRequestContext().get( CTX_LOCAL_ONLY_FLAG );
    }

    /**
     * Sets the request local only.
     * 
     * @param requestLocalOnly the new request local only
     */
    public void setRequestLocalOnly( boolean requestLocalOnly )
    {
        getRequestContext().put( CTX_LOCAL_ONLY_FLAG, requestLocalOnly );
    }

    /**
     * Gets the request context.
     * 
     * @return the request context
     */
    public Map<String, Object> getRequestContext()
    {
        return requestContext;
    }

    /**
     * Sets the request context.
     * 
     * @param requestContext the request context
     */
    public void setRequestContext( Map<String, Object> requestContext )
    {
        this.requestContext = requestContext;
    }

    /**
     * Gets the request repository id.
     * 
     * @return the request repository id
     */
    public String getRequestRepositoryId()
    {
        return requestRepositoryId;
    }

    /**
     * Sets the request repository id.
     * 
     * @param requestRepositoryId the new request repository id
     */
    public void setRequestRepositoryId( String requestRepositoryId )
    {
        this.requestRepositoryId = requestRepositoryId;
    }

    /**
     * Returns the list of processed repositories.
     * 
     * @return
     */
    @SuppressWarnings( "unchecked" )
    public Set<String> getProcessedRepositories()
    {
        return (Set<String>) getRequestContext().get( CTX_PROCESSED_REPOSITORIES );
    }

    /**
     * Returns true if the request is conditional.
     * 
     * @return true if this request is conditional.
     */
    public boolean isConditional()
    {
        return ItemContextUtils.isConditional( getRequestContext() );
    }

    /**
     * Returns the timestamp to check against.
     * 
     * @return
     */
    public long getIfModifiedSince()
    {
        return ItemContextUtils.getIfModifiedSince( getRequestContext() );
    }

    /**
     * Sets the timestamp to check against.
     * 
     * @param ifModifiedSince
     */
    public void setIfModifiedSince( long ifModifiedSince )
    {
        ItemContextUtils.setIfModifiedSince( getRequestContext(), ifModifiedSince );
    }

    /**
     * Gets the ETag (SHA1 in Nexus) to check item against.
     * 
     * @return
     */
    public String getIfNoneMatch()
    {
        return ItemContextUtils.getIfNoneMatch( getRequestContext() );
    }

    /**
     * Sets the ETag (SHA1 in Nexus) to check item against.
     * 
     * @param tag
     */
    public void setIfNoneMatch( String tag )
    {
        ItemContextUtils.setIfNoneMatch( getRequestContext(), tag );
    }
}
