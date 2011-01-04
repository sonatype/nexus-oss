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
