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
package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * Default group repository implementation.
 */
@Component( role = GroupRepository.class, instantiationStrategy = "per-lookup", description = "Default group repository" )
public class DefaultGroupRepository
    extends AbstractGroupRepository
{
    private ContentClass contentClass;

    private RepositoryKind repositoryKind = new DefaultRepositoryKind( GroupRepository.class, null );

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public void setRepositoryContentClass( ContentClass contentClass )
    {
        this.contentClass = contentClass;
    }

    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }
}
