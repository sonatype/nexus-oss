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
 * Top level abstract class that is superclass for all authorization related exceptions.
 * 
 * @author cstamas
 */
public abstract class AuthorizationException
    extends Exception
{
    private static final long serialVersionUID = 391662938886542734L;

    public AuthorizationException( String message )
    {
        super( message );
    }

    public AuthorizationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public AuthorizationException( Throwable cause )
    {
        super( cause );
    }
}
