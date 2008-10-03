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
