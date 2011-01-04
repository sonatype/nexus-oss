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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

public class MavenDeployer
{

    private static Verifier createVerifier( Gav gav, String repositoryUrl, File fileToDeploy, File settings,
                                            String[] extraOptions )
        throws VerificationException, IOException
    {
        File mavenProjectDir = new File( "target" );
        mavenProjectDir.mkdirs();

        System.setProperty( "maven.home", TestProperties.getString( "maven-basedir" ) );

        Verifier verifier = new Verifier( mavenProjectDir.getAbsolutePath(), false );

        String logname = "logs/maven-deploy/" + gav.getGroupId() + "/" + fileToDeploy.getName() + ".log";
        new File( verifier.getBasedir(), logname ).getParentFile().mkdirs();
        verifier.setLogFileName( logname );

        verifier.setLocalRepo( TestProperties.getFile( "maven.local.repo" ).getAbsolutePath() );

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

        if ( extraOptions != null )
        {
            options.addAll( Arrays.asList( extraOptions ) );
        }

        verifier.setCliOptions( options );
        return verifier;
    }

    public static Verifier deployAndGetVerifier( Gav gav, String repositoryUrl, File fileToDeploy, File settings,
                                                 String... extraOptions )
        throws VerificationException, IOException
    {
        Verifier verifier = createVerifier( gav, repositoryUrl, fileToDeploy, settings, extraOptions );
        // verifier.executeGoal( "deploy:deploy-file" );

        Map<String, String> args = new HashMap<String, String>();
        args.put( "url", repositoryUrl );
        args.put( "file", fileToDeploy.getAbsolutePath() );
        args.put( "groupId", gav.getGroupId() );
        args.put( "artifactId", gav.getArtifactId() );
        args.put( "version", gav.getVersion() );
        args.put( "packaging", gav.getExtension() );
        if ( gav.getClassifier() != null )
        {
            args.put( "classifier", gav.getClassifier() );
        }

        Properties props = new Properties();
        props.putAll( args );

        verifier.setSystemProperties( props );

        verifier.executeGoal( "org.apache.maven.plugins:maven-deploy-plugin:2.5:deploy-file", args );

        return verifier;

    }

}
