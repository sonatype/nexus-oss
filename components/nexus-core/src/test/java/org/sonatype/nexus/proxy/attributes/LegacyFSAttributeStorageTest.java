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

package org.sonatype.nexus.proxy.attributes;

import java.io.File;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.internal.DefaultAttributes;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.util.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * AttributeStorage implementation driven by XStream.
 *
 * @author cstamas
 */
public class LegacyFSAttributeStorageTest
    extends AbstractNexusTestEnvironment
{

  protected LegacyFSAttributeStorage attributeStorage;

  protected File proxyAttributesDirectory;

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    // copying files directly, need to make sure the test is clean each time when we run the tests against multiple providers
    File sourceAttributeDirectory = getTestFile("src/test/resources/nexus4660");
    proxyAttributesDirectory = getTestFile("target/nexus4660-" + System.currentTimeMillis());
    FileUtils.copyDirectoryStructure(sourceAttributeDirectory, proxyAttributesDirectory);

    ApplicationConfiguration applicationConfiguration = Mockito.mock(ApplicationConfiguration.class);
    Mockito.when(applicationConfiguration.getWorkingDirectory("proxy/attributes", false)).thenReturn(
        proxyAttributesDirectory);

    attributeStorage = new LegacyFSAttributeStorage(applicationConfiguration);

    attributeStorage.initializeWorkingDirectory();
  }

  protected RepositoryItemUid createUid(final String path) {
    final RepositoryItemUidLock fakeLock = new RepositoryItemUidLock()
    {
      @Override
      public void lock(final Action action) {
      }

      @Override
      public void unlock() {
      }

      @Override
      public boolean hasLocksHeld() {
        return false;
      }
    };
    final Repository repository = Mockito.mock(Repository.class);
    Mockito.when(repository.getId()).thenReturn("test");
    final RepositoryItemUid uid = Mockito.mock(RepositoryItemUid.class);
    Mockito.when(uid.getRepository()).thenReturn(repository);
    Mockito.when(uid.getPath()).thenReturn(path);
    Mockito.when(uid.getLock()).thenReturn(fakeLock);

    return uid;
  }

  @Test
  public void testGetAttributes() {
    final RepositoryItemUid uid = createUid("classworlds/classworlds/1.1/classworlds-1.1.pom");
    final Attributes attributes = attributeStorage.getAttributes(uid);
    // must be present
    MatcherAssert.assertThat(attributes, Matchers.notNullValue());
    // do some random checks that conent is okay
    MatcherAssert.assertThat(attributes.getGeneration(), Matchers.equalTo(4));
    MatcherAssert.assertThat(attributes.get(StorageFileItem.DIGEST_SHA1_KEY),
        Matchers.equalTo("4703c4199028094698c222c17afea6dcd9f04999"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPutAttributes() {
    final RepositoryItemUid uid = createUid("/some/path");
    attributeStorage.putAttributes(uid, new DefaultAttributes());
  }

  @Test
  public void testDeleteAttributes() {
    final String path = "classworlds/classworlds/1.1/classworlds-1.1.pom";
    final RepositoryItemUid uid = createUid(path);
    MatcherAssert.assertThat(attributeStorage.deleteAttributes(uid), Matchers.is(true));
    MatcherAssert.assertThat(new File(proxyAttributesDirectory, path).exists(), Matchers.is(false));
  }
}
