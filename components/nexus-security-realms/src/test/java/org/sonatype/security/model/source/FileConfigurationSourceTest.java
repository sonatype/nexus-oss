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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileConfigurationSourceTest
    extends AbstractSecurityConfigurationSourceTest

{
    protected SecurityModelConfigurationSource getConfigurationSource()
        throws Exception
    {
        FileModelConfigurationSource source =
            (FileModelConfigurationSource) lookup( SecurityModelConfigurationSource.class, "file" );

        source.setConfigurationFile( new File( getSecurityConfiguration() ) );

        return source;
    }

    protected InputStream getOriginatingConfigurationInputStream()
        throws IOException
    {
        return getClass().getResourceAsStream( "/META-INF/security/security.xml" );
    }

    public void testStoreConfiguration()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        configurationSource.loadConfiguration();

        try
        {
            configurationSource.storeConfiguration();
        }
        catch ( UnsupportedOperationException e )
        {
            fail();
        }
    }

    public void testIsConfigurationUpgraded()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        configurationSource.loadConfiguration();

        assertEquals( false, configurationSource.isConfigurationUpgraded() );
    }

    public void testIsConfigurationDefaulted()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        configurationSource.loadConfiguration();

        assertEquals( true, configurationSource.isConfigurationDefaulted() );
    }

    public void testIsConfigurationDefaultedShouldNot()
        throws Exception
    {
        copyDefaultSecurityConfigToPlace();

        configurationSource = getConfigurationSource();

        configurationSource.loadConfiguration();

        assertEquals( false, configurationSource.isConfigurationDefaulted() );
    }

    // NOT EXPOSED
    // public void testGetDefaultsSource()
    // throws Exception
    // {
    // configurationSource = getConfigurationSource();
    //
    // assertFalse( configurationSource.getDefaultsSource() == null );
    // }
}
