/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration.util;

import hidden.org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ImportMessageUtil
{
    public static final String DEFAULT_EMAIL = "juven@mars.com";

    public static MigrationSummaryDTO importBackup( File testFile )
        throws IOException
    {
        String restServiceURL =
            TestProperties.getString( "nexus.base.url" ) + "service/local/migration/artifactory/upload";

        // the method we are calling
        PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );
        filePost.addRequestHeader( "accept", "application/xml" );

        /*
         * new StringPart( "r", repositoryId ), new StringPart( "g", gav.getGroupId() ), new StringPart( "a",
         * gav.getArtifactId() ), new StringPart( "v", gav.getVersion() ), new StringPart( "p", gav.getExtension() ),
         * new StringPart( "c", "" ),
         */
        Part[] parts = { new FilePart( testFile.getName(), testFile ) };

        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );

        HttpMethod method = RequestFacade.executeHTTPClientMethod( new URL( restServiceURL ), filePost );
        if ( Status.isSuccess( method.getStatusCode() ) )
        {
            XStream xs = XStreamFactory.getXmlXStream();
            MigrationSummaryResponseDTO response =
                (MigrationSummaryResponseDTO) xs.fromXML( method.getResponseBodyAsString() );
            return response.getData();
        }
        else
        {
            Assert.fail( "Returned code: " + method.getStatusCode() );
            return null;
        }

    }

    public static Response commitImport( MigrationSummaryDTO migrationSummary )
        throws IOException
    {
        MigrationSummaryRequestDTO request = new MigrationSummaryRequestDTO();
        request.setData( migrationSummary );

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        Response response =
            RequestFacade.sendMessage( "service/local/migration/artifactory/content", Method.POST, representation );

        return response;
    }

}
