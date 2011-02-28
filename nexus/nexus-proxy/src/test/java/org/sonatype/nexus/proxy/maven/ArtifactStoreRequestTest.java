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
package org.sonatype.nexus.proxy.maven;

import junit.framework.Assert;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;

public class ArtifactStoreRequestTest
    extends AbstractProxyTestEnvironment
{

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        return new M2TestsuiteEnvironmentBuilder( ss );
    }
    
    public void testNoDots() throws Exception
    {
        Gav gav = new Gav("nodots", "artifact", "1.0", null, "xml", null, null, null, false, null, false, null);
        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true, false );
        
        Assert.assertEquals( "/nodots/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
    }
    
    public void testDots() throws Exception
    {
        Gav gav = new Gav("a.bunch.of.dots.yeah", "artifact", "1.0", null, "xml", null, null, null, false, null, false, null);
        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true, false );
        
        Assert.assertEquals( "/a/bunch/of/dots/yeah/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
    }
    
    // undefined extra dot
//    public void testExtraDot() throws Exception
//    {
//        Gav gav = new Gav("extra..dot", "artifact", "1.0", null, "xml", null, null, null, false, false, null, false, null);
//        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
//        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true );
//        
//        Assert.assertEquals( "/extra/dot/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
//    }
    
    public void testGroupStartsWithDot() throws Exception
    {
        Gav gav = new Gav(".meta/foo/bar", "artifact", "1.0", null, "xml", null, null, null, false, null, false, null);
        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true, false );
        
        Assert.assertEquals( "/.meta/foo/bar/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
    }
    
    

}
