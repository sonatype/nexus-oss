/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.blobstore

import org.sonatype.nexus.blobstore.api.BlobStore
import org.sonatype.nexus.blobstore.api.configuration.BlobStoreConfiguration
import org.sonatype.nexus.blobstore.api.configuration.ConfigurationStore
import org.sonatype.nexus.configuration.ApplicationDirectories
import org.sonatype.sisu.litmus.testsupport.TestSupport

import com.google.common.collect.Lists
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mock

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class BlobStoreManagerImplTest
    extends TestSupport
{

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder()

  @Mock
  ApplicationDirectories directories

  @Mock
  ConfigurationStore store

  BlobStoreManagerImpl underTest

  @Before
  public void setup() {
    when(directories.getWorkDirectory(anyString())).thenReturn(temporaryFolder.root)
    underTest = spy(new BlobStoreManagerImpl(directories, store))
  }

  @Test
  public void 'Can start with nothing configured'() throws Exception {
    when(store.list()).thenReturn(Lists.newArrayList())
    underTest.doStart()
    assert !underTest.browse()
  }

  @Test
  public void 'Can start with existing configuration'() throws Exception {
    BlobStore blobStore = mock(BlobStore)
    doReturn(blobStore).when(underTest).newBlobStore(any(BlobStoreConfiguration))
    when(store.list()).thenReturn(
        Lists.newArrayList(new BlobStoreConfiguration(name: 'test', path: temporaryFolder.root.absolutePath)))

    underTest.doStart()

    assert underTest.browse().toList() == [blobStore]
  }

  @Test
  public void 'Can create a BlobStore'() throws Exception {
    BlobStore blobStore = mock(BlobStore)
    doReturn(blobStore).when(underTest).newBlobStore(any(BlobStoreConfiguration))
    BlobStoreConfiguration configuration = new BlobStoreConfiguration(name: 'test', path: temporaryFolder.root.absolutePath)
    
    BlobStore createdBlobStore = underTest.create(configuration)

    assert createdBlobStore == blobStore
    verify(store).create(configuration)
    verify(blobStore).start()
    assert underTest.browse().toList() == [blobStore]
    assert underTest.get('test') == blobStore
  }

  @Test
  public void 'Can delete an existing BlobStore'() throws Exception {
    BlobStoreConfiguration configuration = new BlobStoreConfiguration(name: 'test',
        path: temporaryFolder.root.absolutePath)
    BlobStore blobStore = mock(BlobStore)
    doReturn(blobStore).when(underTest).blobStore('test')
    when(store.list()).thenReturn(Lists.newArrayList(configuration));
    
    underTest.delete(configuration.getName())
    
    verify(blobStore).stop();
    verify(store).delete(configuration);
  }
  
  @Test
  public void 'BlobStores will be eagerly created if not already configured'() {
    BlobStore blobStore = mock(BlobStore)
    doReturn(blobStore).when(underTest).newBlobStore(any(BlobStoreConfiguration))
    BlobStore autoCreatedBlobStore = underTest.get('test')
    
    verify(blobStore).start()
    verify(store).create(any(BlobStoreConfiguration))
    assert blobStore == autoCreatedBlobStore
  }
}
