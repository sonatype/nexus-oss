/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;

public class DeployUtils
{

    private static final Logger LOG = Logger.getLogger( DeployUtils.class );

    public static void deployWithWagon( PlexusContainer container, String wagonHint, String repositoryUrl,
                                        File fileToDeploy, String artifactPath )
        throws ComponentLookupException, ConnectionException, AuthenticationException, TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException, InterruptedException, CommandLineException
    {
        TestContext testContext = TestContainer.getInstance().getTestContext();
        if ( testContext.isSecureTest() )
        {
            new WagonDeployer( wagonHint, testContext.getUsername(), testContext.getPassword(), repositoryUrl,
                               fileToDeploy, artifactPath ).deploy();
        }
        else
        {
            new WagonDeployer( wagonHint, null, null, repositoryUrl, fileToDeploy, artifactPath ).deploy();
        }
        //
        // new WagonDeployer( wagonHint, TestContainer.getInstance().getTestContext().getUsername(),
        // TestContainer.getInstance().getTestContext().getPassword(), repositoryUrl, fileToDeploy,
        // artifactPath ).deploy();
    }

    public static void forkDeployWithWagon( PlexusContainer container, String wagonHint, String repositoryUrl,
                                            File fileToDeploy, String artifactPath )
        throws ComponentLookupException, ConnectionException, AuthenticationException, TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException, InterruptedException, CommandLineException
    {
        // must fork due to bug: http://forums.sun.com/thread.jspa?threadID=567697&messageID=2805259
        new WagonDeployer( wagonHint, TestContainer.getInstance().getTestContext().getUsername(),
                           TestContainer.getInstance().getTestContext().getPassword(), repositoryUrl, fileToDeploy,
                           artifactPath ).forkDeploy( container );

    }

    public static int deployUsingGavWithRest( String repositoryId, Gav gav, File fileToDeploy )
        throws HttpException, IOException
    {
        return deployUsingGavWithRest( TestProperties.getString( "nexus.base.url" )
            + "service/local/artifact/maven/content", repositoryId, gav, fileToDeploy );
    }

    public static int deployUsingGavWithRest( String restServiceURL, String repositoryId, Gav gav, File fileToDeploy )
        throws HttpException, IOException
    {

        // the method we are calling
        PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        Part[] parts =
            { new StringPart( "r", repositoryId ), new StringPart( "g", gav.getGroupId() ),
                new StringPart( "a", gav.getArtifactId() ), new StringPart( "v", gav.getVersion() ),
                new StringPart( "p", gav.getExtension() ), new StringPart( "c", "" ),
                new FilePart( fileToDeploy.getName(), fileToDeploy ), };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        return RequestFacade.executeHTTPClientMethod( new URL( restServiceURL ), filePost ).getStatusCode();

    }

    public static int deployUsingPomWithRest( String repositoryId, File fileToDeploy, File pomFile, String classifier,
                                              String extention )
        throws HttpException, IOException
    {
        return deployUsingPomWithRest( TestProperties.getString( "nexus.base.url" )
            + "service/local/artifact/maven/content", repositoryId, fileToDeploy, pomFile, classifier, extention );
    }

    public static HttpMethod deployUsingPomWithRestReturnResult( String repositoryId, File fileToDeploy, File pomFile,
                                                                 String classifier, String extention )
        throws HttpException, IOException
    {
        return deployUsingPomWithRestReturnResult( TestProperties.getString( "nexus.base.url" )
            + "service/local/artifact/maven/content", repositoryId, fileToDeploy, pomFile, classifier, extention );
    }

    public static HttpMethod deployUsingPomWithRestReturnResult( String restServiceURL, String repositoryId,
                                                                 File fileToDeploy, File pomFile, String classifier,
                                                                 String extention )
        throws HttpException, IOException
    {
        // the method we are calling
        PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        classifier = ( classifier == null ) ? "" : classifier;
        extention = ( extention == null ) ? "" : extention;

        Part[] parts =
            { new StringPart( "r", repositoryId ), new StringPart( "e", extention ), new StringPart( "c", classifier ),
                new StringPart( "hasPom", "true" ), new FilePart( pomFile.getName(), pomFile ),
                new FilePart( fileToDeploy.getName(), fileToDeploy ) };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        LOG.debug( "URL:  " + restServiceURL );
        LOG.debug( "Method: Post" );
        LOG.debug( "params: " );
        LOG.debug( "\tr: " + repositoryId );
        LOG.debug( "\thasPom: true" );
        LOG.debug( "\tpom: " + pomFile );
        LOG.debug( "\tfileToDeploy: " + fileToDeploy );

        return RequestFacade.executeHTTPClientMethod( new URL( restServiceURL ), filePost );
    }

    public static int deployUsingPomWithRest( String restServiceURL, String repositoryId, File fileToDeploy,
                                              File pomFile, String classifier, String extention )
        throws HttpException, IOException
    {
        return deployUsingPomWithRestReturnResult( restServiceURL, repositoryId, fileToDeploy, pomFile, classifier,
                                                   extention ).getStatusCode();
    }
}
