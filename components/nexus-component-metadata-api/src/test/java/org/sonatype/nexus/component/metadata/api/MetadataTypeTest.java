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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.component.metadata.testsupport.BadAttributesUnsupportedType;
import org.sonatype.nexus.component.metadata.testsupport.BadMetadataJsonDeserializeBuilderNotBuilder;
import org.sonatype.nexus.component.metadata.testsupport.BadMetadataJsonDeserializeBuilderNotSpecified;
import org.sonatype.nexus.component.metadata.testsupport.BadMetadataJsonDeserializeNotPresent;
import org.sonatype.nexus.component.metadata.testsupport.BadMetadataNotSecondarySubclass;
import org.sonatype.nexus.component.metadata.testsupport.GoodAttributesSupportedTypes;
import org.sonatype.nexus.component.metadata.testsupport.GoodMetadata;
import org.sonatype.nexus.component.metadata.testsupport.GoodMetadata.Builder;
import org.sonatype.nexus.component.metadata.api.MetadataType.Attribute;
import org.sonatype.nexus.component.metadata.api.MetadataType.Attribute.Type;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests for {@link MetadataType}.
 */
public class MetadataTypeTest
    extends TestSupport
{
  @Test(expected = NullPointerException.class)
  public void badNameNull() {
    checkName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badNameEmpty() {
    checkName("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void badNameIllegal() {
    checkName(" ");
  }

  @Test
  public void goodName() {
    assertThat(checkName("name"), is("name"));
  }

  private static String checkName(String name) {
    return new MetadataType.Builder<GoodMetadata>()
        .withName(name)
        .withMetadataClass(GoodMetadata.class)
        .build()
        .getName();
  }

  @Test(expected = NullPointerException.class)
  public void badMetadataClassNull() {
    checkMetadataClass(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badMetadataClassNotSecondarySubclass() {
    checkMetadataClass(BadMetadataNotSecondarySubclass.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badMetadataClassJsonDeserializeNotPresent() {
    checkMetadataClass(BadMetadataJsonDeserializeNotPresent.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badMetadataClassJsonDeserializeBuilderNotSpecified() {
    checkMetadataClass(BadMetadataJsonDeserializeBuilderNotSpecified.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badMetadataClassJsonDeserializeBuilderNotBuilder() {
    checkMetadataClass(BadMetadataJsonDeserializeBuilderNotBuilder.class);
  }

  @Test
  public void goodMetadataClassIdentity() {
    assertThat(checkMetadataClass(IdentityMetadata.class), Matchers.<Class>is(IdentityMetadata.class));
  }

  @Test
  public void goodMetadataClassSecondarySubclass() {
    assertThat(checkMetadataClass(GoodMetadata.class), Matchers.<Class>is(GoodMetadata.class));
  }

  private static <T> Class<T> checkMetadataClass(Class<T> metadataClass) {
    return new MetadataType.Builder<T>()
        .withName("name")
        .withMetadataClass(metadataClass)
        .build()
        .getMetadataClass();
  }

  @Test(expected = IllegalArgumentException.class)
  public void badTextAttributesNonExisting() {
    checkTextAttributes("nonExisting");
  }

  @Test(expected = IllegalArgumentException.class)
  public void badTextAttributesNonTextCompatible() {
    checkTextAttributesSupportedTypes("integerAttribute");
  }

  @Test
  public void goodTextAttributesNone() {
    checkGoodTextAttributes(false);
  }

  @Test
  public void goodTextAttributesOne() {
    checkGoodTextAttributes(true);
  }

  private static void checkGoodTextAttributes(boolean one) {
    Map<String, Attribute> expectedMap = new HashMap<>();
    Attribute identityKeyAttribute = new MetadataType.Attribute(
        "identityKey",
        Type.SMALLSTRING,
        false);
    Attribute fooAttribute = new MetadataType.Attribute(
        "foo",
        one ? Type.TEXT : Type.SMALLSTRING,
        true);
    expectedMap.put("identityKey", identityKeyAttribute);
    expectedMap.put("foo", fooAttribute);

    String[] textAttributes = one ? new String[] { "foo" } : new String[0];

    assertThat(checkTextAttributes(textAttributes), is(expectedMap));
  }

  private static Map<String, Attribute> checkTextAttributes(String... textAttributes) {
    return new MetadataType.Builder<GoodMetadata>()
        .withName("name")
        .withMetadataClass(GoodMetadata.class)
        .withTextAttributes(textAttributes)
        .build()
        .getAttributes();
  }

  private static Map<String, Attribute> checkTextAttributesSupportedTypes(String... textAttributes) {
    return new MetadataType.Builder<GoodAttributesSupportedTypes>()
        .withName("name")
        .withMetadataClass(GoodAttributesSupportedTypes.class)
        .withTextAttributes(textAttributes)
        .build()
        .getAttributes();
  }

  @Test(expected = IllegalArgumentException.class)
  public void badAttributesUnsupportedType() {
    checkMetadataClass(BadAttributesUnsupportedType.class);
  }

  @Test
  public void goodAttributesSupportedTypes() {
    Map<String, Attribute> result = checkAttributes(GoodAttributesSupportedTypes.class);

    checkAttribute(result, "identityKey", Type.SMALLSTRING, false);
    checkAttribute(result, "dateTimeAttribute", Type.DATE, false);
    checkAttribute(result, "longAttribute", Type.LONG, false);
    checkAttribute(result, "longPrimitiveAttribute", Type.LONG, false);
    checkAttribute(result, "integerAttribute", Type.INTEGER, false);
    checkAttribute(result, "intPrimitiveAttribute", Type.INTEGER, false);
    checkAttribute(result, "enumAttribute", Type.ENUM, false);
    checkAttribute(result, "characterAttribute", Type.CHAR, false);
    checkAttribute(result, "charPrimitiveAttribute", Type.CHAR, false);
    checkAttribute(result, "booleanAttribute", Type.BOOLEAN, false);
    checkAttribute(result, "booleanPrimitiveAttribute", Type.BOOLEAN, false);
    checkAttribute(result, "listAttribute", Type.ARRAY, false);
    checkAttribute(result, "mapAttribute", Type.MAP, true);
  }

  private static void checkAttribute(Map<String, Attribute> attributes, String name, Type type, boolean nullable) {
    Attribute attribute = attributes.get(name);
    assertThat(attribute, notNullValue());
    assertThat(attribute.getType(), is(type));
    assertThat(attribute.getName(), is(name));
    assertThat(attribute.isNullable(), is(nullable));
  }

  private static <T> Map<String, Attribute> checkAttributes(Class<T> metadataClass) {
    return new MetadataType.Builder<T>()
        .withName("name")
        .withMetadataClass(metadataClass)
        .build()
        .getAttributes();
  }

  @Test(expected = IllegalArgumentException.class)
  public void badIndexesNonExisting() {
    checkIndexes(new MetadataIndex.Builder()
        .withColumns("nonExisting")
        .build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void badIndexesNotIndexableList() {
    checkIndexes(new MetadataIndex.Builder()
        .withColumns("listAttribute")
        .build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void badIndexesNotIndexableMap() {
    checkIndexes(new MetadataIndex.Builder()
        .withColumns("mapAttribute")
        .build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void badIndexesNotIndexableText() {
    checkIndexes(new MetadataIndex.Builder()
        .withColumns("identityKey")
        .build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void badIndexesMultipleUnique() {
    checkIndexes(
        new MetadataIndex.Builder()
            .withColumns("integerAttribute")
            .unique()
            .build(),
        new MetadataIndex.Builder()
            .withColumns("intPrimitiveAttribute")
            .unique()
            .build());
  }

  @Test
  public void goodIndexesSingleSimple() {
    MetadataIndex index = new MetadataIndex.Builder()
        .withColumns("integerAttribute")
        .build();
    Set<MetadataIndex> expected = Collections.singleton(index);

    assertThat(checkIndexes(index), is(expected));

    MetadataIndex resultIndex = Iterables.getOnlyElement(checkIndexes(index));
    assertThat(resultIndex.isUnique(), is(false));
    assertThat(resultIndex.isDescending(), is(false));
  }

  @Test
  public void goodIndexesSingleSimpleUniqueDescending() {
    MetadataIndex index = new MetadataIndex.Builder()
        .withColumns("integerAttribute")
        .unique()
        .descending()
        .build();
    Set<MetadataIndex> expected = Collections.singleton(index);

    assertThat(checkIndexes(index), is(expected));

    MetadataIndex resultIndex = Iterables.getOnlyElement(checkIndexes(index));
    assertThat(resultIndex.isUnique(), is(true));
    assertThat(resultIndex.isDescending(), is(true));
  }

  @Test
  public void goodIndexesSingleCompound() {
    MetadataIndex index = new MetadataIndex.Builder()
        .withColumns("integerAttribute", "intPrimitiveAttribute")
        .build();
    Set<MetadataIndex> expected = Sets.newHashSet(index);

    assertThat(checkIndexes(index), is(expected));
  }

  @Test
  public void goodIndexesMultiple() {
    MetadataIndex index1 = new MetadataIndex.Builder()
        .withColumns("integerAttribute")
        .build();
    MetadataIndex index2 = new MetadataIndex.Builder()
        .withColumns("intPrimitiveAttribute")
        .build();
    Set<MetadataIndex> expected = Sets.newHashSet(index1, index2);

    assertThat(checkIndexes(index1, index2), is(expected));
  }

  private static Set<MetadataIndex> checkIndexes(MetadataIndex... indexes) {
    return new MetadataType.Builder<GoodAttributesSupportedTypes>()
        .withName("name")
        .withTextAttributes("identityKey")
        .withMetadataClass(GoodAttributesSupportedTypes.class)
        .withIndexes(indexes)
        .build()
        .getIndexes();
  }

  @Test(expected = IllegalArgumentException.class)
  public void getIndexNameBadNonExisting() {
    MetadataIndex index = new MetadataIndex.Builder()
        .withColumns("identityKey")
        .build();

    MetadataType type = new MetadataType.Builder<GoodMetadata>()
        .withName("name")
        .withMetadataClass(GoodMetadata.class)
        .build();

    type.getIndexName(index);
  }

  @Test
  public void getIndexNameGood() {
    MetadataIndex index = new MetadataIndex.Builder()
        .withColumns("identityKey")
        .build();

    MetadataType type = new MetadataType.Builder<GoodMetadata>()
        .withName("name")
        .withMetadataClass(GoodMetadata.class)
        .withIndexes(index)
        .build();

    assertThat(type.getIndexName(index), is("name-identityKey"));
  }

  @Test
  public void getMetadataBuilderClass() {
    MetadataType type = new MetadataType.Builder<GoodMetadata>()
        .withName("name")
        .withMetadataClass(GoodMetadata.class)
        .build();

    assertThat(type.getMetadataBuilderClass(), Matchers.<Class>is(Builder.class));
  }
}