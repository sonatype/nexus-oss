package org.sonatype.nexus.integrationtests.nexus421;

import java.io.IOException;

import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.SystemNotificationSettings;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;

public class Nexus421PlainNotificationTest
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

        // expect 3 mails: for admin user, for pipi1 and for pipi2. Mail should be about "auto-blocked"
        expectMails( 3, "auto-blocked" );

        // make central unblock itself (point it to good URL)
        pointCentralToRemoteUrl( "http://repo1.maven.org/maven2/" );

        // expect 3 mails: for admin user, for pipi1 and for pipi2. Mail should be about "unblocked"
        expectMails( 3, "unblocked" );
    }

    // --

    protected void prepare()
        throws Exception
    {
        // set up repo message util
        this.repoMessageUtil =
            new RepositoryMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON, getRepositoryTypeRegistry() );

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
        throws IOException
    {
        // make a proxy server to block (do it by taking central, and breaking it's remoteURL)
        RepositoryProxyResource central = (RepositoryProxyResource) repoMessageUtil.getRepository( "central" );

        // direct the repo to nonexistent maven2 repo
        central.getRemoteStorage().setRemoteStorageUrl( remoteUrl );

        repoMessageUtil.updateRepo( central );
    }

    protected void expectMails( int count, String... contentToCheckFor )
        throws InterruptedException
    {
        server.waitForIncomingEmail( 10000, count );

        MimeMessage[] msgs = server.getReceivedMessages();

        Assert.assertNotNull( "Messages array should not be null!", msgs );

        Assert.assertEquals( "We expect " + count + " mails!", count, msgs.length );

        // TODO: implement simple string search for contentToCheckFor in mail bodies
    }

}
