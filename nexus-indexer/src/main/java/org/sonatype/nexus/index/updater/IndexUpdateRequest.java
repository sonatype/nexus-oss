/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.File;

import org.sonatype.nexus.index.context.DocumentFilter;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.fs.Locker;

/**
 * @author Eugene Kuleshov
 */
public class IndexUpdateRequest 
{
    private final IndexingContext context;
    
    private ResourceFetcher resourceFetcher;

    private DocumentFilter documentFilter;
    
    private boolean forceFullUpdate;

    private File localIndexCacheDir;

    private Locker locker;

    private boolean offline;

    private boolean cacheOnly;

    public IndexUpdateRequest( IndexingContext context )
    {
        this.context = context;
        this.forceFullUpdate = false;
    }

    public IndexingContext getIndexingContext() 
    {
        return context;
    }
    
    public ResourceFetcher getResourceFetcher()
    {
        return resourceFetcher;
    }

    public DocumentFilter getDocumentFilter()
    {
        return documentFilter;
    }

    public void setDocumentFilter( DocumentFilter documentFilter ) 
    {
        this.documentFilter = documentFilter;
    }

    /**
     * If null, the default wagon manager will be used, incorporating
     * the ProxyInfo and TransferListener if supplied
     * 
     * @param resourceFetcher
     */
    public void setResourceFetcher(ResourceFetcher resourceFetcher) 
    {
        this.resourceFetcher = resourceFetcher;
    }
    
    public void setForceFullUpdate( boolean forceFullUpdate )
    {
        this.forceFullUpdate = forceFullUpdate;
    }
    
    public boolean isForceFullUpdate()
    {
        return forceFullUpdate;
    }
    
    public File getLocalIndexCacheDir()
    {
        return localIndexCacheDir;
    }

    public void setLocalIndexCacheDir( File dir )
    {
        this.localIndexCacheDir = dir;
    }

    public Locker getLocker()
    {
        return locker;
    }

    public void setLocker( Locker locker )
    {
        this.locker = locker;
    }

    public void setOffline( boolean offline )
    {
        this.offline = offline;
    }

    public boolean isOffline()
    {
        return offline;
    }

    public void setCacheOnly( boolean cacheOnly )
    {
        this.cacheOnly = cacheOnly;
    }

    public boolean isCacheOnly()
    {
        return cacheOnly;
    }
}
