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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.MetadataBuilder;
import org.sonatype.nexus.componentmetadata.api.MetadataIndex;
import org.sonatype.nexus.componentmetadata.api.MetadataType;
import org.sonatype.nexus.componentmetadata.api.MetadataType.Attribute.Type;
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonDeserialize(builder = GoodMetadataComplex.Builder.class)
public class GoodMetadataComplex
    extends SecondaryMetadata
{
  public static final MetadataIndex INDEX_IDENTITYKEY = new MetadataIndex.Builder()
      .withColumns("identityKey")
      .unique()
      .build();

  public static final MetadataType<GoodMetadataComplex> TYPE = new MetadataType.Builder<GoodMetadataComplex>()
      .withName("goodattributes")
      .withMetadataClass(GoodMetadataComplex.class)
      .withIndexes(INDEX_IDENTITYKEY)
      .build();

  private final DateTime dateTimeAttribute;
  private final Long longAttribute;
  private final long longPrimitiveAttribute;
  private final Integer integerAttribute;
  private final int intPrimitiveAttribute;
  private final Type enumAttribute;
  private final Character characterAttribute;
  private final char charPrimitiveAttribute;
  private final Boolean booleanAttribute;
  private final boolean booleanPrimitiveAttribute;
  private final List<Object> listAttribute;
  private final Map<Object, Object> mapAttribute;

  private GoodMetadataComplex(String identityKey,
                              DateTime dateTimeAttribute,
                              Long longAttribute,
                              long longPrimitiveAttribute,
                              Integer integerAttribute,
                              int intPrimitiveAttribute,
                              Type enumAttribute,
                              Character characterAttribute,
                              char charPrimitiveAttribute,
                              Boolean booleanAttribute,
                              boolean booleanPrimitiveAttribute,
                              List<Object> listAttribute,
                              @Nullable Map<Object, Object> mapAttribute) {
    super(identityKey);
    this.dateTimeAttribute = checkNotNull(dateTimeAttribute);
    this.longAttribute = checkNotNull(longAttribute);
    this.longPrimitiveAttribute = longPrimitiveAttribute;
    this.integerAttribute = checkNotNull(integerAttribute);
    this.intPrimitiveAttribute = intPrimitiveAttribute;
    this.enumAttribute = checkNotNull(enumAttribute);
    this.characterAttribute = checkNotNull(characterAttribute);
    this.charPrimitiveAttribute = charPrimitiveAttribute;
    this.booleanAttribute = checkNotNull(booleanAttribute);
    this.booleanPrimitiveAttribute = booleanPrimitiveAttribute;
    this.listAttribute = checkNotNull(listAttribute);
    this.mapAttribute = mapAttribute;
  }

  public DateTime getDateTimeAttribute() { return dateTimeAttribute; }
  public Long getLongAttribute() { return longAttribute; }
  public long getLongPrimitiveAttribute() { return longPrimitiveAttribute; }
  public Integer getIntegerAttribute() { return integerAttribute; }
  public int getIntPrimitiveAttribute() { return intPrimitiveAttribute; }
  public Type getEnumAttribute() { return enumAttribute; }
  public Character getCharacterAttribute() { return characterAttribute; }
  public char getCharPrimitiveAttribute() { return charPrimitiveAttribute; }
  public Boolean isBooleanAttribute() { return booleanAttribute; }
  public boolean isBooleanPrimitiveAttribute() { return booleanPrimitiveAttribute; }
  public List<Object> getListAttribute() { return listAttribute; }
  public @Nullable Map<Object, Object> getMapAttribute() { return mapAttribute; }

  public static class Builder
      implements MetadataBuilder<GoodMetadataComplex>
  {
    private String identityKey;
    private DateTime dateTimeAttribute;
    private Long longAttribute;
    private long longPrimitiveAttribute;
    private Integer integerAttribute;
    private int intPrimitiveAttribute;
    private Type enumAttribute;
    private Character characterAttribute;
    private char charPrimitiveAttribute;
    private Boolean booleanAttribute;
    private boolean booleanPrimitiveAttribute;
    private List<Object> listAttribute;
    private Map<Object, Object> mapAttribute;

    public Builder withIdentityKey(String identityKey) { this.identityKey = identityKey; return this; }
    public Builder withDateTimeAttribute(DateTime dateTimeAttribute) { this.dateTimeAttribute = dateTimeAttribute; return this; }
    public Builder withLongAttribute(Long longAttribute) { this.longAttribute = longAttribute; return this; }
    public Builder withLongPrimitiveAttribute(Long longPrimitiveAttribute) { this.longPrimitiveAttribute = longPrimitiveAttribute; return this; }
    public Builder withIntegerAttribute(Integer integerAttribute) { this.integerAttribute = integerAttribute; return this; }
    public Builder withIntPrimitiveAttribute(int intPrimitiveAttribute) { this.intPrimitiveAttribute = intPrimitiveAttribute; return this; }
    public Builder withEnumAttribute(Type enumAttribute) { this.enumAttribute = enumAttribute; return this; }
    public Builder withCharacterAttribute(Character characterAttribute) { this.characterAttribute = characterAttribute; return this; }
    public Builder withCharPrimitiveAttribute(char charPrimitiveAttribute) { this.charPrimitiveAttribute = charPrimitiveAttribute; return this; }
    public Builder withBooleanAttribute(Boolean booleanAttribute) { this.booleanAttribute = booleanAttribute; return this; }
    public Builder withBooleanPrimitiveAttribute(boolean booleanPrimitiveAttribute) { this.booleanPrimitiveAttribute = booleanPrimitiveAttribute; return this; }
    public Builder withListAttribute(List<Object> listAttribute) { this.listAttribute = listAttribute; return this; }
    public Builder withMapAttribute(Map<Object, Object> mapAttribute) { this.mapAttribute = mapAttribute; return this; }

    @Override
    public GoodMetadataComplex build() {
      return new GoodMetadataComplex(
          identityKey,
          dateTimeAttribute,
          longAttribute,
          longPrimitiveAttribute,
          integerAttribute,
          intPrimitiveAttribute,
          enumAttribute,
          characterAttribute,
          charPrimitiveAttribute,
          booleanAttribute,
          booleanPrimitiveAttribute,
          listAttribute,
          mapAttribute);
    }
  }
}
