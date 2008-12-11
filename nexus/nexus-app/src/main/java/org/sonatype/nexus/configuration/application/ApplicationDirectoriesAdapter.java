/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Adapter for NexusConfiguration.
 * 
 * @author cstamas
 */
@Component( role = ApplicationDirectories.class )
public class ApplicationDirectoriesAdapter
    implements ApplicationDirectories
{
    @Requirement
    private NexusConfiguration nexusConfiguration;

    public File getConfigurationDirectory()
    {
        return nexusConfiguration.getConfigurationDirectory();
    }

    public File getTemporaryDirectory()
    {
        return nexusConfiguration.getTemporaryDirectory();
    }

    public File getWastebasketDirectory()
    {
        return nexusConfiguration.getWastebasketDirectory();
    }

    public File getWorkingDirectory()
    {
        return nexusConfiguration.getWorkingDirectory();
    }

    public File getWorkingDirectory( String key )
    {
        return nexusConfiguration.getWorkingDirectory( key );
    }

    public boolean isSecurityEnabled()
    {
        return nexusConfiguration.isSecurityEnabled();
    }
}
