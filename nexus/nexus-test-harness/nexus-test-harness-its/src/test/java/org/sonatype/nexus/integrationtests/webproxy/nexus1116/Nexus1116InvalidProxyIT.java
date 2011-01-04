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
package org.sonatype.nexus.integrationtests.webproxy.nexus1116;

import static org.sonatype.nexus.integrationtests.ITGroups.PROXY;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.sonatype.nexus.integrationtests.webproxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus1116InvalidProxyIT
    extends AbstractNexusWebProxyIntegrationTest
    implements Runnable
{

    @Test(groups = PROXY)
    public void checkInvalidProxy()
        throws Exception
    {
        if ( true )
        {
            printKnownErrorButDoNotFail( getClass(), "downloadArtifactOverWebProxy" );
            return;
        }

        Thread thread = new Thread( this );
        thread.setDaemon( true );// don't stuck VM
        thread.start();
        for ( int i = 0; i < 100; i++ )
        {
            String status = this.status;

            if ( status.startsWith( "fail" ) )
            {
                Assert.fail( "Verifier fail: " + status );
            }
            else if ( status.equals( "executed" ) )
            {
                // finished ok
                return;
            }

            Thread.yield();
            Thread.sleep( 200 );
        }

        Assert.fail( "Verifier didn't runn after 20 seconds: " + this.status );
    }

    private String status = "notStarted";

    public void run()
    {
        status = "started";
        File mavenProject = getTestFile( "pom.xml" ).getParentFile();
        Verifier verifier;
        try
        {
            verifier = new Verifier( mavenProject.getAbsolutePath(), false );
            status = "verifierCreated";
        }
        catch ( VerificationException e )
        {
            status = "failCreation" + e.getMessage();
            return;
        }

        System.setProperty( "maven.home", TestProperties.getString( "maven.instance" ) );

        File mavenRepository = new File( TestProperties.getString( "maven.local.repo" ) );
        verifier.setLocalRepo( mavenRepository.getAbsolutePath() );

        verifier.resetStreams();

        List<String> options = new ArrayList<String>();
        options.add( "-X" );
        options.add( "-Dmaven.repo.local=" + mavenRepository.getAbsolutePath() );
        options.add( "-s " + getOverridableFile( "settings.xml" ) );
        verifier.setCliOptions( options );

        status = "pre-execute";
        try
        {
            verifier.executeGoal( "dependency:resolve" );
            status = "executed";
        }
        catch ( VerificationException e )
        {
            status = "failExecute" + e.getMessage();
        }
    }

}
