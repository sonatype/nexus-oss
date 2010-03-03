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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;

/**
 * {@link File} backed {@link NexusPluginRepository} that supplies system plugins.
 */
@Component( role = NexusPluginRepository.class, hint = SystemNexusPluginRepository.ID )
final class SystemNexusPluginRepository
    extends AbstractFileNexusPluginRepository
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final String ID = "system";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    @Configuration( value = "${nexus-app}/plugin-repository" )
    private File systemPluginsFolder;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getId()
    {
        return ID;
    }

    public int getPriority()
    {
        return 0;
    }

    // ----------------------------------------------------------------------
    // Customized methods
    // ----------------------------------------------------------------------

    @Override
    protected File getNexusPluginsDirectory()
    {
        if ( !systemPluginsFolder.exists() )
        {
            systemPluginsFolder.mkdirs();
        }
        return systemPluginsFolder;
    }
}
