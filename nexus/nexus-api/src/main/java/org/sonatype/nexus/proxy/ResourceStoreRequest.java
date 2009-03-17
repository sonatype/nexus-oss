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

import java.util.List;
import java.util.Stack;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * Request for a resource. It drives many aspects of the request itself.
 * 
 * @author cstamas
 */
public class ResourceStoreRequest
{
    /** The path we want to retrieve. */
    private String requestPath;

    /** Extra data associated with this request. */
    private RequestContext requestContext;

    /** Used internally by Routers. */
    private Stack<String> pathStack;

    public ResourceStoreRequest( String requestPath, boolean localOnly, boolean remoteOnly )
    {
        super();
        this.requestPath = requestPath;
        this.pathStack = new Stack<String>();
        this.requestContext = new RequestContext();
        this.requestContext.setRequestLocalOnly( localOnly );
        this.requestContext.setRequestRemoteOnly( remoteOnly );
    }

    public ResourceStoreRequest( String requestPath, boolean localOnly )
    {
        this( requestPath, localOnly, false );
    }

    public ResourceStoreRequest( String requestPath )
    {
        this( requestPath, false, false );
    }

    /**
     * Creates a request aimed at given path denoted by RepositoryItemUid.
     * 
     * @param uid the uid
     * @deprecated use ResourceStoreRequest(String path)
     */
    public ResourceStoreRequest( RepositoryItemUid uid, boolean localOnly )
    {
        this( uid.getPath(), localOnly, false );
    }

    /**
     * Creates a request for a given item.
     * 
     * @param item
     */
    public ResourceStoreRequest( StorageItem item )
    {
        this( item.getRepositoryItemUid().getPath(), true, false );

        this.requestContext = item.getItemContext();

        // but clear up the list of processed repositories
        this.requestContext.getProcessedRepositories().clear();
    }

    /**
     * Gets the request context.
     * 
     * @return the request context
     */
    public RequestContext getRequestContext()
    {
        return requestContext;
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
        return getRequestContext().isRequestLocalOnly();
    }

    /**
     * Sets the request local only.
     * 
     * @param requestLocalOnly the new request local only
     */
    public void setRequestLocalOnly( boolean requestLocalOnly )
    {
        getRequestContext().setRequestLocalOnly( requestLocalOnly );
    }

    /**
     * Checks if is request remote only.
     * 
     * @return true, if is request remote only
     */
    public boolean isRequestRemoteOnly()
    {
        return getRequestContext().isRequestRemoteOnly();
    }

    /**
     * Sets the request remote only.
     * 
     * @param requestremoteOnly the new request remote only
     */
    public void setRequestRemoteOnly( boolean requestRemoteOnly )
    {
        getRequestContext().setRequestRemoteOnly( requestRemoteOnly );
    }

    /**
     * Returns the list of processed repositories.
     * 
     * @return
     */
    public List<String> getProcessedRepositories()
    {
        return getRequestContext().getProcessedRepositories();
    }

    /**
     * Returns true if the request is conditional.
     * 
     * @return true if this request is conditional.
     */
    public boolean isConditional()
    {
        return getRequestContext().isConditional();
    }

    /**
     * Returns the timestamp to check against.
     * 
     * @return
     */
    public long getIfModifiedSince()
    {
        return getRequestContext().getIfModifiedSince();
    }

    /**
     * Sets the timestamp to check against.
     * 
     * @param ifModifiedSince
     */
    public void setIfModifiedSince( long ifModifiedSince )
    {
        getRequestContext().setIfModifiedSince( ifModifiedSince );
    }

    /**
     * Gets the ETag (SHA1 in Nexus) to check item against.
     * 
     * @return
     */
    public String getIfNoneMatch()
    {
        return getRequestContext().getIfNoneMatch();
    }

    /**
     * Sets the ETag (SHA1 in Nexus) to check item against.
     * 
     * @param tag
     */
    public void setIfNoneMatch( String tag )
    {
        getRequestContext().setIfNoneMatch( tag );
    }

    /**
     * Returns the URL of the original request.
     * 
     * @return
     */
    public String getRequestUrl()
    {
        return getRequestContext().getRequestUrl();
    }

    /**
     * Sets the URL of the original request.
     * 
     * @param url
     */
    public void setRequestUrl( String url )
    {
        getRequestContext().setRequestUrl( url );
    }

    /**
     * Returns the URL of the AppRoot of the incoming request.
     * 
     * @return
     */
    public String getRequestAppRootUrl()
    {
        return getRequestContext().getRequestAppRootUrl();
    }

    /**
     * Sets the URL of the AppRoot of the incoming request.
     * 
     * @param url
     */
    public void setRequestAppRootUrl( String url )
    {
        getRequestContext().setRequestAppRootUrl( url );
    }

    public String toString()
    {
        return getRequestPath();
    }
}
