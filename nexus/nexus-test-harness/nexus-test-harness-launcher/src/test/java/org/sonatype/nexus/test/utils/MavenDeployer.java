package org.sonatype.nexus.test.utils;

import java.io.File;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.sonatype.nexus.artifact.Gav;

public class MavenDeployer
{

    public static void deploy( Gav gav, String repositoryUrl, File fileToDeploy, File settings )
        throws CommandLineException, InterruptedException
    {
        Commandline cli = new Commandline();

        cli.setExecutable( "mvn" );

        if ( settings != null )
        {
            cli.createArg().setLine( "--settings \'" + settings.getAbsolutePath() + "\'" );
        }

        cli.createArg().setLine( "deploy:deploy-file" );
        cli.createArg().setLine( "-Durl=\'" + repositoryUrl + "\'" );
        cli.createArg().setLine( "-Dfile=\'" + fileToDeploy + "\'" );
        cli.createArg().setLine( "-DgroupId=\'" + gav.getGroupId() + "\'" );
        cli.createArg().setLine( "-DartifactId=\'" + gav.getArtifactId() + "\'" );
        cli.createArg().setLine( "-Dversion=\'" + gav.getVersion() + "\'" );
        cli.createArg().setLine( "-Dpackaging=\'" + gav.getExtension() + "\'" );

        CommandLineRunner runner = new CommandLineRunner();

        int status = runner.executeAndWait( cli );
        String consoleOutput = runner.getConsoleOutput();

        if ( status != 0 )
        {
            throw new CommandLineException( "Process failed: \n" + cli.toString() + "\nProcess exit status was: "
                + status + "Process output:\n" + consoleOutput );
        }

    }

}
