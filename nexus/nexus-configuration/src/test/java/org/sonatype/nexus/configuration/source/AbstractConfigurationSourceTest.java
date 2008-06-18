/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.source;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;

public abstract class AbstractConfigurationSourceTest
    extends AbstractNexusTestCase
{
    protected ConfigurationSource configurationSource;

    protected abstract ConfigurationSource getConfigurationSource()
        throws Exception;

    protected abstract InputStream getOriginatingConfigurationInputStream()
        throws IOException;

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( PLEXUS_HOME );
    }

    public void testConfigStream()
        throws Exception
    {
        configurationSource = getConfigurationSource();

        // not using load here since File config would load it and store it
        // thus changing it (but no content change!)
        copyDefaultConfigToPlace();
        
        InputStream configStream = null;
        
        try
        {
            configStream = configurationSource.getConfigurationAsStream();
            
            assertTrue( IOUtil.contentEquals( configStream, getOriginatingConfigurationInputStream() ) );
        }
        finally
        {
            if ( configStream != null )
            {
                configStream.close();
            }
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
