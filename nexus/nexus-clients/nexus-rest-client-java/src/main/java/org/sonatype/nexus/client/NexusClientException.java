/**
 * Sonatype Nexus™ [Open Source Version].
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
package org.sonatype.nexus.client;

/**
 *  Thrown when unexpected problem occur on the client side. 
 */
public class NexusClientException
    extends Exception
{

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = 989102224012468495L;

    public NexusClientException()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public NexusClientException( String message, Throwable cause )
    {
        super( message, cause );
        // TODO Auto-generated constructor stub
    }

    public NexusClientException( String message )
    {
        super( message );
        // TODO Auto-generated constructor stub
    }

    public NexusClientException( Throwable cause )
    {
        super( cause );
        // TODO Auto-generated constructor stub
    }

    
    
}
