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
package org.sonatype.nexus.proxy.maven.maven2;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.DefaultGroupRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.GroupRepositoryConfigurator;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

@Component( role = GroupRepositoryConfigurator.class, hint = "maven2" )
public class M2GroupRepositoryConfigurator
    extends DefaultGroupRepositoryConfigurator
{
    public GroupRepository updateRepositoryFromModel( GroupRepository old, ApplicationConfiguration configuration,
        CRepositoryGroup group, LocalRepositoryStorage ls )
        throws InvalidConfigurationException
    {
        return super.updateRepositoryFromModel( old, configuration, group, ls );
    }

}
