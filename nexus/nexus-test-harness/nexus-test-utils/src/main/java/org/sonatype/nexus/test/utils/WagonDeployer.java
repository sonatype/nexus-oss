/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
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

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.Launcher;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.plexus.classworlds.io.ClassworldsConfWriter;
import org.sonatype.plexus.classworlds.io.ClassworldsIOException;
import org.sonatype.plexus.classworlds.model.ClassworldsAppConfiguration;
import org.sonatype.plexus.classworlds.model.ClassworldsRealmConfiguration;
import org.sonatype.plexus.classworlds.validator.ClassworldsModelValidator;
import org.sonatype.plexus.classworlds.validator.ClassworldsValidationResult;

/**
 * Due to a <a href='http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4941958'>bug</a> ( i wouldn't call this a
 * feature request ) You cannot change change/clear the cache of the default Authenticator, and that is what the Wagon
 * uses. So to work around this very stupid problem I am forking the VM to do a deploy. </p> I wanted to be able to
 * catch each exception, yes, there are better ways to do this, i know.... but this was really easy... and its only for
 * testing... </p> We can look into using the forked app-booter, but that might be a little over kill, and we still
 * couldn't trap the individual exceptions.
 */
public class WagonDeployer
{

    private Wagon wagon;

    private String protocol = "http";

    private String username;

    private String password;

    private String repositoryUrl;

    private File fileToDeploy;

    private String artifactPath;

    private final TestContext testContext;

    private static final Logger LOG = LoggerFactory.getLogger( WagonDeployer.class );

    public WagonDeployer( final Wagon wagon,
                          final String protocol,
                          final String username,
                          final String password,
                          final String repositoryUrl,
                          final File fileToDeploy,
                          final String artifactPath,
                          final TestContext testContext )
    {
        super();
        this.wagon = wagon;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.repositoryUrl = repositoryUrl;
        this.fileToDeploy = fileToDeploy;
        this.artifactPath = artifactPath;
        this.testContext = testContext;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public File getFileToDeploy()
    {
        return fileToDeploy;
    }

    public String getArtifactPath()
    {
        return artifactPath;
    }

    public void deploy()
        throws ComponentLookupException, ConnectionException, AuthenticationException, TransferFailedException,
        ResourceDoesNotExistException, AuthorizationException
    {
        Repository repository = new Repository();
        repository.setUrl( repositoryUrl );

        wagon.connect( repository, getWagonAuthenticationInfo() );
        wagon.put( fileToDeploy, artifactPath );
        wagon.disconnect();

    }

    public void forkDeploy( PlexusContainer container )
        throws InterruptedException, CommandLineException, ConnectionException, ComponentLookupException,
        TransferFailedException, AuthorizationException, ResourceDoesNotExistException, AuthenticationException
    {

        if ( true )
        {
            // warn people that this does not work. I would like to make something like this work later... but for now.
            // I need to move on...
            throw new NotImplementedException(
                "This method does not work due to some classworlds problem, most likely its my fault." );
        }

        String classPath = System.getProperty( "java.class.path" );
        String sureFireClassPath = null;
        File classworldsConf = null;
        if ( System.getProperty( "surefire.test.class.path" ) != null )
        {
            sureFireClassPath = System.getProperty( "surefire.test.class.path" );
            classworldsConf = this.writeClassworlds( sureFireClassPath );
        }
        else
        {
            classworldsConf = this.writeClassworlds( classPath );
        }

        Commandline cli = new Commandline();

        cli.setExecutable( "java" );
        cli.createArg().setLine( "-cp" );
        cli.createArg().setLine( classPath );
        cli.createArg().setLine( "-Dclassworlds.conf=\'" + classworldsConf.getAbsolutePath() + "\'" );
        cli.createArg().setLine( Launcher.class.getName() );

        cli.createArg().setLine( protocol );
        cli.createArg().setLine( username );
        cli.createArg().setLine( password );
        cli.createArg().setLine( repositoryUrl );
        cli.createArg().setLine( fileToDeploy.getAbsolutePath() );
        cli.createArg().setLine( artifactPath );

        this.executeCli( cli );

    }

    private void executeCli( Commandline cli )
        throws CommandLineException, ConnectionException, ComponentLookupException, TransferFailedException,
        AuthorizationException, ResourceDoesNotExistException, AuthenticationException, InterruptedException
    {

        CommandLineRunner runner = new CommandLineRunner();

        int status = runner.executeAndWait( cli );
        String consoleOutput = runner.getConsoleOutput();
        if ( status != 0 )
        {
            switch ( status )
            {
                case 1:
                    throw new CommandLineException(
                        "Process exit status was: " + status + "Process output:\n" + consoleOutput
                    );
                case 2:
                    throw new ConnectionException(
                        "Process exit status was: " + status + "Process output:\n" + consoleOutput
                    );
                case 3:
                    throw new AuthenticationException(
                        "Process exit status was: " + status + "Process output:\n" + consoleOutput
                    );
                case 4:
                    throw new TransferFailedException(
                        "Process exit status was: " + status + "Process output:\n" + consoleOutput
                    );
                case 5:
                    throw new ResourceDoesNotExistException(
                        "Process exit status was: " + status + "Process output:\n" + consoleOutput
                    );
                case 6:
                    throw new AuthorizationException(
                        "Process exit status was: " + status + "Process output:\n" + consoleOutput
                    );
                case 7:
                    throw new ComponentLookupException(
                        "Process exit status was: " + status + "Process output:\n" + consoleOutput, Wagon.ROLE, protocol
                    );
                default:
                    break;
            }
        }

    }

    private File writeClassworlds( String rawClassPath )
    {
        ClassworldsRealmConfiguration rootRealmConfig = new ClassworldsRealmConfiguration( "wagon" );

        rootRealmConfig.addLoadPatterns( Arrays.asList( rawClassPath.split( File.pathSeparator ) ) );

        ClassworldsAppConfiguration config = new ClassworldsAppConfiguration();

        config.setMainClass( WagonDeployer.class.toString() );
        config.addRealmConfiguration( rootRealmConfig );
        config.setMainRealm( rootRealmConfig.getRealmId() );

        ClassworldsValidationResult vr = new ClassworldsModelValidator().validate( config );
        if ( vr.hasErrors() )
        {
            throw new RuntimeException( vr.render() );
        }

        File classWorldsConfig = new File( "target/wagonDeploy/", "classworlds.conf" );
        classWorldsConfig.getParentFile().mkdirs();
        try
        {
            new ClassworldsConfWriter().write( classWorldsConfig, config );
        }
        catch ( ClassworldsIOException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
        return classWorldsConfig;
    }

    public static void main( String[] args, ClassWorld world )
    {
        main( args );
    }

    public static void main( String[] args )
    {
        LOG.debug( "sweet!" );

        if ( args == null || args.length != 6 )
        {
            LOG.debug(
                "Usage: java " + WagonDeployer.class.getName()
                    + " <protocol> <username> <password> <repositoryUrl> <fileToDeploy> <artifactPath>"
            );
            System.exit( 1 );
        }

        // try
        // {
        try
        {
            new WagonDeployer(
                null, args[0], args[1], args[2], args[3], new File( args[4] ), args[5], new TestContext()
            ).deploy();
        }
        catch ( ConnectionException e )
        {
            e.printStackTrace();
            System.exit( 2 );
        }
        catch ( AuthenticationException e )
        {
            e.printStackTrace();
            System.exit( 3 );
        }
        catch ( TransferFailedException e )
        {
            e.printStackTrace();
            System.exit( 4 );
        }
        catch ( ResourceDoesNotExistException e )
        {
            e.printStackTrace();
            System.exit( 5 );
        }
        catch ( AuthorizationException e )
        {
            e.printStackTrace();
            System.exit( 6 );
        }
        catch ( ComponentLookupException e )
        {
            e.printStackTrace();
            System.exit( 7 );
        }
    }

    public AuthenticationInfo getWagonAuthenticationInfo()
    {
        AuthenticationInfo authInfo = null;
        // check the text context to see if this is a secure test
        if ( testContext.isSecureTest() )
        {
            authInfo = new AuthenticationInfo();
            authInfo.setUserName( testContext.getUsername() );
            authInfo.setPassword( testContext.getPassword() );
        }
        return authInfo;
    }

    public static interface Factory
    {

        Wagon get( String protocol );

    }

}
