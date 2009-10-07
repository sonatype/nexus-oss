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

/**
 * IllegalOperationException is thrown when an illegal action is tried against a ResourceStore (ie. write to a read only,
 * unavailable, etc.). Previously it was (wrongly) AccessDeniedException used to mark these problems, and it caused
 * problems on REST API to distinct an "authz problem = accessDenied = HTTP 401" and "bad request = HTTP 400".
 * 
 * @author cstamas
 */
public abstract class IllegalOperationException
    extends Exception
{
    private static final long serialVersionUID = -1075426559861827023L;

    public IllegalOperationException( String message )
    {
        super( message );
    }

    public IllegalOperationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public IllegalOperationException( Throwable cause )
    {
        super( cause );
    }
}
