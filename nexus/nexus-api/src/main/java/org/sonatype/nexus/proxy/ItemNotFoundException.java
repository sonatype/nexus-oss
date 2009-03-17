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
package org.sonatype.nexus.proxy;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Thrown if the requested item is not found.
 * 
 * @author cstamas
 */
public class ItemNotFoundException
    extends Exception
{
    private static final long serialVersionUID = -4964273361722823796L;

    private final Repository repository;

    private final ResourceStoreRequest request;

    public ItemNotFoundException( String path )
    {
        this( path, null );
    }

    public ItemNotFoundException( String path, Throwable cause )
    {
        super( "Item not found on path " + path, cause );

        this.repository = null;

        this.request = null;
    }

    public ItemNotFoundException( ResourceStoreRequest request, Repository repository )
    {
        this( request, repository, null );
    }

    public ItemNotFoundException( ResourceStoreRequest request, Repository repository, Throwable cause )
    {
        super( "Item not found on path " + request.toString() + " in repository " + repository.getId(), cause );

        this.repository = repository;

        this.request = request;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public ResourceStoreRequest getRequest()
    {
        return request;
    }
}
