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

package org.sonatype.nexus.proxy.storage.local.fs;

import com.google.common.collect.Maps;
import com.google.common.io.FileBackedOutputStream;
import junit.framework.Assert;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static  org.mockito.Mockito.*;

/**
 * Tests {@link DefaultFSLocalRepositoryStorage}
 * @since 1.10.0
 */
public class DefaultFSLocalRepositoryStorageTest extends PlexusTestCaseSupport
{

    /**
     * Tests listing a directory, when a contained file does NOT exists.
     * @throws Exception
     */
    @Test
    public void testListFilesThrowsItemNotFoundException() throws Exception
    {

        File repoLocation  = new File( getBasedir(), "target/" + getClass().getSimpleName() + "/repo/" );

        // the contents of the "valid" directory, only contains a "valid.txt" file
        File validDir = new File( repoLocation, "valid/" );
        validDir.mkdirs();
        FileUtils.fileWrite( new File( validDir, "valid.txt" ), "UTF-8", "something valid" );
        Collection<File> validFileCollection = Arrays.asList( validDir.listFiles() );

        // the contents of the "invalid" directory, this dir contains a missing file
        File invalidDir = new File( repoLocation, "invalid/" );
        invalidDir.mkdirs();
        FileUtils.fileWrite( new File( invalidDir, "invalid.txt" ), "UTF-8", "something valid" );
        List<File> invalidFileCollection = new ArrayList<File>( Arrays.asList( invalidDir.listFiles() ) );
        invalidFileCollection.add( new File( invalidDir, "missing.txt") );


        // Mocks
        Wastebasket wastebasket = mock( Wastebasket.class );
        LinkPersister linkPersister = mock( LinkPersister.class );
        MimeUtil mimeUtil = mock( MimeUtil.class );
        Map<String, Long> repositoryContexts = Maps.newHashMap();

        // Mock FSPeer to return the results created above
        FSPeer fsPeer = mock( FSPeer.class );
        when( fsPeer.listItems( any( Repository.class ), any( ResourceStoreRequest.class ), eq( validDir ) ) ).thenReturn( validFileCollection );
        when( fsPeer.listItems( any(Repository.class), any(ResourceStoreRequest.class), eq( new File( repoLocation, "invalid/") ) ) ).thenReturn( invalidFileCollection );

        // create Repository Mock
        Repository repository = mock( Repository.class );
        when( repository.getLocalUrl() ).thenReturn( repoLocation.toURI().toURL().toString() );
        AttributesHandler attributesHandler = mock( AttributesHandler.class );
        when( repository.getAttributesHandler() ).thenReturn( attributesHandler );


        DefaultFSLocalRepositoryStorage localRepositoryStorageUnderTest = new DefaultFSLocalRepositoryStorage( wastebasket, linkPersister, mimeUtil, repositoryContexts, fsPeer );

        ResourceStoreRequest validRequest = new ResourceStoreRequest( "valid" );

        // positive test, valid.txt should be found
        Collection<StorageItem> items = localRepositoryStorageUnderTest.listItems( repository, validRequest );
        Assert.assertEquals( "items: "+ items, 1, items.size() );
        Assert.assertEquals( "valid.txt", items.iterator().next().getName() );

        // missing.txt was listed in this directory, but it does NOT exist, only invalid.txt should be found
        ResourceStoreRequest invalidRequest = new ResourceStoreRequest( "invalid" );
        items = localRepositoryStorageUnderTest.listItems( repository, invalidRequest );
        Assert.assertEquals( 1, items.size() );
        Assert.assertEquals( "invalid.txt", items.iterator().next().getName() );

    }
}
