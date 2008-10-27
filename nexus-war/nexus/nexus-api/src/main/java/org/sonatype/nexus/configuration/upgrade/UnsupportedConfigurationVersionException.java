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
 * Thrown when the configuration has model version but it is unknown to Nexus.
 * 
 * @author cstamas
 */
public class UnsupportedConfigurationVersionException
    extends ConfigurationException
{
    private static final long serialVersionUID = 1965812260368747123L;

    public UnsupportedConfigurationVersionException( String version, File file )
    {
        super( "Unsupported configuration file in " + file.getAbsolutePath() + " with version: " + version
            + ". Cannot upgrade." );
    }
}
