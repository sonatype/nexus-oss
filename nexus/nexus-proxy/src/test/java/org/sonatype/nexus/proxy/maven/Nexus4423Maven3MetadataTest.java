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
package org.sonatype.nexus.proxy.maven;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.maven.gav.Gav;

public class Nexus4423Maven3MetadataTest
    extends AbstractProxyTestEnvironment
{

    private Logger log = LoggerFactory.getLogger(Nexus4423Maven3MetadataTest.class);

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        return new M2TestsuiteEnvironmentBuilder( ss );
    }

    @Test
    public void testMaven3MetadataShouldSucceed()
        throws Exception
    {
        final Gav gav =
            new Gav( "org.exoplatform.social", "exo.social.packaging.pkg", "1.2.1-SNAPSHOT", null, "pom", null, null,
                null, false, null, false, null );

        final MavenRepository mavenRepository =
            getRepositoryRegistry().getRepositoryWithFacet( "nexus4423-snapshot", MavenRepository.class );

        ArtifactStoreRequest gavRequest = new ArtifactStoreRequest( mavenRepository, gav, false );

        ArtifactStoreHelper helper = mavenRepository.getArtifactStoreHelper();

        Gav resolvedGav = helper.resolveArtifact( gavRequest );

        if ( resolvedGav == null )
        {
            Assert.fail( "We should be able to resolve the gav " + gav.toString() );
        }

        log.error("resolvedGav.getSnapshotTimeStamp()" + resolvedGav.getSnapshotTimeStamp());
        Assert.assertEquals( "The expected version does not match!", "1.2.1-20110719.134341-19",
            resolvedGav.getVersion() );

        // ensure GMT 00:00 sine that is what is supposed to be in metadata files
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd.HHmmss", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));

        Assert.assertEquals( "The expected timestamp does not match!", "20110719.134341", df.format( new Date( resolvedGav.getSnapshotTimeStamp() ) ) );
        Assert.assertEquals( "The expected buildNumber does not match!", Integer.valueOf( 19 ),
            resolvedGav.getSnapshotBuildNumber() );
    }

    @Test
    public void testMaven3MetadataShouldFailWithoutNexus4423Fixed()
        throws Exception
    {
        final Gav gav =
            new Gav( "org.exoplatform.social", "exo.social.packaging.pkg", "1.2.1-SNAPSHOT", "tomcat", "zip", null,
                null, null, false, null, false, null );

        final MavenRepository mavenRepository =
            getRepositoryRegistry().getRepositoryWithFacet( "nexus4423-snapshot", MavenRepository.class );

        ArtifactStoreRequest gavRequest = new ArtifactStoreRequest( mavenRepository, gav, false );

        ArtifactStoreHelper helper = mavenRepository.getArtifactStoreHelper();

        Gav resolvedGav = helper.resolveArtifact( gavRequest );

        if ( resolvedGav == null )
        {
            Assert.fail( "We should be able to resolve the gav " + gav.toString() );
        }

        Assert.assertEquals( "The expected version does not match!", "1.2.1-20110719.092007-17",
            resolvedGav.getVersion() );

        // ensure GMT 00:00 sine that is what is supposed to be in metadata files
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd.HHmmss", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));

        Assert.assertEquals( "The expected timestamp does not match!", "20110719.092007", df.format(new Date( resolvedGav.getSnapshotTimeStamp() ) ) );
        Assert.assertEquals( "The expected buildNumber does not match!", Integer.valueOf( 17 ),
            resolvedGav.getSnapshotBuildNumber() );
    }
}
