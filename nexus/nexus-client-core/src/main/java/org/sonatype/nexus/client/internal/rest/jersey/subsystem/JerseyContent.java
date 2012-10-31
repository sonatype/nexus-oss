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
package org.sonatype.nexus.client.internal.rest.jersey.subsystem;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.client.core.spi.SubsystemSupport;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * @since 2.1
 */
public class JerseyContent
    extends SubsystemSupport<JerseyNexusClient>
    implements Content
{

    private static final String CONTENT_PREFIX = "content/";

    public JerseyContent( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    public void download( final Location location, final File target )
        throws IOException
    {
        if ( !target.exists() )
        {
            final File targetDir = target.getParentFile();
            checkState(
                ( targetDir.exists() || targetDir.mkdirs() ) && targetDir.isDirectory(),
                "Directory '%s' does not exist and could not be created", targetDir.getAbsolutePath()
            );
        }
        else
        {
            checkState(
                target.isFile() && target.canWrite(),
                "File '%s' is not a file or could not be written", target.getAbsolutePath()
            );
        }

        final ClientResponse response =
            getNexusClient().uri( CONTENT_PREFIX + location.toContentPath() ).get( ClientResponse.class );

        try
        {
            if ( response.getStatus() >= 200 && response.getStatus() <= 299 )
            {
                FileOutputStream fos = null;
                try
                {
                    fos = new FileOutputStream( target );
                    IOUtil.copy( response.getEntityInputStream(), fos );
                }
                finally
                {
                    IOUtil.close( fos );
                }
            }
            else if ( response.getStatus() == 404 )
            {
                throw new FileNotFoundException( location.toString() );
            }
            else
            {
                throw new IOException( "Unexpected response: " + response.getClientResponseStatus() );
            }
        }
        finally
        {
            response.close();
        }
    }

    @Override
    public void upload( final Location location, final File target )
        throws IOException
    {
        final ClientResponse response =
            getNexusClient().uri( CONTENT_PREFIX + location.toContentPath() ).put( ClientResponse.class, target );
        try
        {
            if ( response.getStatus() >= 200 && response.getStatus() <= 299 )
            {
                // we are okay
            }
            else
            {
                throw new IOException( "Unexpected response: " + response.getClientResponseStatus() );
            }
        }
        finally
        {
            response.close();
        }
    }

    @Override
    public void delete( final Location location )
        throws IOException
    {
        final ClientResponse response =
            getNexusClient().uri( CONTENT_PREFIX + location.toContentPath() ).delete( ClientResponse.class );

        try
        {
            if ( response.getStatus() >= 200 && response.getStatus() <= 299 )
            {
                // we are okay
            }
            else if ( response.getStatus() == 404 )
            {
                throw new FileNotFoundException( location.toString() );
            }
            else
            {
                throw new IOException( "Unexpected response: " + response.getClientResponseStatus() );
            }
        }
        finally
        {
            response.close();
        }
    }
}
