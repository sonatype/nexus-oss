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
package org.sonatype.nexus.plugins.p2.repository.its;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public abstract class AbstractNexusP2IT
    extends AbstractNexusIntegrationTest
{

    protected AbstractNexusP2IT( final String testRepositoryId )
    {
        super( testRepositoryId );
    }

    protected void installUsingP2( final String repositoryURL, final String installIU, final String destination )
        throws Exception
    {
        installUsingP2( repositoryURL, installIU, destination, null );
    }

    protected void installUsingP2( final String repositoryURL, final String installIU, final String destination,
                                   final Map<String, String> extraEnv )
        throws Exception
    {
        FileUtils.deleteDirectory( destination );

        final File basedir = ResourceExtractor.simpleExtractResources( getClass(), "/run-p2" );
        final Verifier verifier = new Verifier( basedir.getAbsolutePath() );

        final Map<String, String> env = new HashMap<String, String>();
        env.put( "org.eclipse.ecf.provider.filetransfer.retrieve.readTimeout", "30000" );
        env.put( "p2.installIU", installIU );
        env.put( "p2.destination", destination );
        env.put( "p2.metadataRepository", repositoryURL );
        env.put( "p2.artifactRepository", repositoryURL );
        env.put( "p2.profile", getTestId() );

        if ( extraEnv != null )
        {
            env.putAll( extraEnv );
        }

        verifier.executeGoals( Arrays.asList( "verify" ), env );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
    }

}
