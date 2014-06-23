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
package org.sonatype.nexus.component.metadata.api;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.component.metadata.api.MetadataType.Attribute.Type;
import org.sonatype.nexus.component.metadata.testsupport.GoodMetadataComplex;
import org.sonatype.nexus.component.metadata.testsupport.GoodMetadataSimple;
import org.sonatype.nexus.component.metadata.testsupport.TestMetadataStore;
import org.sonatype.nexus.component.metadata.testsupport.TestMetadataTypeRegistry;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link AbstractMetadataStore}.
 */
public class MetadataStoreTest
    extends TestSupport
{
  private TestMetadataTypeRegistry testRegistry = new TestMetadataTypeRegistry();

  @Before
  public void setup() {
    testRegistry.registerType(GoodMetadataSimple.TYPE);
    testRegistry.registerType(GoodMetadataComplex.TYPE);
  }

  // ctor

  @Test(expected = NullPointerException.class)
  public void badRegistryNull() {
    getInstance(null, GoodMetadataSimple.class);
  }

  @Test(expected = NullPointerException.class)
  public void badClazzNull() {
    getInstance(testRegistry, null);
  }

  // find

  @Test(expected = NullPointerException.class)
  public void findBadFilterNull() {
    getInstance().find(null, GoodMetadataSimple.INDEX_IDENTITYKEY);
  }

  @Test(expected = NullPointerException.class)
  public void findBadIndexNull() {
    getInstance().find(new GoodMetadataSimple.Builder(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findBadIndexNotRegistered() {
    getInstance().find(new GoodMetadataSimple.Builder(), new MetadataIndex.Builder().withColumns("foo").build());
  }

  @Test
  public void findGoodIndexSupportedTypes() {
    TestMetadataStore<GoodMetadataComplex> store = new TestMetadataStore<>
        (testRegistry, GoodMetadataComplex.class);

    final String identityKeyValue = "a";
    final DateTime dateTimeValue = new DateTime();
    final Long longValue = 1L;
    final long longPrimitiveValue = 2L;
    final Integer integerValue = 3;
    final int intPrimitiveValue = 4;
    final Type enumValue = Type.INTEGER;
    final Character characterValue = 'b';
    final char charPrimitiveValue = 'c';
    final List<Object> listValue = Lists.newArrayList(new Object());
    final Map<Object, Object> mapValue = Maps.newHashMap();

    GoodMetadataComplex.Builder filter = new GoodMetadataComplex.Builder()
        .withIdentityKey(identityKeyValue)
        .withDateTimeAttribute(dateTimeValue)
        .withLongAttribute(longValue)
        .withLongPrimitiveAttribute(longPrimitiveValue)
        .withIntegerAttribute(integerValue)
        .withIntPrimitiveAttribute(intPrimitiveValue)
        .withEnumAttribute(enumValue)
        .withCharacterAttribute(characterValue)
        .withCharPrimitiveAttribute(charPrimitiveValue)
        .withBooleanAttribute(true)
        .withBooleanPrimitiveAttribute(true)
        .withListAttribute(listValue)
        .withMapAttribute(mapValue);

    store.find(filter, GoodMetadataComplex.INDEX_IDENTITYKEY);

    Map<String, Object> expectedFilter = Maps.newHashMap();
    expectedFilter.put("identityKey", identityKeyValue);
    expectedFilter.put("dateTimeAttribute", dateTimeValue);
    expectedFilter.put("longAttribute", longValue);
    expectedFilter.put("longPrimitiveAttribute", longPrimitiveValue);
    expectedFilter.put("integerAttribute", integerValue);
    expectedFilter.put("intPrimitiveAttribute", intPrimitiveValue);
    expectedFilter.put("enumAttribute", enumValue);
    expectedFilter.put("characterAttribute", characterValue);
    expectedFilter.put("charPrimitiveAttribute", charPrimitiveValue);
    expectedFilter.put("booleanAttribute", true);
    expectedFilter.put("booleanPrimitiveAttribute", true);
    expectedFilter.put("listAttribute", listValue);
    expectedFilter.put("mapAttribute", mapValue);

    assertThat(store.getLastFilter(), is(expectedFilter));
    assertThat(store.getLastIndex(), is(GoodMetadataComplex.INDEX_IDENTITYKEY));
  }

  // findFirst

  @Test(expected = NullPointerException.class)
  public void findFirstBadFilterNull() {
    getInstance().findFirst(null, GoodMetadataSimple.INDEX_IDENTITYKEY);
  }

  @Test(expected = NullPointerException.class)
  public void findFirstBadIndexNull() {
    getInstance().findFirst(new GoodMetadataSimple.Builder(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findFirstBadIndexNotRegistered() {
    getInstance().findFirst(new GoodMetadataSimple.Builder(), new MetadataIndex.Builder().withColumns("foo").build());
  }

  @Test
  public void findFirstGoodIndex() {
    assertThat(getInstance().findFirst(new GoodMetadataSimple.Builder(), GoodMetadataSimple.INDEX_IDENTITYKEY), is("first"));
  }

  // support

  private TestMetadataStore<GoodMetadataSimple> getInstance() {
    return getInstance(testRegistry, GoodMetadataSimple.class);
  }

  private static <T> TestMetadataStore<T> getInstance(MetadataTypeRegistry registry, Class<T> clazz) {
    return new TestMetadataStore<>(registry, clazz);
  }
}