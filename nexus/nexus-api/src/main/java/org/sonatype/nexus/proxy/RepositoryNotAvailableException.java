/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.proxy;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Thrown if the repository involved in processing is not available.
 * 
 * @author cstamas
 */
public class RepositoryNotAvailableException
    extends IllegalOperationException
{
    private static final long serialVersionUID = 6414483658234772613L;

    private final Repository repository;

    public RepositoryNotAvailableException( Repository repository )
    {
        super( "Repository with ID='" + repository.getId() + "' is not available!" );

        this.repository = repository;
    }

    public Repository getRepository()
    {
        return repository;
    }

}
