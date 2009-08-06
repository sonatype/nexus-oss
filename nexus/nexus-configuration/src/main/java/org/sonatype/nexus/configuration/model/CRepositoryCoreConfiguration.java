package org.sonatype.nexus.configuration.model;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CRepositoryCoreConfiguration
    extends AbstractCoreConfiguration
{
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
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub

    }
}
