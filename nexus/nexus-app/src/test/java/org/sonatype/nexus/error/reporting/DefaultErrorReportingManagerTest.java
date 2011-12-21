/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.error.reporting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.ExceptionUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.swizzle.jira.Issue;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.jira.AttachmentHandler;
import org.sonatype.jira.mock.MockAttachmentHandler;
import org.sonatype.jira.mock.StubJira;
import org.sonatype.jira.test.JiraXmlRpcTestServlet;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.util.StringDigester;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;

public class DefaultErrorReportingManagerTest
    extends AbstractNexusTestCase
{
    private DefaultErrorReportingManager manager;

    private NexusConfiguration nexusConfig;

    private File unzipHomeDir = null;

    private JettyServerProvider provider;

    @Override
    protected void setUp()
        throws Exception
    {
        setupJiraMock( "src/test/resources/jira-mock.db" );

        super.setUp();

        unzipHomeDir = new File( getPlexusHomeDir(), "unzip" );
        unzipHomeDir.mkdirs();

        nexusConfig = lookup( NexusConfiguration.class );

        manager = (DefaultErrorReportingManager) lookup( ErrorReportingManager.class );
    }

    private void setupJiraMock( String dbPath )
        throws FileNotFoundException, IOException, Exception, MalformedURLException
    {
        StubJira mock = new StubJira();
        FileInputStream in = null;
        try
        {
            in = new FileInputStream( dbPath );
            mock.setDatabase( IOUtil.toString( in ) );

            MockAttachmentHandler handler = new MockAttachmentHandler();
            handler.setMock( mock );
            List<AttachmentHandler> handlers = Arrays.<AttachmentHandler> asList( handler );
            provider = new JettyServerProvider();
            provider.addServlet( new JiraXmlRpcTestServlet( mock, provider.getUrl(), handlers ) );
            provider.start();
        }
        finally
        {
            IOUtil.close( in );
        }
    }

    @Override
    protected void customizeContext( final Context ctx )
    {
        try
        {
            ctx.put( "pr.serverUrl", provider.getUrl().toString() );
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
            ctx.put( "pr.serverUrl", "https://issues.sonatype.org" );
        }
        ctx.put( "pr.auth.login", "sonatype_problem_reporting" );
        ctx.put( "pr.auth.password", "____" );
        ctx.put( "pr.project", "SBOX" );
        ctx.put( "pr.component", "Nexus" );
        ctx.put( "pr.issuetype.default", "1" );
        super.customizeContext( ctx );
    }

    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setClassPathScanning( "ON" );
        configuration.setAutoWiring( true );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        cleanDir( unzipHomeDir );
        provider.stop();
    }

    private void enableErrorReports( boolean useProxy )
        throws Exception
    {
        manager.setEnabled( true );
        try
        {
            manager.setJIRAUrl( provider.getUrl().toString() );
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
            manager.setJIRAUrl( "https://issues.sonatype.org" );
        }
        manager.setJIRAProject( "SBOX" );
        manager.setJIRAUsername( "jira" );
        manager.setJIRAPassword( "jira" );
        manager.setUseGlobalProxy( useProxy );

        nexusConfig.saveConfiguration();
    }

    @Test
    public void testJiraAccess()
        throws Exception
    {
        // enableProxy();
        enableErrorReports( false );

        ErrorReportRequest request = new ErrorReportRequest();

        try
        {
            throw new Exception( "Test exception " + Long.toHexString( System.currentTimeMillis() ) );
        }
        catch ( Exception e )
        {
            request.setThrowable( e );
        }

        // First make sure item doesn't already exist
        List<Issue> issues =
            manager.retrieveIssues( "APR: " + request.getThrowable().getMessage(), manager.getValidJIRAUsername(),
                manager.getValidJIRAPassword() );

        Assert.assertNull( issues );

        manager.handleError( request );

        issues =
            manager.retrieveIssues( "APR: " + request.getThrowable().getMessage(), manager.getValidJIRAUsername(),
                manager.getValidJIRAPassword() );

        Assert.assertEquals( 1, issues.size() );

        manager.handleError( request );

        issues =
            manager.retrieveIssues( "APR: " + request.getThrowable().getMessage(), manager.getValidJIRAUsername(),
                manager.getValidJIRAPassword() );

        Assert.assertEquals( 1, issues.size() );
    }

    @Test
    public void testPackageFiles()
        throws Exception
    {
        addBackupFiles( getConfHomeDir() );
        addDirectory( "test-directory", new String[] { "filename1.file", "filename2.file", "filename3.file" } );
        addDirectory( "nested-test-directory/more-nested-test-directory", new String[] { "filename1.file",
            "filename2.file", "filename3.file" } );

        Exception exception;

        try
        {
            throw new Exception( "Test exception" );
        }
        catch ( Exception e )
        {
            exception = e;
        }

        manager.setEnabled( true );
        manager.setJIRAProject( "NEXUS" );

        nexusConfiguration.saveConfiguration();

        ErrorReportRequest request = new ErrorReportRequest();
        request.setThrowable( exception );

        IssueSubmissionRequest subRequest =
            manager.buildRequest( request, manager.getValidJIRAUsername(), manager.isUseGlobalProxy() );

        assertEquals( "NEXUS", subRequest.getProjectId() );
        assertEquals( "APR: Test exception", subRequest.getSummary() );
        assertEquals(
            "The following exception occurred: " + StringDigester.LINE_SEPERATOR
                + ExceptionUtils.getFullStackTrace( exception ), subRequest.getDescription() );
        assertNotNull( subRequest.getProblemReportBundle() );

        extractZipFile( subRequest.getProblemReportBundle(), unzipHomeDir );

        assertTrue( unzipHomeDir.exists() );

        File[] files = unzipHomeDir.listFiles();

        assertNotNull( files );
        assertEquals( 6, files.length ); // TODO: was seven with the directory listing, but that was removed, as it
                                         // OOM'd

        files = unzipHomeDir.listFiles( new FileFilter()
        {
            public boolean accept( File pathname )
            {
                if ( pathname.isDirectory() && pathname.getName().equals( "test-directory" ) )
                {
                    return true;
                }

                return false;
            }
        } );

        assertEquals( 1, files.length );

        files = files[0].listFiles();

        boolean file1found = false;
        boolean file2found = false;
        boolean file3found = false;
        for ( File file : files )
        {
            if ( file.getName().equals( "filename1.file" ) )
            {
                file1found = true;
            }
            else if ( file.getName().equals( "filename2.file" ) )
            {
                file2found = true;
            }
            else if ( file.getName().equals( "filename3.file" ) )
            {
                file3found = true;
            }
        }

        assertTrue( file1found && file2found && file3found );

        files = unzipHomeDir.listFiles( new FileFilter()
        {
            public boolean accept( File pathname )
            {
                if ( pathname.isDirectory() && pathname.getName().equals( "nested-test-directory" ) )
                {
                    return true;
                }

                return false;
            }
        } );

        files = files[0].listFiles( new FileFilter()
        {
            public boolean accept( File pathname )
            {
                if ( pathname.isDirectory() && pathname.getName().equals( "more-nested-test-directory" ) )
                {
                    return true;
                }

                return false;
            }
        } );

        files = files[0].listFiles();

        file1found = false;
        file2found = false;
        file3found = false;
        for ( File file : files )
        {
            if ( file.getName().equals( "filename1.file" ) )
            {
                file1found = true;
            }
            else if ( file.getName().equals( "filename2.file" ) )
            {
                file2found = true;
            }
            else if ( file.getName().equals( "filename3.file" ) )
            {
                file3found = true;
            }
        }

        assertTrue( file1found && file2found && file3found );
    }

    private void addBackupFiles( File dir )
        throws Exception
    {
        new File( dir, "nexus.xml.bak" ).createNewFile();
        new File( dir, "security.xml.bak" ).createNewFile();
    }

    private void addDirectory( String path, String[] filenames )
        throws Exception
    {
        File confDir = new File( getConfHomeDir(), path );
        File unzipDir = new File( unzipHomeDir, path );
        confDir.mkdirs();
        unzipDir.mkdirs();

        for ( String filename : filenames )
        {
            new File( confDir, filename ).createNewFile();
        }
    }

    private void extractZipFile( File zipFile, File outputDirectory )
        throws Exception
    {
        FileInputStream fis = new FileInputStream( zipFile );
        ZipInputStream zin = null;

        try
        {
            zin = new ZipInputStream( new BufferedInputStream( fis ) );

            ZipEntry entry;
            while ( ( entry = zin.getNextEntry() ) != null )
            {
                FileOutputStream fos = new FileOutputStream( new File( outputDirectory, entry.getName() ) );
                BufferedOutputStream bos = null;

                try
                {
                    byte[] buffer = new byte[2048];
                    bos = new BufferedOutputStream( fos, 2048 );

                    int count;
                    while ( ( count = zin.read( buffer, 0, buffer.length ) ) != -1 )
                    {
                        bos.write( buffer, 0, count );
                    }

                }
                finally
                {
                    if ( bos != null )
                    {
                        bos.close();
                    }
                }
            }
        }
        finally
        {
            if ( zin != null )
            {
                zin.close();
            }
        }
    }

    @Test
    public void testTaskFailure()
        throws Exception
    {
        // since Timeline moved into plugin, we need EventInspectorHost too
        // That's why we add a "ping" for Nexus component, and it installs the EventInspectorHost too
        // awake nexus, to awake EventInspector host too
        lookup( Nexus.class );
        // we will need this to properly wait the async event inspectors to finish
        final EventInspectorHost eventInspectorHost = lookup( EventInspectorHost.class );

        enableErrorReports( false );

        String msg = "Runtime exception " + Long.toHexString( System.currentTimeMillis() );
        ExceptionTask task = (ExceptionTask) lookup( SchedulerTask.class, "ExceptionTask" );
        task.setMessage( msg );

        // First make sure item doesn't already exist
        List<Issue> issues =
            manager.retrieveIssues( "APR: " + new RuntimeException( msg ).getMessage(), manager.getValidJIRAUsername(),
                manager.getValidJIRAPassword() );

        Assert.assertNull( issues );

        doCall( task, eventInspectorHost );

        issues =
            manager.retrieveIssues( "APR: " + new RuntimeException( msg ).getMessage(), manager.getValidJIRAUsername(),
                manager.getValidJIRAPassword() );

        Assert.assertEquals( 1, issues.size() );

        doCall( task, eventInspectorHost );

        issues =
            manager.retrieveIssues( "APR: " + new RuntimeException( msg ).getMessage(), manager.getValidJIRAUsername(),
                manager.getValidJIRAPassword() );

        Assert.assertEquals( 1, issues.size() );
    }

    private void doCall( final NexusTask<?> task, final EventInspectorHost inspectorHost )
        throws InterruptedException
    {
        try
        {
            task.call();
        }
        catch ( Throwable t )
        {
        }
        finally
        {
            do
            {
                Thread.sleep( 100 );
            }
            while ( !inspectorHost.isCalmPeriod() );
        }
    }

}
