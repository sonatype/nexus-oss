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
package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{
    public String getPreHeadContribution( Map<String, Object> context )
    {
        return null;
    }

    public String getPostHeadContribution( Map<String, Object> context )
    {
        return null;
    }

    public String getPreBodyContribution( Map<String, Object> context )
    {
        return null;
    }

    public String getPostBodyContribution( Map<String, Object> context )
    {
        return null;
    }

    // ==

    protected String getVersionFromJarFile( String path )
    {
        Properties props = new Properties();

        InputStream is = getClass().getResourceAsStream( path );

        if ( is != null )
        {
            try
            {
                props.load( is );
            }
            catch ( IOException e )
            {
                // no prop file ?? back out
                return null;
            }
        }

        return props.getProperty( "version" );
    }
}
