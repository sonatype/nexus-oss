/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.sonatype.nexus.artifact.Gav;

public class MavenDeployer
{

    private static Verifier createVerifier( Gav gav, String repositoryUrl, File fileToDeploy, File settings )
        throws VerificationException, IOException
    {
        File mavenProjectDir = new File( "target" );
        mavenProjectDir.mkdirs();

        Verifier verifier = new Verifier( mavenProjectDir.getAbsolutePath(), false );
        verifier.setAutoclean( false );
        verifier.resetStreams();
        
        List<String> options = new ArrayList<String>();
        if ( settings != null )
        {
            options.add( "-s " + settings.getAbsolutePath() );
        }
        
        options.add( "-Durl=\'" + repositoryUrl + "\'" );
        options.add( "-Dfile=\'" + fileToDeploy + "\'" );
        options.add( "-DgroupId=\'" + gav.getGroupId() + "\'" );
        options.add( "-DartifactId=\'" + gav.getArtifactId() + "\'" );
        options.add( "-Dversion=\'" + gav.getVersion() + "\'" );
        options.add( "-Dpackaging=\'" + gav.getExtension() + "\'" );
        
        
        verifier.setCliOptions( options );
        return verifier;
    }

    public static Verifier deployAndGetVerifier( Gav gav, String repositoryUrl, File fileToDeploy, File settings ) throws VerificationException, IOException
    {
        Verifier verifier = createVerifier( gav, repositoryUrl, fileToDeploy, settings );
//        verifier.executeGoal( "deploy:deploy-file" );
        
        Map<String, String> args = new HashMap<String, String>();
        args.put( "url", repositoryUrl );
        args.put( "file", fileToDeploy.getAbsolutePath() );
        args.put( "groupId", gav.getGroupId() );
        args.put( "artifactId", gav.getArtifactId() );
        args.put( "version", gav.getVersion() );
        args.put( "packaging", gav.getExtension() );
        
        Properties props = new Properties();
        props.putAll( args );
        
        verifier.setSystemProperties( props );
        
        verifier.executeGoal( "deploy:deploy-file", args );
        
        return verifier;
        
    }
    
    @Deprecated
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
        if ( consoleOutput.contains( "FATAL ERROR" ) )
        {
            throw new CommandLineException( "Process failed: \n" + cli.toString() + "\nFATAL ERROR token found\n"
                                            + "Process output:\n" + consoleOutput );
        }

        return consoleOutput;
    }

}
