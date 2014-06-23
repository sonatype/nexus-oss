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
package org.sonatype.nexus.componentmetadata.testsupport;

import java.util.Set;

import org.sonatype.nexus.componentmetadata.api.ComponentMetadataService;
import org.sonatype.nexus.componentmetadata.api.MetadataHandle;
import org.sonatype.nexus.componentmetadata.api.MetadataKeyIterable;
import org.sonatype.nexus.componentmetadata.api.MetadataStore;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import cucumber.api.java.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public abstract class AbstractComponentMetadataServiceTest
{
  protected ComponentMetadataService service;

  protected abstract ComponentMetadataService createTestInstance();

  protected abstract void destroyTestInstance(final ComponentMetadataService service);

  @Before
  public void setup() {
    service = createTestInstance();
  }

  @After
  public void teardown() {
    destroyTestInstance(service);
  }

  @Test
  public void registerAndGetSimpleType() {
    service.getTypeRegistry().registerType(GoodMetadataSimple.TYPE);
    assertThat(service.getTypeRegistry().getType(GoodMetadataSimple.class), is(GoodMetadataSimple.TYPE));
  }

  @Test
  public void registerAndGetComplexType() {
    service.getTypeRegistry().registerType(GoodMetadataComplex.TYPE);
    assertThat(service.getTypeRegistry().getType(GoodMetadataComplex.class), is(GoodMetadataComplex.TYPE));
  }

  @Test
  public void completeCrudAndFindScenario() {

    // Prepare store

    service.getTypeRegistry().registerType(GoodMetadataSimple.TYPE);
    MetadataStore<GoodMetadataSimple> store = service.getStore(GoodMetadataSimple.class);

    // Prepare data

    final String identityKey1 = "identityKey1";
    final String identityKey2 = "identityKey2";
    final String fooValue = "fooValue";

    GoodMetadataSimple metadata1 = new GoodMetadataSimple.Builder()
        .withIdentityKey(identityKey1)
        .withFoo(fooValue)
        .build();

    GoodMetadataSimple metadata2 = new GoodMetadataSimple.Builder()
        .withIdentityKey(identityKey2)
        .withFoo(fooValue)
        .build();

    // Store should start out empty

    assertEmpty(store);

    // Create two metadata records

    MetadataHandle<GoodMetadataSimple> handle1 = store.create(metadata1);
    MetadataHandle<GoodMetadataSimple> handle2 = store.create(metadata2);

    // Returned handles should hold a distinct key, a version, and the unchanged metadata

    assertThat(handle1, notNullValue());
    assertThat(handle2, notNullValue());
    assertThat(handle1.getKey(), not(isEmptyOrNullString()));
    assertThat(handle2.getKey(), not(isEmptyOrNullString()));
    assertThat(handle1, not(is(handle2)));
    assertThat(handle1.getVersion(), not(isEmptyOrNullString()));
    assertThat(handle2.getVersion(), not(isEmptyOrNullString()));
    assertThat(handle1.getMetadata(), is(metadata1));
    assertThat(handle2.getMetadata(), is(metadata2));

    // Retrieve both metadata records

    MetadataHandle<GoodMetadataSimple> retrievedHandle1 = store.retrieve(handle1.getKey());
    MetadataHandle<GoodMetadataSimple> retrievedHandle2 = store.retrieve(handle2.getKey());

    // Retrieved handles should match those that were provided at creation time

    assertThat(retrievedHandle1, is(handle1));
    assertThat(retrievedHandle2, is(handle2));

    // Should get one result for query on unique attribute

    GoodMetadataSimple.Builder filter1 = new GoodMetadataSimple.Builder().withIdentityKey(identityKey1);
    assertResultsMatchKeys(store.find(filter1, GoodMetadataSimple.INDEX_IDENTITYKEY), handle1.getKey());

    // Should get two results for query on non-unique attribute with same value

    GoodMetadataSimple.Builder filter2 = new GoodMetadataSimple.Builder().withFoo(fooValue);
    assertResultsMatchKeys(store.find(filter2, GoodMetadataSimple.INDEX_IDENTITYKEY), handle1.getKey(),
        handle2.getKey());

    // Update one record with new foo value

    GoodMetadataSimple metadata1Updated = new GoodMetadataSimple.Builder()
        .withIdentityKey(identityKey1)
        .withFoo("fooUpdated")
        .build();

    MetadataHandle<GoodMetadataSimple> handle1Updated = store.update(handle1.getKey(), handle1.getVersion(),
        metadata1Updated);

    // Returned handle's key should be same, version should be different, and metadata should match what was given

    assertThat(handle1Updated, notNullValue());
    assertThat(handle1Updated.getKey(), is(handle1.getKey()));
    assertThat(handle1Updated.getVersion(), not(is(handle1.getVersion())));
    assertThat(handle1Updated.getMetadata(), is(metadata1Updated));
  }

  private void assertResultsMatchKeys(MetadataKeyIterable resultKeys, String... keys) {
    Set<String> expectedKeys = Sets.newHashSet(keys);
    Set<String> actualKeys = Sets.newHashSet(Iterables.toArray(resultKeys, String.class));
    assertThat(actualKeys, is(expectedKeys));
  }

  private void assertEmpty(MetadataStore<GoodMetadataSimple> store) {
    assertThat(store.findFirst(new GoodMetadataSimple.Builder(), GoodMetadataSimple.INDEX_IDENTITYKEY), nullValue());
  }
}
