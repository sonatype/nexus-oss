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
package org.sonatype.nexus.email;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.imp.DefaultMailType;

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
    private NexusEmailer m_emailer;

    /**
     * {@inheritDoc}
     */
    public void sendNexusTaskFailure( final String email,
                                      final String taskId,
                                      final String taskName,
                                      final Throwable cause )
    {
        final MailRequest request = new MailRequest( NexusEmailer.NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID );
        request.setFrom( new Address( m_emailer.getSystemEmailAddress(), "Nexus Repository Manager" ) );
        request.getToAddresses().add( new Address( email ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: Task execution failure" );

        final StringBuilder body = new StringBuilder();
        if( taskId != null )
        {
            body.append( String.format( "Task ID: %s", taskId ) ).append( "\n" );
        }
        if( taskName != null )
        {
            body.append( String.format( "Task Name: %s", taskName ) ).append( "\n" );
        }
        if( cause != null )
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter( sw );
            cause.printStackTrace( pw );
            body.append( "Stack trace: " )
                .append( "\n" )
                .append( sw.toString() );
        }

        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        m_emailer.sendMail( request );
    }
    
}