package org.sonatype.nexus.proxy.repository;

import java.io.File;

public class ClientSSLRemoteAuthenticationSettings
    implements RemoteAuthenticationSettings
{
    private final File trustStore;

    private final String trustStorePassword;

    private final File keyStore;

    private final String keyStorePassword;

    public ClientSSLRemoteAuthenticationSettings( File trustStore, String trustStorePassword, File keyStore,
                                                  String keyStorePassword )
    {
        this.trustStore = trustStore;

        this.trustStorePassword = trustStorePassword;

        this.keyStore = keyStore;

        this.keyStorePassword = keyStorePassword;
    }

    public File getTrustStore()
    {
        return trustStore;
    }

    public String getTrustStorePassword()
    {
        return trustStorePassword;
    }

    public File getKeyStore()
    {
        return keyStore;
    }

    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }
}
