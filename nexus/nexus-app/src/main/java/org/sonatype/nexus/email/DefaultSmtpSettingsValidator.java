package org.sonatype.nexus.email;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.micromailer.imp.DefaultMailType;
import org.sonatype.nexus.configuration.modello.CSmtpConfiguration;

/**
 * @author velo
 */
@Component( role = SmtpSettingsValidator.class )
public class DefaultSmtpSettingsValidator
    extends AbstractLogEnabled
    implements SmtpSettingsValidator, Contextualizable
{

    private PlexusContainer plexusContainer;

    private static final String NEXUS_MAIL_ID = "Nexus";

    public boolean sendSmtpConfigurationTest( CSmtpConfiguration smtp, String email )
        throws EmailerException
    {
        EmailerConfiguration config = new EmailerConfiguration();
        config.setDebug( smtp.isDebugMode() );
        config.setMailHost( smtp.getHostname() );
        config.setMailPort( smtp.getPort() );
        config.setPassword( smtp.getPassword() );
        config.setSsl( smtp.isSslEnabled() );
        config.setTls( smtp.isTlsEnabled() );
        config.setUsername( smtp.getUsername() );

        EMailer emailer;
        try
        {
            emailer = plexusContainer.lookup( EMailer.class );
        }
        catch ( ComponentLookupException e )
        {
            throw new EmailerException("Unable to create EMailer", e);
        }
        emailer.configure( config );

        MailRequest request = new MailRequest( NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID );
        request.setFrom( new Address( smtp.getSystemEmailAddress(), "Nexus Repository Manager" ) );
        request.getToAddresses().add( new Address( email ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: Stmp Configuration validation." );

        StringBuilder body = new StringBuilder();
        body.append( "Your current SMTP configuration is valid!" );

        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        MailRequestStatus status = emailer.sendMail( request );

        for ( int i = 0; i < 1000; i++ )
        {
            Thread.yield();
            try
            {
                Thread.sleep( 1 );
            }
            catch ( InterruptedException e )
            {
                // meanless
            }

            if ( status.isSent() || status.getErrorCause() != null )
            {
                break;
            }
        }

        if ( status.getErrorCause() != null )
        {
            getLogger().error( "Unable to send e-mail", status.getErrorCause() );
            throw new EmailerException( "Unable to send e-mail", status.getErrorCause() );
        }

        return status.isSent();
    }

    public void contextualize( Context context )
        throws ContextException
    {
        plexusContainer = (PlexusContainer) context.get( "plexus" );
    }
}
