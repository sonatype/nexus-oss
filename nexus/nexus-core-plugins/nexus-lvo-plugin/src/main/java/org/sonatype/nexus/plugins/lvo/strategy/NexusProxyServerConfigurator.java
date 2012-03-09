/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.lvo.strategy;

import org.apache.commons.httpclient.HttpClient;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.HttpClientProxyUtil;
import org.sonatype.spice.utils.proxyserver.ProxyServerConfigurator;

public class NexusProxyServerConfigurator
    implements ProxyServerConfigurator
{
    private Logger logger;
    private RemoteStorageContext ctx;
    
    public NexusProxyServerConfigurator( RemoteStorageContext ctx, Logger logger )
    {
        this.ctx = ctx;
        this.logger = logger;
    }
    
    public void applyToClient( HttpClient client )
    {   
        HttpClientProxyUtil.applyProxyToHttpClient( client, ctx, logger );
    }
}
