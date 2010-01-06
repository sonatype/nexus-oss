package org.sonatype.nexus.plugins.maven.wagon;

import java.io.IOException;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

/**
 * Manages Nexus URLs. In form of "nexus://repoId"
 * 
 * @author cstamas
 */
@Component( role = Wagon.class, hint = "nexus", instantiationStrategy = "per-lookup" )
public class NexusWagon
    extends StreamWagon
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    private MavenRepository nexusRepository;

    public RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    public MavenRepository getNexusRepository()
    {
        return nexusRepository;
    }

    @Override
    public void connect( org.apache.maven.wagon.repository.Repository repository,
                         AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider )
        throws ConnectionException, AuthenticationException
    {
        // auth and proxy infos are completely neglected
        super.connect( repository, authenticationInfo, proxyInfoProvider );

        try
        {
            nexusRepository =
                getRepositoryRegistry().getRepositoryWithFacet( repository.getHost(), MavenRepository.class );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ConnectionException( "Nexus does not have repository with ID=\"" + getRepository().getUrl()
                + "\"!", e );
        }
    }

    @Override
    public void openConnectionInternal()
        throws ConnectionException, AuthenticationException
    {
        // nothing
    }

    @Override
    public void closeConnection()
        throws ConnectionException
    {
        // nothing
    }

    @Override
    public void fillInputData( InputData inputData )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        try
        {
            ResourceStoreRequest request = new ResourceStoreRequest( inputData.getResource().getName() );

            StorageItem item = getNexusRepository().retrieveItem( request );

            if ( item instanceof StorageFileItem )
            {
                StorageFileItem fitem = (StorageFileItem) item;

                inputData.setInputStream( fitem.getInputStream() );

                inputData.getResource().setLastModified( fitem.getModified() );

                inputData.getResource().setContentLength( fitem.getLength() );
            }
            else
            {
                throw new ResourceDoesNotExistException( "Item " + inputData.getResource().getName()
                    + " does not exist in Nexus repository ID=\"" + getNexusRepository().getId() + "\"." );
            }
        }
        catch ( ItemNotFoundException e )
        {
            throw new ResourceDoesNotExistException( "Item " + inputData.getResource().getName()
                + " does not exist in Nexus repository ID=\"" + getNexusRepository().getId() + "\"." );
        }
        catch ( IOException e )
        {
            throw new TransferFailedException( "IO problem during retrieve from repository with ID=\""
                + getRepository().getUrl() + "\"!", e );
        }
        catch ( Exception e )
        {
            throw new TransferFailedException( "Problem during retrieve from repository with ID=\""
                + getRepository().getUrl() + "\"!", e );
        }
    }

    @Override
    public void fillOutputData( OutputData outputData )
        throws TransferFailedException
    {
        // nothing yet
    }
}
