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

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.plugin.ExtensionPoint;

/**
 * A component responsible for "apply" (config -> repo) and "prepare" (repo -> config) steps.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface Configurator
{
    /**
     * Will apply the configuration parameters from repo model to the repository.
     */
    void applyConfiguration( Object target, ApplicationConfiguration configuration, CoreConfiguration coreConfiguration )
        throws ConfigurationException;

    /**
     * Will prepare repo model for save, by syncing it with repository state (if needed).
     */
    void prepareForSave( Object repository, ApplicationConfiguration configuration, CoreConfiguration coreConfiguration );
}
