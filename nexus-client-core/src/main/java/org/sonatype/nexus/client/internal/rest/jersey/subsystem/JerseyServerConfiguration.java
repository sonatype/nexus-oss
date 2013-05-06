/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.client.internal.rest.jersey.subsystem;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.ServerConfiguration;
import org.sonatype.nexus.client.core.subsystem.config.HttpProxy;
import org.sonatype.nexus.client.internal.rest.jersey.subsystem.config.JerseyHttpProxy;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;

/**
 * @since 2.2
 */
public class JerseyServerConfiguration
    extends SubsystemSupport<JerseyNexusClient>
    implements ServerConfiguration
{

    /**
     * Http Proxy configuration segment.
     * Lazy initialized on first request.
     */
    private HttpProxy httpProxy;

    public JerseyServerConfiguration( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    public HttpProxy proxySettings()
    {
        if ( httpProxy == null )
        {
            httpProxy = new JerseyHttpProxy( getNexusClient() );
        }
        return httpProxy;
    }

}
