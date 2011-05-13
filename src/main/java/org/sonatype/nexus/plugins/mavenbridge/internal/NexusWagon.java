package org.sonatype.nexus.plugins.mavenbridge.internal;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

@Named( "nexus" )
public class NexusWagon
    extends StreamWagon
{

    private final RepositoryRegistry repositoryRegistry;

    private MavenRepository nexusRepository;

    @Inject
    NexusWagon( final RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = repositoryRegistry;
    }

    @Override
    public void connect( final org.apache.maven.wagon.repository.Repository repository,
                         final AuthenticationInfo authenticationInfo, final ProxyInfoProvider proxyInfoProvider )
        throws ConnectionException, AuthenticationException
    {
        // auth and proxy infos are completely neglected
        super.connect( repository, authenticationInfo, proxyInfoProvider );

        try
        {
            nexusRepository = repositoryRegistry.getRepositoryWithFacet( repository.getHost(), MavenRepository.class );
        }
        catch ( final NoSuchRepositoryException e )
        {
            throw new ConnectionException( String.format( "Nexus does not have a repository with id [%s]",
                repository.getHost() ), e );
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
    public void fillInputData( final InputData inputData )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        try
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( inputData.getResource().getName() );

            final StorageItem item = nexusRepository.retrieveItem( request );

            if ( item instanceof StorageFileItem )
            {
                final StorageFileItem fitem = (StorageFileItem) item;

                inputData.setInputStream( fitem.getInputStream() );

                inputData.getResource().setLastModified( fitem.getModified() );

                inputData.getResource().setContentLength( fitem.getLength() );
            }
            else
            {
                throw new ResourceDoesNotExistException( String.format(
                    "Item [%s] does not exist in Nexus repository [%s]", inputData.getResource().getName(),
                    nexusRepository.getId() ) );
            }
        }
        catch ( final ItemNotFoundException e )
        {
            throw new ResourceDoesNotExistException( String.format(
                "Item [%s] does not exist in Nexus repository [%s]", inputData.getResource().getName(),
                nexusRepository.getId() ) );
        }
        catch ( final IOException e )
        {
            throw new TransferFailedException( String.format( "IO problem during retrieve from repository [%s]",
                nexusRepository.getId() ), e );
        }
        catch ( final Exception e )
        {
            throw new TransferFailedException( String.format( "Problem during retrieve from repository [%s]",
                nexusRepository.getId() ), e );
        }
    }

    @Override
    public void fillOutputData( final OutputData outputData )
        throws TransferFailedException
    {
        // nothing yet
    }
}
