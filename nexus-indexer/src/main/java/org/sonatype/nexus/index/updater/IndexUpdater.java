/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.updater;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

/** 
 * @author Jason van Zyl 
 */
public interface IndexUpdater
{
    /**
     * @return timestamp for updated index
     */
    Date fetchAndUpdateIndex( IndexingContext context, TransferListener listener )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    /**
     * @return timestamp for updated index
     */
    Date fetchAndUpdateIndex( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException,
            UnsupportedExistingLuceneIndexException;

    Properties fetchIndexProperties( IndexingContext context, TransferListener listener, ProxyInfo proxyInfo )
        throws IOException;

    Date getTimestamp( Properties properties, String key ); 

    String getUpdateChunkName( Date contextTimestamp, Properties properties ); 

}
