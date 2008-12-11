/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.application.runtime;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * A component to be slimmed! Actually, it is a "factory" (backed by Plexus) that creates repo and other instances. It
 * should realy onto plexus as much can.
 * 
 * @author cstamas
 */
public interface ApplicationRuntimeConfigurationBuilder
{
    void initialize( NexusConfiguration configuration );

    Repository createRepositoryFromModel( Configuration configuration, CRepository repository )
        throws InvalidConfigurationException;

    Repository updateRepositoryFromModel( Repository old, Configuration configuration, CRepository repository )
        throws InvalidConfigurationException;

    ShadowRepository createRepositoryFromModel( Configuration configuration, CRepositoryShadow repositoryShadow )
        throws InvalidConfigurationException;

    ShadowRepository updateRepositoryFromModel( ShadowRepository old, Configuration configuration,
        CRepositoryShadow repositoryShadow )
        throws InvalidConfigurationException;
}
