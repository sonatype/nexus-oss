/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.micromailer.imp.DefaultMailType;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

/**
 * @author velo
 */
@Component( role = SmtpSettingsValidator.class )
public class DefaultSmtpSettingsValidator
    extends AbstractLoggingComponent
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
            throw new EmailerException( "Unable to create EMailer", e );
        }
        emailer.configure( config );

        MailRequest request = new MailRequest( NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID );
        request.setFrom( new Address( smtp.getSystemEmailAddress(), "Nexus Repository Manager" ) );
        request.getToAddresses().add( new Address( email ) );
        request.getBodyContext().put( DefaultMailType.SUBJECT_KEY, "Nexus: SMTP Configuration validation." );

        StringBuilder body = new StringBuilder();
        body.append( "Your current SMTP configuration is valid!" );

        request.getBodyContext().put( DefaultMailType.BODY_KEY, body.toString() );

        MailRequestStatus status = emailer.sendSyncedMail( request );

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
