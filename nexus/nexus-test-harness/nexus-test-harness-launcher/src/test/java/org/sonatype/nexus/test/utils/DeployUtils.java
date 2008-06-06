package org.sonatype.nexus.test.utils;

import java.io.File;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class DeployUtils
{

    public static void deployWithWagon(PlexusContainer container, String wagonHint, String repositoryUrl, File fileToDeploy, String artifactPath) throws ComponentLookupException, ConnectionException, AuthenticationException, TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {

        Wagon wagon = (Wagon) container.lookup( Wagon.ROLE, wagonHint );

        Repository repository = new Repository();
        repository.setUrl( repositoryUrl );

        wagon.connect( repository );
        wagon.put( fileToDeploy, artifactPath );

    }

}
