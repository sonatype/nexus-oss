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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailComposer;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestSource;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.micromailer.MailSender;
import org.sonatype.micromailer.MailStorage;
import org.sonatype.micromailer.MailTypeSource;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

/**
 * Default {@link NexusEmailer}.
 * Keeps the emailer synchornized to Nexus configuration.
 *
 * @author cstamas
 * @author Alin Dreghiciu
 */
@Component( role = NexusEmailer.class )
public class DefaultNexusEmailer
    implements NexusEmailer, EventListener, Initializable
{

    @Requirement
    private EMailer m_emailer;

    @Requirement
    private NexusConfiguration m_nexusConfiguration;

    @Requirement
    private ApplicationEventMulticaster m_eventMulticaster;

    /**
     * Current SMTP configuration.
     */
    private CSmtpConfiguration m_smtpConfiguration;

    /**
     * Delegates to {@link EMailer}.
     * {@inheritDoc}
     */
    public void configure( final EmailerConfiguration config )
    {
        m_emailer.configure( config );
    }

    /**
     * Delegates to {@link EMailer}.
     * {@inheritDoc}
     */
    public MailComposer getMailComposer()
    {
        return m_emailer.getMailComposer();
    }

    /**
     * Delegates to {@link EMailer}.
     * {@inheritDoc}
     */
    public MailSender getMailSender()
    {
        return m_emailer.getMailSender();
    }

    /**
     * Delegates to {@link EMailer}.
     * {@inheritDoc}
     */
    public MailStorage getMailStorage()
    {
        return m_emailer.getMailStorage();
    }

    /**
     * Delegates to {@link EMailer}.
     * {@inheritDoc}
     */
    public MailTypeSource getMailTypeSource()
    {
        return m_emailer.getMailTypeSource();
    }

    /**
     * Delegates to {@link EMailer}.
     * {@inheritDoc}
     */
    public MailRequestStatus sendMail( final MailRequest request )
    {
        return m_emailer.sendMail( request );
    }

    /**
     * Delegates to {@link EMailer}.
     * {@inheritDoc}
     */
    public void sendMailBatch( final MailRequestSource mailRequestSource )
    {
        m_emailer.sendMailBatch( mailRequestSource );
    }

    /**
     * Current configured SMTP system email address.
     * {@inheritDoc}
     */
    public String getSystemEmailAddress()
    {
        return m_smtpConfiguration.getSystemEmailAddress();
    }

    /**
     * Accepts {@link ConfigurationChangeEvent}s of {@link ApplicationConfiguration}s.
     * {@inheritDoc}
     */
    private boolean accepts( final Event<?> evt )
    {
        return evt != null
               && evt instanceof ConfigurationChangeEvent
               && ( (ConfigurationChangeEvent) evt ).getApplicationConfiguration() != null;
    }

    /**
     * Update smtp configuration if changed.
     * {@inheritDoc}
     */
    public void onEvent( final Event<?> evt )
    {
        if( !( accepts( evt ) ) )
        {
            return;
        }
        final ApplicationConfiguration config = ( (ConfigurationChangeEvent) evt ).getApplicationConfiguration();
        final CSmtpConfiguration newSmtp = config.getConfiguration().getSmtpConfiguration();
        if( configChanged( newSmtp ) )
        {
            updateConfig();
        }
    }

    /**
     * Initial emailer configuration.
     *
     * {@inheritDoc}
     */
    public void initialize()
    {
        m_eventMulticaster.addEventListener( this );
        // now initialize the smtp config
        configChanged( m_nexusConfiguration.getConfiguration().getSmtpConfiguration() );
        // update the config on the mailer component
        updateConfig();
    }

    /**
     * Configures emailer based on current SMTP configuration.
     */
    private void updateConfig()
    {
        final EmailerConfiguration config = new EmailerConfiguration();
        config.setDebug( m_smtpConfiguration.isDebugMode() );
        config.setMailHost( m_smtpConfiguration.getHostname() );
        config.setMailPort( m_smtpConfiguration.getPort() );
        config.setPassword( m_smtpConfiguration.getPassword() );
        config.setSsl( m_smtpConfiguration.isSslEnabled() );
        config.setTls( m_smtpConfiguration.isTlsEnabled() );
        config.setUsername( m_smtpConfiguration.getUsername() );

        m_emailer.configure( config );
    }

    /**
     * Updates configuration (if changed).
     *
     * @param newSmtp new configuration
     *
     * @return if teh new configuration differs then old one
     */
    boolean configChanged( final CSmtpConfiguration newSmtp )
    {
        if( m_smtpConfiguration == null
            || ( m_smtpConfiguration.getHostname() == null
                 && newSmtp.getHostname() != null )
            || ( m_smtpConfiguration.getHostname() != null
                 && !m_smtpConfiguration.getHostname().equals( newSmtp.getHostname() ) )
            || ( m_smtpConfiguration.getUsername() == null
                 && newSmtp.getUsername() != null )
            || ( m_smtpConfiguration.getUsername() != null
                 && !m_smtpConfiguration.getUsername().equals( newSmtp.getUsername() ) )
            || ( m_smtpConfiguration.getPassword() == null
                 && newSmtp.getPassword() != null )
            || ( m_smtpConfiguration.getPassword() != null
                 && !m_smtpConfiguration.getPassword().equals( newSmtp.getPassword() ) )
            || !( m_smtpConfiguration.getPort() == newSmtp.getPort() )
            || ( m_smtpConfiguration.getSystemEmailAddress() == null
                 && newSmtp.getSystemEmailAddress() != null )
            || ( m_smtpConfiguration.getSystemEmailAddress() != null
                 && !m_smtpConfiguration.getSystemEmailAddress().equals( newSmtp.getSystemEmailAddress() ) )
            || !( m_smtpConfiguration.isSslEnabled() == newSmtp.isSslEnabled() )
            || !( m_smtpConfiguration.isTlsEnabled() == newSmtp.isTlsEnabled() )
            || !( m_smtpConfiguration.isDebugMode() == newSmtp.isDebugMode() ) )
        {
            m_smtpConfiguration = newSmtp;
            return true;
        }

        return false;
    }


}