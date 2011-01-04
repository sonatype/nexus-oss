/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nxcm970;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

public class ContinuousDeployer
    implements Runnable
{
    private HttpClient httpClient;

    private volatile boolean deploying;

    private final String targetUrl;

    private int result = -1;

    public ContinuousDeployer( String targetUrl )
    {
        super();

        this.targetUrl = targetUrl;

        this.deploying = true;

        this.httpClient = new HttpClient();
    }

    public boolean isDeploying()
    {
        return deploying;
    }

    public void finishDeploying()
    {
        this.deploying = false;
    }

    public boolean isFinished()
    {
        return result != -1;
    }

    public int getResult()
    {
        return result;
    }

    public void run()
    {
        PutMethod method = new PutMethod( targetUrl );

        method.setRequestEntity( new InputStreamRequestEntity( new EndlessBlockingInputStream( this ) ) );

        try
        {
            result = httpClient.executeMethod( method );
        }
        catch ( Exception e )
        {
            result = -2;

            e.printStackTrace();
        }
    }

    /**
     * This is an endless stream, that will sleep a little and then serve the 'O' character.
     * 
     * @author cstamas
     */
    public static class EndlessBlockingInputStream
        extends InputStream
    {
        private final ContinuousDeployer continuousDeployer;

        public EndlessBlockingInputStream( ContinuousDeployer deployer )
        {
            this.continuousDeployer = deployer;
        }

        @Override
        public int read()
            throws IOException
        {
            if ( continuousDeployer.isDeploying() )
            {
                try
                {
                    Thread.sleep( 300 );

                    return 'T';
                }
                catch ( InterruptedException e )
                {
                    throw new IOException( e.getMessage() );
                }
            }
            else
            {
                // finish
                return -1;
            }
        }
    }
}
