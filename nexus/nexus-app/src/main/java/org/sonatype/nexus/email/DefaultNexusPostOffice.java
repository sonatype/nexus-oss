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
package org.sonatype.nexus.email;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.MailRequest;

/**
 * The default nexus post office.
 * 
 * @author Alin Dreghiciu
 */
@Component( role = NexusPostOffice.class )
public class DefaultNexusPostOffice
    implements NexusPostOffice
{
    @Requirement
    private NexusEmailer nexusEmailer;

    /**
     * {@inheritDoc}
     */
    public void sendNexusTaskFailure( final String email, final String taskId, final String taskName,
                                      final Throwable cause )
    {
        final StringBuilder body = new StringBuilder();
        
        if ( taskId != null )
        {
            body.append( String.format( "Task ID: %s", taskId ) ).append( "\n" );
        }
        
        if ( taskName != null )
        {
            body.append( String.format( "Task Name: %s", taskName ) ).append( "\n" );
        }
        
        if ( cause != null )
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter( sw );
            cause.printStackTrace( pw );
            body.append( "Stack trace: " ).append( "\n" ).append( sw.toString() );
        }

        MailRequest request = nexusEmailer.getDefaultMailRequest( "Nexus: Task execution failure", body.toString() );

        request.getToAddresses().add( new Address( email ) );

        nexusEmailer.sendMail( request );
    }

}