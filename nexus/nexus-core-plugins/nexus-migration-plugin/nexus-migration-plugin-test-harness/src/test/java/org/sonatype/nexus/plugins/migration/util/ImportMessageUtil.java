/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
