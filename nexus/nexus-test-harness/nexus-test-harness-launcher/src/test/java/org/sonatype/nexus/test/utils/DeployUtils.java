package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.artifact.Gav;

public class DeployUtils
{

    public static void deployWithWagon( PlexusContainer container, String wagonHint, String repositoryUrl,
                                        File fileToDeploy, String artifactPath )
        throws ComponentLookupException, ConnectionException, AuthenticationException, TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException
    {

        Wagon wagon = (Wagon) container.lookup( Wagon.ROLE, wagonHint );

        Repository repository = new Repository();
        repository.setUrl( repositoryUrl );

        wagon.connect( repository );
        wagon.put( fileToDeploy, artifactPath );

    }

    public static int deployUsingGavWithRest( String restServiceURL, String repositoryId, Gav gav, File fileToDeploy )
        throws HttpException, IOException
    {

        // the method we are calling
        PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        try
        {
            Part[] parts =
                { new StringPart( "r", repositoryId ), new StringPart( "g", gav.getGroupId() ),
                    new StringPart( "a", gav.getArtifactId() ), new StringPart( "v", gav.getVersion() ),
                    new StringPart( "p", gav.getExtension() ), new StringPart( "c", "" ),
                    new FilePart( fileToDeploy.getName(), fileToDeploy ), };

            filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout( 5000 );
            return client.executeMethod( filePost );
        }
        finally
        {
            filePost.releaseConnection();
        }
    }

    public static int deployUsingPomWithRest( String restServiceURL, String repositoryId, Gav gav, File fileToDeploy,
                                             File pomFile )
        throws HttpException, IOException
    {
        // the method we are calling
        PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        try
        {
            Part[] parts =
                { new StringPart( "r", repositoryId ), new StringPart( "hasPom", "true" ),
                    new FilePart( pomFile.getName(), pomFile ),
                    new FilePart( fileToDeploy.getName(), fileToDeploy ), };

            filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout( 5000 );
            return client.executeMethod( filePost );
        }
        finally
        {
            filePost.releaseConnection();
        }
    }

}
