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
import java.io.IOException;

import org.sonatype.nexus.configuration.model.Configuration;

/**
 * A component involved only if old Nexus configuration is found. It will fetch the old configuration, transform it to
 * current Configuration model and return it. Nothing else.
 * 
 * @author cstamas
 */
public interface ConfigurationUpgrader
{
    String ROLE = ConfigurationUpgrader.class.getName();

    /**
     * Tries to load an old configuration from file and will try to upgrade it to current model.
     * 
     * @param file
     * @return
     * @throws IOException
     * @throws ConfigurationIsCorruptedException
     * @throws UnsupportedConfigurationVersionException
     */
    public Configuration loadOldConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException,
            UnsupportedConfigurationVersionException;
}
