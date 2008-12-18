package org.sonatype.nexus.proxy.maven.maven2;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.GroupRepositoryConfigurator;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

@Component( role = GroupRepositoryConfigurator.class, hint = "maven2" )
public class M2GroupRepositoryConfigurator
    implements GroupRepositoryConfigurator
{

    @SuppressWarnings("unchecked")
    public GroupRepository updateRepositoryFromModel( GroupRepository old, ApplicationConfiguration configuration,
        CRepositoryGroup group, LocalRepositoryStorage ls )
        throws InvalidConfigurationException
    {
        AbstractGroupRepository repository = (AbstractGroupRepository) old;

        repository.setId( group.getGroupId() );
        repository.setName( group.getName() );

        File defaultStorageFile = new File( new File( configuration.getWorkingDirectory(), "storage" ), repository
            .getId() );

        String localUrl = null;

        try
        {
            localUrl = defaultStorageFile.toURL().toString();
        }
        catch ( MalformedURLException e )
        {
            // will not happen, not user settable
            throw new InvalidConfigurationException( "Malformed URL for LocalRepositoryStorage!", e );
        }

        if ( group.getLocalStorage() != null )
        {
            localUrl = group.getLocalStorage().getUrl();
        }
        else
        {
            // Default dir is going to be valid
            defaultStorageFile.mkdirs();
        }

        try
        {
            ls.validateStorageUrl( localUrl );

            repository.setLocalUrl( localUrl );
            repository.setLocalStorage( ls );
        }
        catch ( StorageException e )
        {
            ValidationResponse response = new ApplicationValidationResponse();

            ValidationMessage error = new ValidationMessage(
                "overrideLocalStorageUrl",
                "Repository has an invalid local storage URL '" + localUrl,
                "Invalid file location" );

            response.addValidationError( error );

            throw new InvalidConfigurationException( response );
        }
        
        repository.setMemberRepositories( new ArrayList<String>( group.getRepositories() ) );

        return repository;
    }

}
