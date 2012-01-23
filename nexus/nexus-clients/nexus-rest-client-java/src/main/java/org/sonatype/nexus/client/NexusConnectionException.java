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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.plexus.rest.resource.error.ErrorMessage;

/**
 * Thrown when a NexusClient cannot connect to a Nexus instance, or the Nexus instance returns a non success response.
 */
public class NexusConnectionException
    extends Exception
{

    /**
     * Errors returned from a Nexus server.
     */
    private List<ErrorMessage> errors = new ArrayList<ErrorMessage>();

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -5163493126499979929L;

    public NexusConnectionException()
    {
        super();
    }

    public NexusConnectionException( List<ErrorMessage> errors )
    {
        super();
        this.errors = errors;
    }

    public NexusConnectionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NexusConnectionException( String message, Throwable cause, List<ErrorMessage> errors  )
    {
        super( message, cause );
        this.errors = errors;

    }

    public NexusConnectionException( String message )
    {
        super( message );
    }


    public NexusConnectionException( String message, List<ErrorMessage> errors  )
    {
        super( message );
        this.errors = errors;
    }

    public NexusConnectionException( Throwable cause )
    {
        super( cause );
    }

    public NexusConnectionException( Throwable cause, List<ErrorMessage> errors  )
    {
        super( cause );
        this.errors = errors;
    }

    /**
     * A list of errors returned from the server, if any.  Could be empty or null.
     *
     * @return A List of errors returned from the server.
     */
    public List<ErrorMessage> getErrors()
    {
        return errors;
    }

    @Override
    public String getMessage()
    {
        StringBuffer message = new StringBuffer(super.getMessage());

        if(this.getErrors() != null)
        {
            for ( ErrorMessage error : this.getErrors() )
            {
                message.append( "\n" ).append( error.getMsg() );
            }
        }

        return message.toString();
    }



}
