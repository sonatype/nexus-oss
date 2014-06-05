/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.timeline.feeds.subscribers;

import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.internal.DefaultAttributes;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenArtifactSignatureAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenChecksumAttribute;
import org.sonatype.nexus.proxy.maven.uid.IsMavenRepositoryMetadataAttribute;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ItemSubscriberTest
    extends TestSupport
{

  @Mock
  private FeedRecorder feedRecorder;

  @Mock
  private Repository repository;

  @Mock
  private StorageFileItem storageFileItem;

  @Mock
  private RepositoryItemUid repositoryItemUid;

  @Before
  public void setup() {
    when(repository.getId()).thenReturn("test");
    when(repository.getName()).thenReturn("test");
    when(storageFileItem.getItemContext()).thenReturn(new RequestContext());
    when(storageFileItem.getRepositoryItemUid()).thenReturn(repositoryItemUid);
    when(storageFileItem.getRepositoryItemAttributes()).thenReturn(new DefaultAttributes());
    when(storageFileItem.getResourceStoreRequest()).thenReturn(new ResourceStoreRequest("blah"));
    when(repositoryItemUid.getRepository()).thenReturn(repository);
  }

  @Test
  public void eventsOnHiddenFilesAreNotRecorded() {
    final ItemSubscriber underTest =
        new ItemSubscriber(feedRecorder);
    final RepositoryItemEventStoreCreate evt = new RepositoryItemEventStoreCreate(repository, storageFileItem);
    when(storageFileItem.getPath()).thenReturn("/some/path");
    when(storageFileItem.getRepositoryId()).thenReturn("central");
    when(storageFileItem.getRemoteUrl()).thenReturn("http://repo1.maven.org/maven2");
    when(repositoryItemUid.getBooleanAttributeValue(IsHiddenAttribute.class)).thenReturn(true);
    underTest.on(evt);

    verifyNoMoreInteractions(feedRecorder);
  }

  @Test
  public void eventsOnMavenMetadataSignatureAndHashFilesShouldNotBeRecorded() {
    final ItemSubscriber underTest =
        new ItemSubscriber(feedRecorder);
    when(storageFileItem.getPath()).thenReturn("/some/path");
    when(storageFileItem.getRepositoryId()).thenReturn("central");
    when(storageFileItem.getRemoteUrl()).thenReturn("http://repo1.maven.org/maven2");
    {
      final RepositoryItemEventStoreCreate evt = new RepositoryItemEventStoreCreate(repository, storageFileItem);
      when(repositoryItemUid.getBooleanAttributeValue(IsMavenRepositoryMetadataAttribute.class)).thenReturn(
          true);
      underTest.on(evt);
    }
    {
      final RepositoryItemEventStoreCreate evt = new RepositoryItemEventStoreCreate(repository, storageFileItem);
      when(repositoryItemUid.getBooleanAttributeValue(IsMavenArtifactSignatureAttribute.class)).thenReturn(
          true);
      underTest.on(evt);
    }
    {
      final RepositoryItemEventStoreCreate evt = new RepositoryItemEventStoreCreate(repository, storageFileItem);
      when(repositoryItemUid.getBooleanAttributeValue(IsMavenChecksumAttribute.class)).thenReturn(true);
      underTest.on(evt);
    }

    // these events above should be filtered out by ItemChangesFeedEventInspector, feedRecordes shall be untouched
    verifyNoMoreInteractions(feedRecorder);

    // now do touch it (with event that has all the flags we added false)
    final RepositoryItemEventStoreCreate evt = new RepositoryItemEventStoreCreate(repository, storageFileItem);
    when(repositoryItemUid.getBooleanAttributeValue(IsMavenRepositoryMetadataAttribute.class)).thenReturn(
        false);
    when(repositoryItemUid.getBooleanAttributeValue(IsMavenArtifactSignatureAttribute.class)).thenReturn(false);
    when(repositoryItemUid.getBooleanAttributeValue(IsMavenChecksumAttribute.class)).thenReturn(false);
    underTest.on(evt);
    // method touched only once
    verify(feedRecorder, times(1)).addEvent(any(FeedEvent.class));
  }
}
