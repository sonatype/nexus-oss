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
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;

public class DeployUtils
    extends ITUtil
{
    private static final Logger LOG = Logger.getLogger( DeployUtils.class );

    public DeployUtils( AbstractNexusIntegrationTest test )
    {
        super( test );
    }

    public void deployWithWagon( String wagonHint, String repositoryUrl, File fileToDeploy, String artifactPath )
        throws Exception
    {
        TestContext testContext = TestContainer.getInstance().getTestContext();
        if ( testContext.isSecureTest() )
        {
            new WagonDeployer( getTest().getITPlexusContainer(), wagonHint, testContext.getUsername(),
                testContext.getPassword(), repositoryUrl, fileToDeploy, artifactPath ).deploy();
        }
        else
        {
            new WagonDeployer( getTest().getITPlexusContainer(), wagonHint, null, null, repositoryUrl, fileToDeploy,
                artifactPath ).deploy();
        }
        //
        // new WagonDeployer( wagonHint, TestContainer.getInstance().getTestContext().getUsername(),
        // TestContainer.getInstance().getTestContext().getPassword(), repositoryUrl, fileToDeploy,
        // artifactPath ).deploy();
    }

    public int deployUsingGavWithRest( String repositoryId, Gav gav, File fileToDeploy )
        throws HttpException, IOException
    {
        return deployUsingGavWithRest( AbstractNexusIntegrationTest.nexusBaseUrl
            + "service/local/artifact/maven/content", repositoryId, gav, fileToDeploy );
    }

    public int deployUsingGavWithRest( String restServiceURL, String repositoryId, Gav gav, File fileToDeploy )
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

    public int deployUsingPomWithRest( String repositoryId, File fileToDeploy, File pomFile, String classifier,
                                       String extention )
        throws HttpException, IOException
    {
        return deployUsingPomWithRest( AbstractNexusIntegrationTest.nexusBaseUrl
            + "service/local/artifact/maven/content", repositoryId, fileToDeploy, pomFile, classifier, extention );
    }

    public HttpMethod deployUsingPomWithRestReturnResult( String repositoryId, File fileToDeploy, File pomFile,
                                                          String classifier, String extention )
        throws HttpException, IOException
    {
        return deployUsingPomWithRestReturnResult( AbstractNexusIntegrationTest.nexusBaseUrl
            + "service/local/artifact/maven/content", repositoryId, fileToDeploy, pomFile, classifier, extention );
    }

    public HttpMethod deployUsingPomWithRestReturnResult( String restServiceURL, String repositoryId,
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

    public int deployUsingPomWithRest( String restServiceURL, String repositoryId, File fileToDeploy, File pomFile,
                                       String classifier, String extention )
        throws HttpException, IOException
    {
        return deployUsingPomWithRestReturnResult( restServiceURL, repositoryId, fileToDeploy, pomFile, classifier,
            extention ).getStatusCode();
    }

    public HttpMethod deployPomWithRest( String repositoryId, File pomFile )
        throws HttpException, IOException
    {
        String restServiceURL = AbstractNexusIntegrationTest.nexusBaseUrl + "service/local/artifact/maven/content";
        // the method we are calling
        PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        Part[] parts =
            { new StringPart( "r", repositoryId ), new StringPart( "hasPom", "true" ),
                new FilePart( pomFile.getName(), pomFile ), };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        LOG.debug( "URL:  " + restServiceURL );
        LOG.debug( "Method: Post" );
        LOG.debug( "params: " );
        LOG.debug( "\tr: " + repositoryId );
        LOG.debug( "\thasPom: true" );
        LOG.debug( "\tpom: " + pomFile );

        return RequestFacade.executeHTTPClientMethod( new URL( restServiceURL ), filePost );
    }
}
