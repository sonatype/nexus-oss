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
package org.sonatype.nexus.plugins.mavenbridge;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.junit.Test;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.plugins.mavenbridge.internal.FileItemModelSource;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;

public class MavenBridgeTest
    extends AbstractMavenRepoContentTests
{
    protected NexusMavenBridge mavenBridge;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        mavenBridge = lookup( NexusMavenBridge.class );

        shutDownSecurity();
    }

    @Test
    public void testSimple()
        throws Exception
    {
        Assert.assertNotNull( mavenBridge );

        MavenRepository publicRepo = repositoryRegistry.getRepositoryWithFacet( "public", MavenGroupRepository.class );

        ResourceStoreRequest req =
            new ResourceStoreRequest( "/org/apache/maven/apache-maven/3.0.3/apache-maven-3.0.3.pom" );

        StorageFileItem pomItem = (StorageFileItem) publicRepo.retrieveItem( req );

        ModelSource pomSource = new FileItemModelSource( pomItem );

        List<MavenRepository> participants =
            Arrays.asList( pomItem.getRepositoryItemUid().getRepository().adaptToFacet( MavenRepository.class ) );

        Model model = mavenBridge.buildModel( pomSource, participants );

        // very simple check: if interpolated/effective, license node is present, but if you look
        // at pom above that has no license node. Hence, if present, it means parent found and successfully calculated
        // effective
        Assert.assertTrue( model.getLicenses().size() > 0 );


        // for debug
        //MavenXpp3Writer w = new MavenXpp3Writer();
        //w.write( System.out, model );
    }
}
