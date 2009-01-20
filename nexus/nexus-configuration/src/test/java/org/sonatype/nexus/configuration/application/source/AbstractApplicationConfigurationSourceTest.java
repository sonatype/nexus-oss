/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration.application.source;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;

public abstract class AbstractApplicationConfigurationSourceTest
    extends AbstractNexusTestCase
{
    protected ApplicationConfigurationSource configurationSource;

    protected abstract ApplicationConfigurationSource getConfigurationSource()
        throws Exception;

    protected abstract InputStream getOriginatingConfigurationInputStream()
        throws IOException;
    
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
