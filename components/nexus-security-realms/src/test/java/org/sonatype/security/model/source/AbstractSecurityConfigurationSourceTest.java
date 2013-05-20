/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.model.source;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.security.model.AbstractSecurityConfigTest;

public abstract class AbstractSecurityConfigurationSourceTest
    extends AbstractSecurityConfigTest
{

    protected SecurityModelConfigurationSource configurationSource;

    protected abstract SecurityModelConfigurationSource getConfigurationSource()
        throws Exception;

    protected abstract InputStream getOriginatingConfigurationInputStream()
        throws IOException;

    public void testConfigStream()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        // not using load here since File config would load it and store it
        // thus changing it (but no content change!)
        copyDefaultSecurityConfigToPlace();

        InputStream configStream = null;

        InputStream origStream = null;

        try
        {
            configStream = configurationSource.getConfigurationAsStream();

            origStream = getOriginatingConfigurationInputStream();

            assertTrue( IOUtil.contentEquals( configStream, origStream ) );
        }
        finally
        {
            IOUtil.close( origStream );

            IOUtil.close( configStream );
        }
    }

    public void testGetConfiguration()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        assertTrue( configurationSource.getConfiguration() == null );

        configurationSource.loadConfiguration();

        assertFalse( configurationSource.getConfiguration() == null );
    }

}
