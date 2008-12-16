package org.sonatype.nexus.plugins.migration.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ImportMessageUtil
{

    public static MigrationSummaryDTO importBackup( File testFile )
        throws IOException
    {
        String restServiceURL =
            TestProperties.getString( "nexus.base.url" ) + "service/local/migration/artifactory/upload";

        // the method we are calling
        PostMethod filePost = new PostMethod( restServiceURL );
        filePost.getParams().setBooleanParameter( HttpMethodParams.USE_EXPECT_CONTINUE, true );

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
