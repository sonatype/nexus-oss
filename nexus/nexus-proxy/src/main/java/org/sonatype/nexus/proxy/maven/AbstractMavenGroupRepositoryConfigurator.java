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
package org.sonatype.nexus.proxy.maven;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepositoryConfigurator;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public abstract class AbstractMavenGroupRepositoryConfigurator
    extends AbstractGroupRepositoryConfigurator
{
    protected void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration,
        CRepositoryCoreConfiguration coreConfiguration )
        throws ConfigurationException
    {
        super.doApplyConfiguration( repository, configuration, coreConfiguration );
        
        // now check the content type of these repos
        GroupRepository groupRepo = (GroupRepository) repository;
        
        for ( Repository memberRepo : groupRepo.getMemberRepositories() )
        {
            
        }
    }
}
