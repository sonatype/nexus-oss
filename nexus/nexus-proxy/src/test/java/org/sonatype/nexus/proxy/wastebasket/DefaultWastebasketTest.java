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

package org.sonatype.nexus.proxy.wastebasket;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;

/**
 * Tests the {@link DefaultWastebasket} class.
 */
public class DefaultWastebasketTest
    extends AbstractNexusTestEnvironment
{

    private File addRepository( String id )
        throws Exception
    {
        // ading one hosted only
        M2Repository repo = (M2Repository) lookup( Repository.class, "maven2" );

        CRepository repoConf = new DefaultCRepository();

        repoConf.setProviderRole( Repository.class.getName() );
        repoConf.setProviderHint( "maven2" );
        repoConf.setId( id );

        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );

        final File repoLocation = new File( getBasedir(), "target/test-classes/" + id );
        repoConf.getLocalStorage().setUrl( repoLocation.toURI().toURL().toString() );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );
        M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
        exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );

        repo.configure( repoConf );

        lookup( ApplicationConfiguration.class ).getConfigurationModel().addRepository( repoConf );

        lookup( RepositoryRegistry.class ).addRepository( repo );

        return repoLocation;
    }

    /**
     * Tests that that empting the trash does NOT fail for an out-of-service repository.<BR>
     * Verifies fix for: NEXUS-4554 - Out of service proxy repo appears to cause Empty Trash task to abort as BROKEN
     * 
     * @throws Exception
     */
    @Test
    public void testPurgeAllWithAnOutOfServiceRepo()
        throws Exception
    {

        this.addRepository( "out-of-service-repo" );
        File repoLocation = this.addRepository( "active-repo" );
        assertThat( new File( repoLocation, ".nexus/trash" ), FileMatchers.isDirectory() );

        M2Repository outOfServiceRepo =
            (M2Repository) this.lookup( RepositoryRegistry.class ).getRepository( "out-of-service-repo" );
        outOfServiceRepo.setLocalStatus( LocalStatus.OUT_OF_SERVICE );
        outOfServiceRepo.commitChanges();

        Wastebasket wastebasket = this.lookup( Wastebasket.class );
        wastebasket.purgeAll( 1L );

        // NEXUS-4642 check if directories were deleted
        assertThat( new File( repoLocation, ".nexus/trash" ), FileMatchers.isDirectory() );
        assertThat( new File( repoLocation, ".nexus/trash" ), FileMatchers.isEmpty() );
    }

    /**
     * NEXUS-4468 Check if nexus is cleaning up legacy trash properly (deleting content w/o deleting the directory)
     */
    @Test
    public void testPurgeAllLegacyTrash()
        throws Exception
    {

        this.addRepository( "out-of-service-repo" );
        this.addRepository( "active-repo" );

        ApplicationConfiguration applicationConfiguration = lookup( ApplicationConfiguration.class );
        File basketDir =
            applicationConfiguration.getWorkingDirectory( AbstractRepositoryFolderCleaner.GLOBAL_TRASH_KEY );

        // fill legacy trash
        basketDir.mkdirs();
        File trashContent = new File( "src/test/resources/active-repo/.nexus/trash" );
        FileUtils.copyDirectoryStructure( trashContent, basketDir );

        M2Repository outOfServiceRepo =
            (M2Repository) this.lookup( RepositoryRegistry.class ).getRepository( "out-of-service-repo" );
        outOfServiceRepo.setLocalStatus( LocalStatus.OUT_OF_SERVICE );
        outOfServiceRepo.commitChanges();

        Wastebasket wastebasket = this.lookup( Wastebasket.class );
        wastebasket.purgeAll( 1L );

        assertThat( basketDir, FileMatchers.isDirectory() );
        assertThat( basketDir, FileMatchers.isEmpty() );
    }

}
