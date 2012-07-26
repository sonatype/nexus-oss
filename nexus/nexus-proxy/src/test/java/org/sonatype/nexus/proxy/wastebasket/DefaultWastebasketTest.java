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
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.proxy.walker.WalkerProcessor;
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
        wastebasket.purgeAll( DefaultWastebasket.ALL );

        assertThat( basketDir, FileMatchers.isDirectory() );
        assertThat( basketDir, FileMatchers.isEmpty() );
    }

    /**
     * NXCM-4474 Check that if a directory is removed by some other means while it has being emptied the walker will not
     * fail.
     *
     * The test will add a walker processor in front of wastebasket processor that will delete the first
     * collection (directory) it finds. In that way by the time that wastebasket processor wants to remove it, it is
     * already removed. Yet, this should not end up in an error but just silently skip it.
     */
    @Test
    public void walkingDoesNotFailIfDirectoryRemoved()
        throws Exception
    {
        final File repoLocation = this.addRepository( "active-repo" );
        final File repoTrashLocation = new File( repoLocation, ".nexus/trash" );
        final File trashContent = new File( "src/test/resources/active-repo/.nexus/trash" );
        FileUtils.copyDirectoryStructure( trashContent, repoTrashLocation );

        final DefaultWastebasket wastebasket = (DefaultWastebasket) this.lookup( Wastebasket.class );
        final Walker walker = wastebasket.getWalker();
        wastebasket.setWalker(
            new Walker()
            {
                @Override
                public void walk( final WalkerContext context )
                    throws WalkerException
                {
                    context.getProcessors().add( 0, new AbstractWalkerProcessor()
                    {
                        @Override
                        public void processItem( final WalkerContext context, final StorageItem item )
                            throws Exception
                        {
                            // do noting
                        }

                        @Override
                        public void onCollectionExit( final WalkerContext context, final StorageCollectionItem coll )
                            throws Exception
                        {
                            System.out.println( coll.getPath() );
                            FileUtils.deleteDirectory( new File( repoLocation, coll.getPath() ) );
                        }
                    } );
                    walker.walk( context );
                }
            }
        );
        wastebasket.purgeAll( DefaultWastebasket.ALL );
    }

}
