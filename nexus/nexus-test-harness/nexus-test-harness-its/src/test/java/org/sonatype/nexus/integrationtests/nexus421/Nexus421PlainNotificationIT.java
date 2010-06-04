package org.sonatype.nexus.integrationtests.nexus421;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.SystemNotificationSettings;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus421PlainNotificationIT
    extends AbstractEmailServerNexusIT
{
    protected RepositoryMessageUtil repoMessageUtil;

    @Test
    public void testAutoBlockNotification()
        throws Exception
    {
        prepare();

        // make central auto-block itself (point it to bad URL)
        pointCentralToRemoteUrl( "http://repo1.maven.org/mavenFooBar/not-here/" );

        // we have 3 recipients set
        checkMails( 3, 0 );

        // make central unblock itself (point it to good URL)
        pointCentralToRemoteUrl( "http://repo1.maven.org/maven2/" );

        // we have 3 recipients set
        checkMails( 0, 3 );
    }

    // --

    protected void prepare()
        throws Exception
    {
        // set up repo message util
        this.repoMessageUtil =
            new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );

        // CONFIG CHANGES (using Nexus factory-defaults!)
        // set up SMTP to use our mailServer
        // set admin role as role to be notified
        // set pipi1@wherever.com and pipi2@wherever.com as external mails to be notified
        // set notification enabled
        // save
        // enable auto-block on central

        GlobalConfigurationResource globalSettings = SettingsMessageUtil.getCurrentSettings();

        // correct SMTP hostname
        globalSettings.getSmtpSettings().setHost( "localhost" );
        globalSettings.getSmtpSettings().setPort( Integer.valueOf( TestProperties.getString( "email.server.port" ) ) );

        SystemNotificationSettings notificationSettings = globalSettings.getSystemNotificationSettings();

        // Damian returns null here (already fixed in trunk, remove this!)
        if ( notificationSettings == null )
        {
            notificationSettings = new SystemNotificationSettings();

            globalSettings.setSystemNotificationSettings( notificationSettings );
        }

        // set email addresses
        notificationSettings.setEmailAddresses( "pipi1@wherever.com,pipi2@wherever.com" );

        // this is ROLE!
        notificationSettings.getRoles().add( "admin" );

        // enable notification
        notificationSettings.setEnabled( true );

        Assert.assertEquals( "On saving global config, response should be success.", true, SettingsMessageUtil.save(
            globalSettings ).isSuccess() );

        // make a proxy server to block (do it by taking central, and breaking it's remoteURL)
        RepositoryProxyResource central = (RepositoryProxyResource) repoMessageUtil.getRepository( "central" );

        // make auto block active
        central.setAutoBlockActive( true );

        repoMessageUtil.updateRepo( central );
    }

    protected void pointCentralToRemoteUrl( String remoteUrl )
        throws IOException, InterruptedException
    {
        // make a proxy server to block (do it by taking central, and breaking it's remoteURL)
        RepositoryProxyResource central = (RepositoryProxyResource) repoMessageUtil.getRepository( "central" );

        // direct the repo to nonexistent maven2 repo
        central.getRemoteStorage().setRemoteStorageUrl( remoteUrl );

        repoMessageUtil.updateRepo( central );

        // to "ping it" (and wait for all the thread to check remote availability)
        RepositoryStatusResource res = repoMessageUtil.getStatus( "central", true );

        while ( RemoteStatus.UNKNOWN.name().equals( res.getRemoteStatus() ) )
        {
            res = repoMessageUtil.getStatus( "central", false );

            Thread.sleep( 10000 );
        }
    }

    protected void checkMails( int expectedBlockedMails, int expectedUnblockedMails )
        throws InterruptedException, MessagingException
    {
        // expect total 2*count mails: once for auto-block, once for unblock, for admin user, for pipi1 and for pipi2.
        // Mail
        // should be about "unblocked"
        // wait for long, since we really _dont_ know when the mail gonna be sent:
        // See "fibonacci" calculation above!
        server.waitForIncomingEmail( 440000, expectedBlockedMails + expectedUnblockedMails );

        MimeMessage[] msgs = server.getReceivedMessages();

        Assert.assertNotNull( "Messages array should not be null!", msgs );

        Assert.assertEquals( "We expect " + ( expectedBlockedMails + expectedUnblockedMails ) + " mails!",
            ( expectedBlockedMails + expectedUnblockedMails ), msgs.length );

        int blockedMails = 0;

        int unblockedMails = 0;

        for ( int i = 0; i < msgs.length; i++ )
        {
            MimeMessage msg = msgs[i];

            if ( msg.getSubject().toLowerCase().contains( "auto-blocked" ) )
            {
                blockedMails++;
            }
            else if ( msg.getSubject().toLowerCase().contains( "unblocked" ) )
            {
                unblockedMails++;
            }
        }

        Assert.assertEquals( "We should have " + expectedBlockedMails + " auto-blocked mails!", expectedBlockedMails,
            blockedMails );

        Assert.assertEquals( "We should have " + expectedUnblockedMails + " auto-UNblocked mails!",
            expectedUnblockedMails, unblockedMails );
    }
}
