/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
