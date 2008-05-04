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
package org.sonatype.nexus.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * Request for a resource. It drives many aspects of the request itself.
 * 
 * @author cstamas
 */
public class ResourceStoreRequest
{

    /** The path we want to retrieve. */
    private String requestPath;

    /** If true, there will not happen remote access to get this item. Only local storage will be involved. */
    private boolean requestLocalOnly;

    /** IfModifiedSince. */
    private Long ifModifiedSince;

    /** Extra data associated with this request. */
    private Map<String, Object> requestContext;

    /** Explicitly targets a repository. */
    private String requestRepositoryId;

    /** Explicitly targets a repository group. */
    private String requestRepositoryGroupId;

    /** Used internally by Routers. */
    private Stack<String> pathStack;

    public ResourceStoreRequest( String requestPath, boolean localOnly, String repositoryId, String repositoryGroupId )
    {
        super();
        this.requestPath = requestPath;
        this.requestLocalOnly = localOnly;
        this.requestRepositoryId = repositoryId;
        this.requestRepositoryGroupId = repositoryGroupId;
        this.requestContext = new HashMap<String, Object>();
        this.pathStack = new Stack<String>();
        this.ifModifiedSince = null;
    }

    /**
     * Creates a request aimed at given path. You are free to set some other attributes of this created default request.
     * 
     * @param requestPath the path.
     */
    public ResourceStoreRequest( String requestPath, boolean localOnly )
    {
        this( requestPath, localOnly, null, null );
    }

    /**
     * Creates a request aimed at given path denoted by RepositoryItemUid.
     * 
     * @param uid the uid
     */
    public ResourceStoreRequest( RepositoryItemUid uid, boolean localOnly )
    {
        this( uid.getPath(), localOnly, uid.getRepository().getId(), null );
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
        return requestLocalOnly;
    }

    /**
     * Sets the request local only.
     * 
     * @param requestLocalOnly the new request local only
     */
    public void setRequestLocalOnly( boolean requestLocalOnly )
    {
        this.requestLocalOnly = requestLocalOnly;
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
     * Gets the request repository group id.
     * 
     * @return the request repository group id
     */
    public String getRequestRepositoryGroupId()
    {
        return requestRepositoryGroupId;
    }

    /**
     * Sets the request repository group id.
     * 
     * @param requestRepositoryGroupId the new request repository group id
     */
    public void setRequestRepositoryGroupId( String requestRepositoryGroupId )
    {
        this.requestRepositoryGroupId = requestRepositoryGroupId;
    }

    /**
     * Returns true if the request is conditional.
     * 
     * @return true if this request is conditional.
     */
    public boolean isConditional()
    {
        return this.ifModifiedSince != null;
    }

    /**
     * Returns the timestamp to checg against if
     * 
     * @return
     */
    public long getIfModifiedSince()
    {
        return ifModifiedSince.longValue();
    }

    public void setIfModifiedSince( long ifModifiedSince )
    {
        if ( ifModifiedSince != 0 )
        {
            this.ifModifiedSince = Long.valueOf( ifModifiedSince );
        }
        else
        {
            this.ifModifiedSince = null;
        }
    }
}
