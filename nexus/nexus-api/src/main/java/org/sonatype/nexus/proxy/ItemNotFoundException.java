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

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * Thrown if the requested item is not found.
 * 
 * @author cstamas
 */
public class ItemNotFoundException
    extends Exception
{
    private static final long serialVersionUID = -4964273361722823796L;

    public ItemNotFoundException( String path )
    {
        super( "Item not found on path " + path );
    }

    public ItemNotFoundException( String path, String repo )
    {
        super( "Item not found on path " + path + " in repository " + repo );
    }

    public ItemNotFoundException( RepositoryItemUid uid )
    {
        super( "Item not found on UID " + uid.toString() + " in repository " + uid.getRepository().getId() );
    }

}
