package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CGlobalRestApiCoreConfiguration
    extends AbstractCoreConfiguration
{
    private boolean nullified;

    public CGlobalRestApiCoreConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        super( applicationConfiguration );
    }

    @Override
    protected Object extractConfiguration( Configuration configuration )
    {
        return configuration.getRestApi();
    }

    @Override
    public CRestApiSettings getConfiguration( boolean forWrite )
    {
        return (CRestApiSettings) super.getConfiguration( forWrite );
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }

    public void nullifyConfig()
    {
        setChangedConfiguration( null );

        setOriginalConfiguration( null );

        nullified = true;
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
            getApplicationConfiguration().getConfigurationModel().setRestApi( null );
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

    public void initConfig()
    {
        CRestApiSettings restApiSettings = new CRestApiSettings();

        getApplicationConfiguration().getConfigurationModel().setRestApi( restApiSettings );

        setOriginalConfiguration( restApiSettings );
    }
}
