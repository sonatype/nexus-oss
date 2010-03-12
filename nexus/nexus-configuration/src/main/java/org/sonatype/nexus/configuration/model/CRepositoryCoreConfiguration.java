package org.sonatype.nexus.configuration.model;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.LocalStatus;

public class CRepositoryCoreConfiguration
    extends AbstractCoreConfiguration
{
    private static final String REPOSITORY_ID_PATTERN = "^[a-zA-Z0-9_\\-\\.]+$";

    private final CRepository repositoryModel;

    private final CRepositoryExternalConfigurationHolderFactory<?> externalConfigurationFactory;

    public CRepositoryCoreConfiguration( ApplicationConfiguration configuration, CRepository repositoryModel,
                                         CRepositoryExternalConfigurationHolderFactory<?> extFactory )
    {
        super( configuration );

        setOriginalConfiguration( repositoryModel );

        this.repositoryModel = repositoryModel;

        this.externalConfigurationFactory = extFactory;
    }

    @Override
    public CRepository getConfiguration( boolean forWrite )
    {
        return (CRepository) super.getConfiguration( forWrite );
    }

    @Override
    protected void copyTransients( Object source, Object destination )
    {
        ( (CRepository) destination ).setExternalConfiguration( ( (CRepository) source ).getExternalConfiguration() );

        ( (CRepository) destination ).externalConfigurationImple = ( (CRepository) source ).externalConfigurationImple;

        ( (CRepository) destination ).defaultLocalStorageUrl = ( (CRepository) source ).defaultLocalStorageUrl;

        // trick with RemoteStorage, which is an object, and XStream will not "overlap" it properly (ie. destionation !=
        // null but source == null)
        if ( ( (CRepository) source ).getRemoteStorage() == null )
        {
            ( (CRepository) destination ).setRemoteStorage( null );
        }
    }

    @Override
    protected ExternalConfiguration<?> prepareExternalConfiguration( Object configuration )
    {
        if ( externalConfigurationFactory == null )
        {
            return null;
        }

        // prepare the Xpp3Dom root node
        if ( repositoryModel.getExternalConfiguration() == null )
        {
            // just put an elephant in South Africa to find it for sure ;)
            repositoryModel
                           .setExternalConfiguration( new Xpp3Dom( DefaultCRepository.EXTERNAL_CONFIGURATION_NODE_NAME ) );
        }

        // set the holder
        if ( repositoryModel.externalConfigurationImple == null )
        {
            // in 1st round, i intentionally choosed to make our lives bitter, and handle config manually
            // later we will see about it
            repositoryModel.externalConfigurationImple =
                externalConfigurationFactory.createExternalConfigurationHolder( repositoryModel );
        }

        return new DefaultExternalConfiguration<AbstractXpp3DomExternalConfigurationHolder>(
                                                                                             getApplicationConfiguration(),
                                                                                             this,
                                                                                             (AbstractXpp3DomExternalConfigurationHolder) repositoryModel.externalConfigurationImple );
    }

    @Override
    protected CRepository extractConfiguration( Configuration configuration )
    {
        // this is an exceptional situation, the "normal" way will not work, look at the constructor
        return null;
    }

    // ==

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        CRepository cfg = (CRepository) changedConfiguration;

        ValidationResponse response = new ApplicationValidationResponse();

        // ID
        if ( StringUtils.isBlank( cfg.getId() ) )
        {
            response.addValidationError( new ValidationMessage( "id", "Repository ID must not be blank!" ) );
        }
        else if ( !cfg.getId().matches( REPOSITORY_ID_PATTERN ) )
        {
            response
                    .addValidationError( new ValidationMessage( "id",
                                                                "Only letters, digits, underscores, hyphens, and dots are allowed in Repository ID" ) );
        }
        // ID uniqueness
        List<CRepository> repositories = getApplicationConfiguration().getConfigurationModel().getRepositories();

        for ( CRepository other : repositories )
        {
            // skip ourselves
            if ( other != getOriginalConfiguration() )
            {
                if ( StringUtils.equals( cfg.getId(), other.getId() ) )
                {
                    response.addValidationError( new ValidationMessage( "id", "Repository with ID=\"" + cfg.getId()
                        + "\" already exists (name of the existing repository: \"" + other.getName() + "\")" ) );
                }
            }
        }

        // Name
        if ( StringUtils.isBlank( cfg.getName() ) )
        {
            response.addValidationWarning( new ValidationMessage( "id", "Repository with ID='" + cfg.getId()
                + "' has no name, defaulted it's name to it's ID." ) );

            cfg.setName( cfg.getId() );

            response.setModified( true );
        }

        // LocalStatus
        try
        {
            LocalStatus.valueOf( cfg.getLocalStatus() );
        }
        catch ( Exception e )
        {
            response.addValidationError( new ValidationMessage( "localStatus", "LocalStatus of repository with ID=\""
                + cfg.getId() + "\" has unacceptable value \"" + cfg.getLocalStatus() + "\"! (Allowed values are: \""
                + LocalStatus.IN_SERVICE + "\" and \"" + LocalStatus.OUT_OF_SERVICE + "\")", e ) );
        }

        // indexable
        if ( cfg.isIndexable() && ( !"maven2".equals( cfg.getProviderHint() ) ) )
        {
            response.addValidationWarning( new ValidationMessage( "indexable", "Indexing isn't supported for \""
                + cfg.getProviderHint() + "\" repositories, only Maven2 repositories are indexable!" ) );

            cfg.setIndexable( false );

            response.setModified( true );
        }

        // proxy repo URL (if set)
        if ( cfg.getRemoteStorage() != null && cfg.getRemoteStorage().getUrl() != null
            && !cfg.getRemoteStorage().getUrl().endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            cfg.getRemoteStorage().setUrl( cfg.getRemoteStorage().getUrl() + RepositoryItemUid.PATH_SEPARATOR );
        }

        return response;
    }
}
