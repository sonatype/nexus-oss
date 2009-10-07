package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Checkbox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Fieldset;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.TwinPanel;

import com.thoughtworks.selenium.Selenium;

public class ServerTab
    extends Component
{

    private TextField smtpHost;

    private TextField smtpPort;

    private TextField smtpUser;

    private TextField smtpPassword;

    private Checkbox smtpSsl;

    private Checkbox smtpTls;

    private TextField smtpEmail;

    private TextField globalAgent;

    private TextField globalParameters;

    private TextField globalTimeout;

    private TextField globalRetry;

    private TextField securityEnable;

    private Checkbox securityAnonymousAccess;

    private TextField securityAnonymousUsername;

    private TextField securityAnonymousPassword;

    private TextField applicationBaseUrl;

    private Checkbox applicationForceBaseUrl;

    private TextField proxyHost;

    private TextField proxyPort;

    private TextField proxyUsername;

    private TextField proxyPassword;

    private TextField proxyPrivatekey;

    private TextField proxyKeyPhrase;

    private TextField proxyNtlmHost;

    private TextField proxyNtlmDomain;

    private Button saveButton;

    private Button cancelButton;

    private Fieldset serverSettings;

    private Fieldset proxySettings;

    private Fieldset proxyAuthentication;

    private TwinPanel securityRealms;

    public ServerTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('nexus-config')" );

        smtpHost = new TextField( selenium, expression + ".find('name', 'smtpSettings.host')[0]" );
        smtpPort = new TextField( selenium, expression + ".find('name', 'smtpSettings.port')[0]" );
        smtpUser = new TextField( selenium, expression + ".find('name', 'smtpSettings.username')[0]" );
        smtpPassword = new TextField( selenium, expression + ".find('name', 'smtpSettings.password')[0]" );
        smtpSsl = new Checkbox( selenium, expression + ".find('name', 'smtpSettings.sslEnabled')[0]" );
        smtpTls = new Checkbox( selenium, expression + ".find('name', 'smtpSettings.tlsEnabled')[0]" );
        smtpEmail = new TextField( selenium, expression + ".find('name', 'smtpSettings.systemEmailAddress')[0]" );

        globalAgent =
            new TextField( selenium, expression + ".find('name', 'globalConnectionSettings.userAgentString')[0]" );
        globalParameters =
            new TextField( selenium, expression + ".find('name', 'globalConnectionSettings.queryString')[0]" );
        globalTimeout =
            new TextField( selenium, expression + ".find('name', 'globalConnectionSettings.connectionTimeout')[0]" );
        globalRetry =
            new TextField( selenium, expression + ".find('name', 'globalConnectionSettings.retrievalRetryCount')[0]" );

        securityEnable = new TextField( selenium, expression + ".find('name', 'securityEnabled')[0]" );
        securityRealms = new TwinPanel( selenium, expression + ".find('name', 'securityRealms')[0]" );

        securityAnonymousAccess =
            new Checkbox( selenium, expression + ".find('name', 'anonymousAccessFields')[0].checkbox" );
        securityAnonymousUsername = new TextField( selenium, expression + ".find('name', 'securityAnonymousUsername')[0]" );
        securityAnonymousPassword = new TextField( selenium, expression + ".find('name', 'securityAnonymousPassword')[0]" );

        applicationBaseUrl = new TextField( selenium, expression + ".find('name', 'baseUrl')[0]" );
        applicationForceBaseUrl = new Checkbox( selenium, expression + ".find('name', 'forceBaseUrl')[0]" );

        proxyHost = new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.proxyHostname')[0]" );
        proxyPort = new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.proxyPort')[0]" );
        proxyUsername =
            new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.authentication.username')[0]" );
        proxyPassword =
            new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.authentication.password')[0]" );
        proxyPrivatekey =
            new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.authentication.privateKey')[0]" );
        proxyKeyPhrase =
            new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.authentication.passphrase')[0]" );
        proxyNtlmHost =
            new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.authentication.ntlmHost')[0]" );
        proxyNtlmDomain =
            new TextField( selenium, expression + ".find('name', 'globalHttpProxySettings.authentication.ntlmDomain')[0]" );

        saveButton = new Button( selenium, "window.Ext.getCmp('savebutton')" );
        cancelButton = new Button( selenium, "window.Ext.getCmp('cancelbutton')" );

        serverSettings = new Fieldset( selenium, expression + ".find('name', 'applicationServerSettings')[0]" );
        proxySettings = new Fieldset( selenium, expression + ".find('name', 'globalHttpProxySettings')[0]" );
        proxyAuthentication = new Fieldset( selenium, expression + ".find('name', 'globalHttpProxySettings.authentication')[0]" );
    }

    public ServerTab save()
    {
        saveButton.click();

        return this;
    }

    public ServerTab toggleServerSetting( boolean enable )
    {
        serverSettings.check( enable );

        return this;
    }

    public ServerTab toggleProxy( boolean enable )
    {
        proxySettings.check( enable );

        return this;
    }

    public ServerTab toggleProxyAuthentication( boolean enable )
    {
        proxyAuthentication.check( enable );

        return this;
    }

    public TextField getSmtpHost()
    {
        return smtpHost;
    }

    public TextField getSmtpPort()
    {
        return smtpPort;
    }

    public TextField getSmtpUser()
    {
        return smtpUser;
    }

    public TextField getSmtpPassword()
    {
        return smtpPassword;
    }

    public Checkbox getSmtpSsl()
    {
        return smtpSsl;
    }

    public Checkbox getSmtpTls()
    {
        return smtpTls;
    }

    public TextField getSmtpEmail()
    {
        return smtpEmail;
    }

    public TextField getGlobalAgent()
    {
        return globalAgent;
    }

    public TextField getGlobalParameters()
    {
        return globalParameters;
    }

    public TextField getGlobalTimeout()
    {
        return globalTimeout;
    }

    public TextField getGlobalRetry()
    {
        return globalRetry;
    }

    public TextField getSecurityEnable()
    {
        return securityEnable;
    }

    public Checkbox getSecurityAnonymousAccess()
    {
        return securityAnonymousAccess;
    }

    public TextField getSecurityAnonymousUsername()
    {
        return securityAnonymousUsername;
    }

    public TextField getSecurityAnonymousPassword()
    {
        return securityAnonymousPassword;
    }

    public TextField getApplicationBaseUrl()
    {
        return applicationBaseUrl;
    }

    public Checkbox getApplicationForceBaseUrl()
    {
        return applicationForceBaseUrl;
    }

    public TextField getProxyHost()
    {
        return proxyHost;
    }

    public TextField getProxyPort()
    {
        return proxyPort;
    }

    public TextField getProxyUsername()
    {
        return proxyUsername;
    }

    public TextField getProxyPassword()
    {
        return proxyPassword;
    }

    public TextField getProxyPrivatekey()
    {
        return proxyPrivatekey;
    }

    public TextField getProxyKeyPhrase()
    {
        return proxyKeyPhrase;
    }

    public TextField getProxyNtlmHost()
    {
        return proxyNtlmHost;
    }

    public TextField getProxyNtlmDomain()
    {
        return proxyNtlmDomain;
    }

    public Button getSaveButton()
    {
        return saveButton;
    }

    public Button getCancelButton()
    {
        return cancelButton;
    }

    public Fieldset getServerSettings()
    {
        return serverSettings;
    }

    public Fieldset getProxySettings()
    {
        return proxySettings;
    }

    public Fieldset getProxyAuthentication()
    {
        return proxyAuthentication;
    }

    public TwinPanel getSecurityRealms()
    {
        return securityRealms;
    }
}
