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
package org.sonatype.nexus.email;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.imp.DefaultMailType;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;

/**
 * The default emailer.
 * 
 * @author cstamas
 */
@Component( role = NexusEmailer.class )
public class DefaultNexusEmailer
    implements NexusEmailer, EventListener, Initializable
{
    @Requirement
    private EMailer emailer;

    @Requirement
    private NexusConfiguration configuration;

    private CSmtpConfiguration smtp;

    private static final String NEXUS_MAIL_ID = "Nexus";

    public void sendNewUserCreated( String email, String userid, String password )
    {
        MailRequest request = new MailRequest( NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID );
        request.setFrom( new Address( smtp.getSystemEmailAddress(), "Nexus Repository Manager" ) );
        request.getToAddresses().add( new Address( email ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: New user account created." );

        StringBuilder body = new StringBuilder();
        body.append( "User Account " );
        body.append( userid );
        body.append( " has been created.  Another email will be sent shortly containing your password." );

        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        emailer.sendMail( request );

        request = new MailRequest( NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID );
        request.setFrom( new Address( smtp.getSystemEmailAddress(), "Nexus Repository Manager" ) );
        request.getToAddresses().add( new Address( email ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: New user account created." );

        body = new StringBuilder();
        body.append( "Your new password is " );
        body.append( password );

        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        emailer.sendMail( request );
    }

    public void sendForgotUsername( String email, List<String> userIds )
    {
        MailRequest request = new MailRequest( NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID );
        request.setFrom( new Address( smtp.getSystemEmailAddress(), "Nexus Repository Manager" ) );
        request.getToAddresses().add( new Address( email ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: User account notification." );

        StringBuilder body = new StringBuilder();

        body.append( "Your email is associated with the following Nexus User Id(s):\n " );
        for ( String userId : userIds )
        {
            body.append( "\n - \"" );
            body.append( userId );
            body.append( "\"" );
        }

        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        emailer.sendMail( request );
    }

    public void sendResetPassword( String email, String password )
    {
        MailRequest request = new MailRequest( NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID );
        request.setFrom( new Address( smtp.getSystemEmailAddress(), "Nexus Repository Manager" ) );
        request.getToAddresses().add( new Address( email ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: User account notification." );

        StringBuilder body = new StringBuilder();
        body.append( "Your password has been reset.  Your new password is: " );
        body.append( password );

        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        emailer.sendMail( request );
    }

    public void initialize()
        throws InitializationException
    {
        configuration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            if ( ApplicationConfiguration.class.isAssignableFrom( ( (ConfigurationChangeEvent) evt )
                .getNotifiableConfiguration().getClass() ) )
            {
                ApplicationConfiguration config = (ApplicationConfiguration) ( (ConfigurationChangeEvent) evt )
                    .getNotifiableConfiguration();

                CSmtpConfiguration newSmtp = config.getConfiguration().getSmtpConfiguration();

                if ( configChanged( newSmtp ) )
                {
                    updateConfig();
                }
            }
        }
    }

    private void updateConfig()
    {
        EmailerConfiguration config = new EmailerConfiguration();
        config.setDebug( smtp.isDebugMode() );
        config.setMailHost( smtp.getHost() );
        config.setMailPort( smtp.getPort() );
        config.setPassword( smtp.getPassword() );
        config.setSsl( smtp.isSslEnabled() );
        config.setTls( smtp.isTlsEnabled() );
        config.setUsername( smtp.getUsername() );

        emailer.configure( config );
    }

    protected boolean configChanged( CSmtpConfiguration newSmtp )
    {
        if ( smtp == null 
            || ( smtp.getHost() == null && newSmtp.getHost() != null )
            || ( smtp.getHost() != null && !smtp.getHost().equals( newSmtp.getHost() ) )
            || ( smtp.getUsername() == null && newSmtp.getUsername() != null )
            || ( smtp.getUsername() != null && !smtp.getUsername().equals( newSmtp.getUsername() ) )
            || ( smtp.getPassword() == null && newSmtp.getPassword() != null )
            || ( smtp.getPassword() != null && !smtp.getPassword().equals( newSmtp.getPassword() ) ) 
            || !( smtp.getPort() == newSmtp.getPort() )
            || ( smtp.getSystemEmailAddress() == null && newSmtp.getSystemEmailAddress() != null )
            || ( smtp.getSystemEmailAddress() != null && !smtp.getSystemEmailAddress().equals( newSmtp.getSystemEmailAddress() ) )
            || !( smtp.isSslEnabled() == newSmtp.isSslEnabled() ) 
            || !( smtp.isTlsEnabled() == newSmtp.isTlsEnabled() )
            || !( smtp.isDebugMode() == newSmtp.isDebugMode() ) )
        {
            smtp = newSmtp;
            return true;
        }

        return false;
    }
}
