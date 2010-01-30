/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.packer.IndexPacker;

/**
 * An index updater provides functionality to update index for remote
 * repositories using transfer format produced by the {@link IndexPacker}.
 * <p>
 * The following snippet shows how to update/download remote index:
 * 
 * <pre>
 *   IndexingContext context = indexer.getIndexingContexts().get(indexId);
 *   Settings settings = embedder.getSettings();
 *   Proxy proxy = settings.getActiveProxy();
 *   ProxyInfo proxyInfo = null;
 *   if(proxy != null) {
 *     proxyInfo = new ProxyInfo();
 *     proxyInfo.setHost(proxy.getHost());
 *     proxyInfo.setPort(proxy.getPort());
 *     proxyInfo.setNonProxyHosts(proxy.getNonProxyHosts());
 *     proxyInfo.setUserName(proxy.getUsername());
 *     proxyInfo.setPassword(proxy.getPassword());
 *   }
 *   
 *   Date indexTime = updater.fetchAndUpdateIndex(context, transferListener, proxyInfo);
 *   ...
 * </pre>
 * 
 * @author Jason van Zyl
 * @author Eugene Kuleshov
 */
public interface IndexUpdater
{
    Properties fetchIndexProperties( IndexingContext context, ResourceFetcher fetcher )
        throws IOException;
    
    /**
     * @return IndexUpdateResult
     */
    IndexUpdateResult fetchAndUpdateIndex( IndexUpdateRequest updateRequest )
        throws IOException; 

    /**
     * @deprecated use {@link #fetchIndexProperties(IndexingContext, ResourceFetcher)}
     */
    Properties fetchIndexProperties( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException;

    /**
     * @return timestamp for updated index
     * @deprecated use {@link #fetchAndUpdateIndex(IndexUpdateRequest)}
     */
    Date fetchAndUpdateIndex( IndexingContext context, TransferListener listener )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    /**
     * @return timestamp for updated index
     * @deprecated use {@link #fetchAndUpdateIndex(IndexUpdateRequest)}
     */
    Date fetchAndUpdateIndex( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    Date getTimestamp( Properties properties, String key );
    
}
