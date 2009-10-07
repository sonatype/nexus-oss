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
package org.sonatype.nexus.configuration;

import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.plugin.ExtensionPoint;

/**
 * A component responsible for "apply" (config -> repo) and "prepare" (repo -> config) steps for all those config
 * elements that does not map directly to a model and some extra processing is needed.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface Configurator
{
    /**
     * Will apply the configuration parameters from coreConfiguratuin to the target.
     */
    void applyConfiguration( Object target, ApplicationConfiguration configuration, CoreConfiguration coreConfiguration )
        throws ConfigurationException;

    /**
     * Will prepare model for save, by syncing it with target state (if needed).
     */
    void prepareForSave( Object target, ApplicationConfiguration configuration, CoreConfiguration coreConfiguration );
}
