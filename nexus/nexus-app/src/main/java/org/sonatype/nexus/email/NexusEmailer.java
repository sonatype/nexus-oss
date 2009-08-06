package org.sonatype.nexus.email;

import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.nexus.configuration.Configurable;

/**
 * Simple service interface to expose the properly configured MicroMailer and also some helper methods for creating and
 * sending emails. This component will also keep EMailer configuration in sync with Nexus.
 * 
 * @author cstamas
 */
public interface NexusEmailer
    extends Configurable
{
    /**
     * Gets the preconfigured EMailer instance for prepared for using it.
     * 
     * @return
     */
    EMailer getEMailer();

    /**
     * Returns the system-wide default mail type used as default mailType for outgoing mails.
     * 
     * @return
     */
    String getDefaultMailTypeId();

    /**
     * Returns a prepopulated MailRequest. The request only needs to set the To, CC, Bcc (or override any of the
     * defaulted values) and send it.
     * 
     * @param subject the string used for subject. May be Velocity template, but the API consumer should take care to
     *            populate the request context then.
     * @param body the string used for body. May be Velocity template, but the API consumer should take care to populate
     *            the request context then.
     * @return
     */
    MailRequest getDefaultMailRequest( String subject, String body );

    /**
     * A shortcut method.
     * 
     * @param request
     * @return
     */
    MailRequestStatus sendMail( MailRequest request );

    // ==

    String getSMTPHostname();

    void setSMTPHostname( String host );

    int getSMTPPort();

    void setSMTPPort( int port );

    boolean isSMTPSslEnabled();

    void setSMTPSslEnabled( boolean val );

    boolean isSMTPTlsEnabled();

    void setSMTPTlsEnabled( boolean val );

    String getSMTPUsername();

    void setSMTPUsername( String username );

    String getSMTPPassword();

    void setSMTPPassword( String password );

    Address getSMTPSystemEmailAddress();

    void setSMTPSystemEmailAddress( Address adr );

    boolean isSMTPDebug();

    void setSMTPDebug( boolean val );
}
