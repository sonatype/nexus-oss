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
package org.sonatype.nexus.proxy.registry;

/**
 * Thrown when invalid grouping is tried: for example grouping of repositories without same content class.
 * 
 * @author cstamas
 */
public class InvalidGroupingException
    extends Exception
{
    private static final long serialVersionUID = -738329028288324297L;

    public InvalidGroupingException( ContentClass c1, ContentClass c2 )
    {
        super( "The content classes are not groupable! '" + c1.getId() + "' and '" + c2.getId()
            + "' are not compatible!" );
    }
}
