/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.wagon.Wagon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;

public class DeployUtils
{

    private static final Logger LOG = LoggerFactory.getLogger( DeployUtils.class );

    private final NexusRestClient nexusRestClient;

    private final WagonDeployer.Factory wagonFactory;

    public DeployUtils( final NexusRestClient nexusRestClient )
    {
        this.nexusRestClient = checkNotNull( nexusRestClient );
        this.wagonFactory = null;
    }

    public DeployUtils( final NexusRestClient nexusRestClient,
                        final WagonDeployer.Factory wagonFactory )
    {
        this.nexusRestClient = checkNotNull( nexusRestClient );
        this.wagonFactory = checkNotNull( wagonFactory );
    }

    public void deployWithWagon( String wagonHint, String repositoryUrl, File fileToDeploy, String artifactPath )
        throws Exception
    {
        checkState( wagonFactory != null, "Wagon factory must be provided to be able to deploy" );

        final TestContext testContext = nexusRestClient.getTestContext();
        final Wagon wagon = wagonFactory.get( wagonHint );

        String username = null;
        String password = null;

        if ( testContext.isSecureTest() )
        {
            username = testContext.getUsername();
            password = testContext.getPassword();
        }

        new WagonDeployer(
            wagon, wagonHint, username, password, repositoryUrl, fileToDeploy, artifactPath,
            nexusRestClient.getTestContext()
        ).deploy();
    }

    public int deployUsingGavWithRest( final String repositoryId,
                                       final Gav gav,
                                       final File fileToDeploy )
        throws IOException
    {
        return deployUsingGavWithRest(
            nexusRestClient.toNexusURL( "service/local/artifact/maven/content" ).toExternalForm(),
            repositoryId,
            gav,
            fileToDeploy
        );
    }

    public int deployUsingGavWithRest( final String restServiceURL,
                                       final String repositoryId,
                                       final Gav gav,
                                       final File fileToDeploy )
        throws IOException
    {
        // the method we are calling
        final PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        final String extension = gav.getExtension() != null ? gav.getExtension() : "";
        final String classifier = gav.getClassifier() != null ? gav.getClassifier() : "";

        final Part[] parts = {
            new StringPart( "r", repositoryId ),
            new StringPart( "g", gav.getGroupId() ),
            new StringPart( "a", gav.getArtifactId() ),
            new StringPart( "v", gav.getVersion() ),
            new StringPart( "p", extension ),
            new StringPart( "c", classifier ),
            new FilePart( fileToDeploy.getName(), fileToDeploy )
        };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        return nexusRestClient.executeHTTPClientMethod( filePost ).getStatusCode();

    }

    public int deployUsingPomWithRest( final String repositoryId,
                                       final File fileToDeploy,
                                       final File pomFile,
                                       final String classifier,
                                       final String extension )
        throws IOException
    {
        return deployUsingPomWithRest(
            nexusRestClient.toNexusURL( "service/local/artifact/maven/content" ).toExternalForm(),
            repositoryId, fileToDeploy, pomFile, classifier,
            extension
        );
    }

    public HttpMethod deployUsingPomWithRestReturnResult( final String repositoryId,
                                                          final File fileToDeploy,
                                                          final File pomFile,
                                                          final String classifier,
                                                          final String extension )
        throws IOException
    {
        return deployUsingPomWithRestReturnResult(
            nexusRestClient.toNexusURL( "service/local/artifact/maven/content" ).toExternalForm(),
            repositoryId, fileToDeploy, pomFile, classifier, extension
        );
    }

    public HttpMethod deployUsingPomWithRestReturnResult( final String restServiceURL,
                                                          final String repositoryId,
                                                          final File fileToDeploy,
                                                          final File pomFile,
                                                          final String classifier,
                                                          final String extension )
        throws IOException
    {
        // the method we are calling
        final PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        final String fixedClassifier = ( classifier == null ) ? "" : classifier;
        final String fixedExtension = ( extension == null ) ? "" : extension;

        final Part[] parts = {
            new StringPart( "r", repositoryId ),
            new StringPart( "e", fixedExtension ),
            new StringPart( "c", fixedClassifier ),
            new StringPart( "hasPom", "true" ),
            new FilePart( pomFile.getName(), pomFile ),
            new FilePart( fileToDeploy.getName(), fileToDeploy )
        };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        LOG.debug( "URL:  " + restServiceURL );
        LOG.debug( "Method: Post" );
        LOG.debug( "params: " );
        LOG.debug( "\tr: " + repositoryId );
        LOG.debug( "\thasPom: true" );
        LOG.debug( "\tpom: " + pomFile );
        LOG.debug( "\tfileToDeploy: " + fileToDeploy );

        return nexusRestClient.executeHTTPClientMethod( filePost );
    }

    public int deployUsingPomWithRest( final String restServiceURL,
                                       final String repositoryId,
                                       final File fileToDeploy,
                                       final File pomFile,
                                       final String classifier,
                                       final String extension )
        throws IOException
    {
        return deployUsingPomWithRestReturnResult(
            restServiceURL, repositoryId, fileToDeploy, pomFile, classifier, extension
        ).getStatusCode();
    }

    public HttpMethod deployPomWithRest( final String repositoryId,
                                         final File pomFile )
        throws IOException
    {
        // the method we are calling
        final PostMethod filePost = new PostMethod(
            nexusRestClient.toNexusURL( "service/local/artifact/maven/content" ).toExternalForm()
        );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        final Part[] parts = {
            new StringPart( "r", repositoryId ),
            new StringPart( "hasPom", "true" ),
            new FilePart( pomFile.getName(), pomFile )
        };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        LOG.debug( "URL:  " + filePost.getURI() );
        LOG.debug( "Method: Post" );
        LOG.debug( "params: " );
        LOG.debug( "\tr: " + repositoryId );
        LOG.debug( "\thasPom: true" );
        LOG.debug( "\tpom: " + pomFile );

        return nexusRestClient.executeHTTPClientMethod( filePost );
    }

    public int deployWithRest( final String repositoryId,
                               final String groupId,
                               final String artifactId,
                               final String version,
                               final String classifier,
                               final String extension,
                               final File fileToDeploy )
        throws IOException
    {
        return deployWithRest(
            nexusRestClient.toNexusURL( "service/local/artifact/maven/content" ).toExternalForm(),
            repositoryId,
            groupId,
            artifactId,
            version,
            classifier,
            extension,
            fileToDeploy
        );
    }

    public int deployWithRest( final String restServiceURL,
                               final String repositoryId,
                               final String groupId,
                               final String artifactId,
                               final String version,
                               final String classifier,
                               final String extension,
                               final File fileToDeploy )
        throws IOException
    {
        // the method we are calling
        final PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

        final Part[] parts = {
            new StringPart( "r", repositoryId ),
            new StringPart( "g", groupId ),
            new StringPart( "a", artifactId ),
            new StringPart( "v", version ),
            new StringPart( "p", extension == null ? "" : extension ),
            new StringPart( "c", classifier == null ? "" : classifier ),
            new FilePart( fileToDeploy.getName(), fileToDeploy )
        };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        return nexusRestClient.executeHTTPClientMethod( filePost ).getStatusCode();

    }

}
