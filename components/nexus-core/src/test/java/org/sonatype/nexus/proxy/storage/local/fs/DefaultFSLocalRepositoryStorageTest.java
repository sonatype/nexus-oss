/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.storage.local.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link DefaultFSLocalRepositoryStorage}
 *
 * @since 2.0
 */
public class DefaultFSLocalRepositoryStorageTest
    extends PlexusTestCaseSupport
{

  /**
   * Tests listing a directory, when a contained file does NOT exists.
   */
  @SuppressWarnings({"unchecked"})
  @Test
  public void testListFilesThrowsItemNotFoundException() throws Exception {

    File repoLocation = new File(getBasedir(), "target/" + getClass().getSimpleName() + "/repo/");

    // the contents of the "valid" directory, only contains a "valid.txt" file
    File validDir = new File(repoLocation, "valid/");
    validDir.mkdirs();
    FileUtils.fileWrite(new File(validDir, "valid.txt"), "UTF-8", "something valid");
    Collection<File> validFileCollection = Arrays.asList(validDir.listFiles());

    // the contents of the "invalid" directory, this dir contains a missing file
    File invalidDir = new File(repoLocation, "invalid/");
    invalidDir.mkdirs();
    FileUtils.fileWrite(new File(invalidDir, "invalid.txt"), "UTF-8", "something valid");
    List<File> invalidFileCollection = new ArrayList<File>(Arrays.asList(invalidDir.listFiles()));
    invalidFileCollection.add(new File(invalidDir, "missing.txt"));

    // Mocks
    Wastebasket wastebasket = mock(Wastebasket.class);
    LinkPersister linkPersister = mock(LinkPersister.class);
    MimeSupport mimeUtil = mock(MimeSupport.class);
    when(mimeUtil.guessMimeTypeFromPath(Mockito.any(MimeRulesSource.class), Mockito.anyString())).thenReturn(
        "text/plain");

    // Mock FSPeer to return the results created above
    FSPeer fsPeer = mock(FSPeer.class);
    when(fsPeer
        .listItems(Mockito.any(Repository.class), Mockito.any(File.class), Mockito.any(ResourceStoreRequest.class),
            eq(validDir))).thenReturn(validFileCollection);
    when(fsPeer
        .listItems(Mockito.any(Repository.class), Mockito.any(File.class), Mockito.any(ResourceStoreRequest.class),
            eq(new File(repoLocation, "invalid/")))).thenReturn(invalidFileCollection);

    // create Repository Mock
    Repository repository = mock(Repository.class);
    when(repository.getId()).thenReturn("mock");
    when(repository.getRepositoryKind()).thenReturn(new DefaultRepositoryKind(HostedRepository.class, null));
    when(repository.getLocalUrl()).thenReturn(repoLocation.toURI().toURL().toString());
    AttributesHandler attributesHandler = mock(AttributesHandler.class);
    when(repository.getAttributesHandler()).thenReturn(attributesHandler);


    DefaultFSLocalRepositoryStorage localRepositoryStorageUnderTest = new DefaultFSLocalRepositoryStorage(wastebasket,
        linkPersister, mimeUtil, fsPeer);

    ResourceStoreRequest validRequest = new ResourceStoreRequest("valid");

    // positive test, valid.txt should be found
    Collection<StorageItem> items = localRepositoryStorageUnderTest.listItems(repository, validRequest);
    assertThat(items.iterator().next().getName(), equalTo("valid.txt"));
    assertThat(items, hasSize(1));


    // missing.txt was listed in this directory, but it does NOT exist, only invalid.txt should be found
    ResourceStoreRequest invalidRequest = new ResourceStoreRequest("invalid");
    items = localRepositoryStorageUnderTest.listItems(repository, invalidRequest);
    assertThat(items.iterator().next().getName(), equalTo("invalid.txt"));
    assertThat(items, hasSize(1));
  }

  /**
   * Expects an already deleted file to thrown an ItemNotFoundException. More specifically if a file was deleted
   * after the call to file.exists() was called.
   */
  @Test(expected = ItemNotFoundException.class)
  public void testRetrieveItemFromFileThrowsItemNotFoundExceptionForDeletedFile()
      throws Exception
  {
    // Mocks
    Wastebasket wastebasket = mock(Wastebasket.class);
    Repository repository = mock(Repository.class);
    FSPeer fsPeer = mock(FSPeer.class);
    MimeSupport mimeSupport = mock(MimeSupport.class);

    // mock file
    File mockFile = mock(File.class);
    when(mockFile.isDirectory()).thenReturn(false);
    when(mockFile.isFile()).thenReturn(true);
    when(mockFile.exists()).thenReturn(true);


    // needs to throw a FileNotFound when _opening_ the file
    LinkPersister linkPersister = mock(LinkPersister.class);
    when(linkPersister.isLinkContent(Mockito.any(ContentLocator.class)))
        .thenThrow(new FileNotFoundException("Expected to be thrown from mock."));

    // object to test
    DefaultFSLocalRepositoryStorage localRepositoryStorageUnderTest = new DefaultFSLocalRepositoryStorage(wastebasket,
        linkPersister, mimeSupport, fsPeer)
    {
      // expose protected method
      @Override
      public AbstractStorageItem retrieveItemFromFile(Repository repository, ResourceStoreRequest request,
                                                      File target)
          throws ItemNotFoundException, LocalStorageException
      {
        return super.retrieveItemFromFile(repository, request, target);
      }
    };

    // expected to throw a ItemNotFoundException
    localRepositoryStorageUnderTest.retrieveItemFromFile(repository, new ResourceStoreRequest("not-used"), mockFile);
  }
}
