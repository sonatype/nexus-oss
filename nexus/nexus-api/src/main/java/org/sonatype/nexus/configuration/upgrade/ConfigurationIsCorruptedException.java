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
package org.sonatype.nexus.configuration.upgrade;

import java.io.File;

import org.sonatype.nexus.configuration.ConfigurationException;

/**
 * Thrown when the configuration file is corrupt and cannot be loaded neither upgraded. It has wrong syntax or is
 * unreadable.
 * 
 * @author cstamas
 */
public class ConfigurationIsCorruptedException
    extends ConfigurationException
{
    private static final long serialVersionUID = 5592204171297423008L;

    public ConfigurationIsCorruptedException( File file )
    {
        this( file.getAbsolutePath() );
    }

    public ConfigurationIsCorruptedException( String filePath )
    {
        this( filePath, null );
    }

    public ConfigurationIsCorruptedException( String filePath, Throwable t )
    {
        super( "Could not read or parse Nexus configuration file on path " + filePath + "! It may be corrupted.", t );
    }

}
