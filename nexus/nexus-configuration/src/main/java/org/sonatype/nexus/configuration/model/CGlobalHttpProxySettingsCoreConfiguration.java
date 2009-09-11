package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CGlobalHttpProxySettingsCoreConfiguration
    extends AbstractCoreConfiguration
{
    private boolean nullified;

    public CGlobalHttpProxySettingsCoreConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        super( applicationConfiguration );
    }

    @Override
    public CRemoteHttpProxySettings getConfiguration( boolean forWrite )
    {
        return (CRemoteHttpProxySettings) super.getConfiguration( forWrite );
    }

    @Override
    protected CRemoteHttpProxySettings extractConfiguration( Configuration configuration )
    {
        return configuration.getGlobalHttpProxySettings();
    }

    public void initConfig()
    {
        CRemoteHttpProxySettings newProxy = new CRemoteHttpProxySettings();

        getApplicationConfiguration().getConfigurationModel().setGlobalHttpProxySettings( newProxy );

        setOriginalConfiguration( newProxy );
    }

    public void nullifyConfig()
    {
        setChangedConfiguration( null );

        nullified = true;
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }

    @Override
    public boolean isDirty()
    {
        return super.isDirty() || nullified;
    }

    @Override
    public void commitChanges()
        throws ConfigurationException
    {
        if ( nullified )
        {
            // nullified, nothing to validate and the super.commitChanges() will not work
            getApplicationConfiguration().getConfigurationModel().setGlobalHttpProxySettings( null );
        }
        else
        {
            super.commitChanges();
        }

        nullified = false;
    }

    @Override
    public void rollbackChanges()
    {
        super.rollbackChanges();

        nullified = false;
    }
}
