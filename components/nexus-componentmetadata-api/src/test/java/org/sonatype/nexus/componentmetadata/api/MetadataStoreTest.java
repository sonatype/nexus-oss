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
package org.sonatype.nexus.componentmetadata.api;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.componentmetadata.api.MetadataType.Attribute.Type;
import org.sonatype.nexus.componentmetadata.testsupport.GoodMetadataComplex;
import org.sonatype.nexus.componentmetadata.testsupport.GoodMetadataSimple;
import org.sonatype.nexus.componentmetadata.testsupport.TestMetadataStore;
import org.sonatype.nexus.componentmetadata.testsupport.TestMetadataTypeRegistry;
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
    getInstanceSimple().find(null, GoodMetadataSimple.INDEX_IDENTITYKEY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findBadFilterAttributeNotIndexed() {
    getInstanceSimple().find(new GoodMetadataSimple.Builder()
        .withFoo("thisValueIsNotIndexed"), GoodMetadataSimple.INDEX_IDENTITYKEY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findBadFilterFirstIndexColumnNotConstrained() {
    testRegistry.registerType(GoodMetadataComplex.TYPE2);
    getInstanceComplex().find(new GoodMetadataComplex.Builder()
            .withEnumAttribute(Type.INTEGER),
        GoodMetadataComplex.INDEX_IDENTITYKEY_ENUMATTRIBUTE_LONGATTRIBUTE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findBadFilterAllUniqueIndexColumnsNotConstrained() {
    testRegistry.registerType(GoodMetadataComplex.TYPE2);
    getInstanceComplex().find(new GoodMetadataComplex.Builder()
        .withIdentityKey("identityKey")
        .withLongAttribute(1L),
        GoodMetadataComplex.INDEX_IDENTITYKEY_ENUMATTRIBUTE_LONGATTRIBUTE);
  }

  @Test(expected = NullPointerException.class)
  public void findBadIndexNull() {
    getInstanceSimple().find(new GoodMetadataSimple.Builder(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findBadIndexNotRegistered() {
    getInstanceSimple().find(new GoodMetadataSimple.Builder(), new MetadataIndex.Builder().withColumns("foo").build());
  }

  @Test
  public void findGoodIndexSupportedTypes() {
    MetadataIndex testIndex = new MetadataIndex.Builder()
        .withColumns("identityKey", "dateTimeAttribute", "longAttribute", "longPrimitiveAttribute",
            "integerAttribute", "intPrimitiveAttribute", "enumAttribute", "characterAttribute",
            "charPrimitiveAttribute", "booleanAttribute", "booleanPrimitiveAttribute")
        .build();
    testRegistry.registerType(new MetadataType.Builder<GoodMetadataComplex>()
        .withName("complex")
        .withMetadataClass(GoodMetadataComplex.class)
        .withIndexes(testIndex)
        .build());
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
        .withBooleanPrimitiveAttribute(true);

    store.find(filter, testIndex);

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

    assertThat(store.getLastFilter(), is(expectedFilter));
    assertThat(store.getLastIndex(), is(testIndex));
  }

  // findFirst

  @Test(expected = NullPointerException.class)
  public void findFirstBadFilterNull() {
    getInstanceSimple().findFirst(null, GoodMetadataSimple.INDEX_IDENTITYKEY);
  }

  @Test(expected = NullPointerException.class)
  public void findFirstBadIndexNull() {
    getInstanceSimple().findFirst(new GoodMetadataSimple.Builder(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findFirstBadIndexNotRegistered() {
    getInstanceSimple().findFirst(new GoodMetadataSimple.Builder(),
        new MetadataIndex.Builder().withColumns("foo").build());
  }

  @Test
  public void findFirstGoodIndex() {
    assertThat(getInstanceSimple().findFirst(new GoodMetadataSimple.Builder().withIdentityKey("identityKey"),
        GoodMetadataSimple.INDEX_IDENTITYKEY), is("first"));
  }

  // support

  private TestMetadataStore<GoodMetadataSimple> getInstanceSimple() {
    return getInstance(testRegistry, GoodMetadataSimple.class);
  }

  private TestMetadataStore<GoodMetadataComplex> getInstanceComplex() {
    return getInstance(testRegistry, GoodMetadataComplex.class);
  }

  private static <T> TestMetadataStore<T> getInstance(MetadataTypeRegistry registry, Class<T> clazz) {
    return new TestMetadataStore<>(registry, clazz);
  }
}
