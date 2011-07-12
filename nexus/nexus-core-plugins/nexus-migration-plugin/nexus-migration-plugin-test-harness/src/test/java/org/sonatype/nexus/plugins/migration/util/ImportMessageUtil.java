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

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.mortbay.log.Log;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationResource;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ImportMessageUtil
{
    private static XStream xstream;

    private static MediaType mediaType;

    static
    {
        xstream = XStreamFactory.getXmlXStream();

        mediaType = MediaType.APPLICATION_XML;
    }

    public static MigrationSummaryDTO importBackup( File testFile )
        throws IOException
    {

        String serviceURI = "service/local/migration/artifactory/filelocation";

        FileLocationResource data = new FileLocationResource();

        data.setFileLocation( testFile.getAbsolutePath() );

        FileLocationRequestDTO request = new FileLocationRequestDTO();

        request.setData( data );

        XStreamRepresentation requestRepresentation = new XStreamRepresentation( xstream, "", mediaType );

        requestRepresentation.setPayload( request );

        Response response = RequestFacade.sendMessage( serviceURI, Method.POST, requestRepresentation );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Invalid response for server " + response.getEntity().getText() + "\n" + response.getStatus() );
        }

        String responseString = response.getEntity().getText();

        Log.debug( "Response Text: " + responseString );

        XStreamRepresentation responseRepresentation = new XStreamRepresentation( xstream, responseString, mediaType );

        MigrationSummaryResponseDTO migrationSummaryResponse =
            (MigrationSummaryResponseDTO) responseRepresentation.getPayload( new MigrationSummaryResponseDTO() );

        return migrationSummaryResponse.getData();

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
