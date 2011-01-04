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
package org.sonatype.nexus.test.utils;

import java.io.File;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.util.FileUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ErrorReportRequest;
import org.sonatype.nexus.rest.model.ErrorReportRequestDTO;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

public class ErrorReportUtil
{
    private static XStream xstream = XStreamFactory.getXmlXStream();

    public static ErrorReportResponse generateProblemReport( String title, String description )
        throws IOException
    {
        return generateProblemReport( title, description, null, null );
    }

    public static ErrorReportResponse generateProblemReport( String title, String description, String jiraUser,
                                                             String jiraPassword )
        throws IOException
    {
        Response response = generateProblemResponse( title, description, jiraUser, jiraPassword );

        Assert.assertNotNull( response );

        if ( title != null )
        {
            final String text = response.getEntity().getText();

            Assert.assertTrue( response.getStatus().isSuccess(), text + "\n" + response.getStatus() );

            XStreamRepresentation representation = new XStreamRepresentation( xstream, text, MediaType.APPLICATION_XML );

            ErrorReportResponse responseObj =
                (ErrorReportResponse) representation.getPayload( new ErrorReportResponse() );

            return responseObj;
        }
        else
        {
            Assert.assertFalse( response.getStatus().isSuccess() );
            Assert.assertEquals( 400, response.getStatus().getCode() );
        }

        return null;
    }

    public static Response generateProblemResponse( String title, String description, String jiraUser,
                                                    String jiraPassword )
        throws IOException
    {
        ErrorReportRequest request = new ErrorReportRequest();
        request.setData( new ErrorReportRequestDTO() );
        request.getData().setTitle( title );
        request.getData().setDescription( description );
        if ( jiraUser != null )
        {
            request.getData().setErrorReportingSettings( new ErrorReportingSettings() );
            request.getData().getErrorReportingSettings().setJiraUsername( jiraUser );
            request.getData().getErrorReportingSettings().setJiraPassword( jiraPassword );
        }

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", MediaType.APPLICATION_XML );
        representation.setPayload( request );

        String serviceURI = "service/local/error_reporting";
        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );
        return response;
    }

    public static void cleanErrorBundleDir( String directory )
        throws IOException
    {
        File errorBundleDir = new File( directory + "/error-report-bundles" );

        if ( errorBundleDir.exists() )
        {
            FileUtils.deleteDirectory( errorBundleDir );
        }
    }

    public static void validateNoZip( String directory )
    {
        File errorBundleDir = new File( directory + "/error-report-bundles" );

        Assert.assertFalse( errorBundleDir.exists() );
    }

    public static void validateZipContents( String directory )
        throws IOException
    {
        File errorBundleDir = new File( directory + "/error-report-bundles" );

        Assert.assertTrue( errorBundleDir.exists() );

        File[] files = errorBundleDir.listFiles();

        Assert.assertNotNull( files );
        Assert.assertEquals( 1, files.length );
        Assert.assertTrue( files[0].getName().startsWith( "nexus-error-bundle" ) );
        Assert.assertTrue( files[0].getName().endsWith( ".zip" ) );

        validateZipContents( files[0] );
    }

    public static void validateZipContents( File file )
        throws IOException
    {
        boolean foundException = false;
        // boolean foundFileList = false;
        boolean foundContextList = false;
        boolean foundLog4j = false;
        boolean foundNexusXml = false;
        boolean foundSecurityXml = false;
        boolean foundSecurityConfigXml = false;
        boolean foundOthers = false;

        ZipFile zipFile = new ZipFile( file );

        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

        while ( enumeration.hasMoreElements() )
        {
            ZipEntry entry = enumeration.nextElement();

            if ( entry.getName().equals( "exception.txt" ) )
            {
                foundException = true;
            }
            // TODO: removed because the listing of the files OOM'd
            // else if ( entry.getName().equals( "fileListing.txt" ) )
            // {
            // foundFileList = true;
            // }
            else if ( entry.getName().equals( "contextListing.txt" ) )
            {
                foundContextList = true;
            }
            else if ( entry.getName().equals( "log4j.properties" ) )
            {
                foundLog4j = true;
            }
            else if ( entry.getName().equals( "nexus.xml" ) )
            {
                foundNexusXml = true;
            }
            else if ( entry.getName().equals( "security.xml" ) )
            {
                foundSecurityXml = true;
            }
            else if ( entry.getName().equals( "security-configuration.xml" ) )
            {
                foundSecurityConfigXml = true;
            }
            else
            {
                String confDir = AbstractNexusIntegrationTest.WORK_CONF_DIR;

                // any extra plugin config goes in the zip, so if we find something from the conf dir that is ok.
                if ( !new File( confDir, entry.getName() ).exists() )
                {
                    foundOthers = true;
                }
            }
        }

        Assert.assertTrue( foundException );
        // Assert.assertTrue( foundFileList );
        Assert.assertTrue( foundContextList );
        Assert.assertTrue( foundLog4j );
        Assert.assertTrue( foundNexusXml );
        Assert.assertTrue( foundSecurityXml );
        Assert.assertTrue( foundSecurityConfigXml );
        // plugins can input others!
        // Assert.assertFalse( foundOthers );
    }
}
