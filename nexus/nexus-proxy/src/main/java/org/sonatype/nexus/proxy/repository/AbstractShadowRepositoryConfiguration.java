package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;

public abstract class AbstractShadowRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MASTER_REPOSITORY_ID = "masterRepositoryId";

    private static final String SYNCHRONIZE_AT_STARTUP = "synchronizeAtStartup";

    public AbstractShadowRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public String getMasterRepositoryId()
    {
        return getNodeValue( getRootNode(), MASTER_REPOSITORY_ID, null );
    }

    public void setMasterRepositoryId( String id )
    {
        setNodeValue( getRootNode(), MASTER_REPOSITORY_ID, id );
    }

    public boolean isSynchronizeAtStartup()
    {
        return Boolean.parseBoolean( getNodeValue( getRootNode(), SYNCHRONIZE_AT_STARTUP, Boolean.FALSE.toString() ) );
    }

    public void setSynchronizeAtStartup( boolean val )
    {
        setNodeValue( getRootNode(), SYNCHRONIZE_AT_STARTUP, Boolean.toString( val ) );
    }

    @Override
    public ValidationResponse doValidateChanges( ApplicationConfiguration applicationConfiguration,
                                                 CoreConfiguration owner, Xpp3Dom config )
    {
        ValidationResponse response = super.doValidateChanges( applicationConfiguration, owner, config );

        // validate master

        List<CRepository> allReposes = applicationConfiguration.getConfigurationModel().getRepositories();

        boolean masterFound = false;

        for ( CRepository repository : allReposes )
        {
            masterFound = masterFound || StringUtils.equals( repository.getId(), getMasterRepositoryId() );
        }

        if ( !masterFound )
        {
            String id = ( (CRepositoryCoreConfiguration) owner ).getConfiguration( false ).getId();
            ValidationMessage message =
                new ValidationMessage( "shadowOf", "Master repository id=\"" + getMasterRepositoryId()
                    + "\" not found for ShadowRepository with id=\"" + id + "\"!",
                                       "The source nexus repository is not existing." );

            response.addValidationError( message );
        }
        
        return response;
    }
}
