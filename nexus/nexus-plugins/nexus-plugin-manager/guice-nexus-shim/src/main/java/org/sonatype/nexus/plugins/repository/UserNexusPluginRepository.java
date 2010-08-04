/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugins.repository;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * {@link File} backed {@link NexusWritablePluginRepository} that supplies user plugins.
 */
@Named( UserNexusPluginRepository.ID )
@Singleton
final class UserNexusPluginRepository
    extends AbstractFileNexusWritablePluginRepository
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String ID = "user";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Inject
    @Named( "${nexus-work}/plugin-repository" )
    private File userPluginsFolder;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getId()
    {
        return ID;
    }

    public int getPriority()
    {
        return 50;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected File getNexusPluginsDirectory()
    {
        if ( !userPluginsFolder.exists() )
        {
            userPluginsFolder.mkdirs();
        }
        return userPluginsFolder;
    }
}
