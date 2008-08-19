package org.sonatype.nexus.test.utils;

import java.io.File;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.sonatype.nexus.artifact.Gav;

public class MavenDeployer
{

    public static String deploy( Gav gav, String repositoryUrl, File fileToDeploy, File settings )
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

        if ( consoleOutput.contains( "BUILD ERROR" ) )
        {
            throw new CommandLineException( "Process failed: \n" + cli.toString() + "\nBUILD ERROR token found\n"
                + "Process output:\n" + consoleOutput );
        }

        return consoleOutput;
    }

}
