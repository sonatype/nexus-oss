/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
