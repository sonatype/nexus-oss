package org.sonatype.nexus.integrationtests.nexus1230;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.TestProperties;

public class ImportMessageUtil
{

    public static int importBackup( File testFile )
        throws IOException
    {
        String restServiceURL =
            TestProperties.getString( "nexus.base.url" ) + "service/local/migration/artifactory/content";

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

        return RequestFacade.executeHTTPClientMethod( new URL( restServiceURL ), filePost ).getStatusCode();

    }

}
