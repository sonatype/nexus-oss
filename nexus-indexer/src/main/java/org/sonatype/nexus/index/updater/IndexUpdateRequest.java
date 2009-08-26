/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.sonatype.nexus.index.context.DocumentFilter;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @author Eugene Kuleshov
 */
public class IndexUpdateRequest 
{
    private final IndexingContext context;
    
    private ResourceFetcher resourceFetcher;

    private DocumentFilter documentFilter;
    
    private AuthenticationInfo authenticationInfo;
    
    private ProxyInfo proxyInfo;
    
    private TransferListener transferListener;
    
    private boolean forceFullUpdate;

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
    
    public ProxyInfo getProxyInfo()
    {
        return proxyInfo;
    }
    
    /**
     * ProxyInfo object that will be used by the default wagon manager
     * if no other fetcher is set
     * 
     * @param proxyInfo
     */
    public void setProxyInfo( ProxyInfo proxyInfo )
    {
        this.proxyInfo = proxyInfo;
    }
    
    public TransferListener getTransferListener()
    {
        return transferListener;
    }
    
    /**
     * TransferListener that will be used by the default wagon manager
     * if no other fetcher is set
     * 
     * @param transferListener
     */
    public void setTransferListener( TransferListener transferListener )
    {
        this.transferListener = transferListener;
    }

    public AuthenticationInfo getAuthenticationInfo()
    {
        return authenticationInfo;
    }

    public void setAuthenticationInfo( AuthenticationInfo authenticationInfo )
    {
        this.authenticationInfo = authenticationInfo;
    }
}
