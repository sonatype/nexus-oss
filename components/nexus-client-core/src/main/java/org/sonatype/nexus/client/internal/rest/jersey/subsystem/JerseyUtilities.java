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

import java.util.Date;

import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.Utilities;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;

/**
 * @since 2.1
 */
public class JerseyUtilities
    extends SubsystemSupport<JerseyNexusClient>
    implements Utilities
{

    public JerseyUtilities( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    public Date getLastModified( final String uri )
    {
        try
        {
            final ClientResponse response = getNexusClient()
                .uri( uri )
                .head();

            return response.getLastModified();
        }
        catch ( ClientHandlerException e )
        {
            throw getNexusClient().convert( e );
        }
    }
}
