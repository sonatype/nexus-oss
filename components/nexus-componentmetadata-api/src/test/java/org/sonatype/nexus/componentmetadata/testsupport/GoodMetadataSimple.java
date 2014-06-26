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

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.MetadataBuilder;
import org.sonatype.nexus.componentmetadata.api.MetadataIndex;
import org.sonatype.nexus.componentmetadata.api.MetadataType;
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

@JsonDeserialize(builder = GoodMetadataSimple.Builder.class)
public class GoodMetadataSimple
    extends SecondaryMetadata
{
  public static final MetadataIndex INDEX_IDENTITYKEY = new MetadataIndex.Builder()
      .withColumns("identityKey")
      .unique()
      .build();

  public static final MetadataIndex INDEX_FOO = new MetadataIndex.Builder()
      .withColumns("foo")
      .build();

  public static final MetadataType<GoodMetadataSimple> TYPE = new MetadataType.Builder<GoodMetadataSimple>()
      .withName("good")
      .withMetadataClass(GoodMetadataSimple.class)
      .withIndexes(INDEX_IDENTITYKEY, INDEX_FOO)
      .build();

  private final String foo;

  private GoodMetadataSimple(String identityKey, @Nullable String foo) {
    super(identityKey);
    this.foo = foo;
  }

  @Nullable
  public String getFoo() {
    return foo;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final GoodMetadataSimple other = (GoodMetadataSimple) obj;
    return Objects.equal(identityKey, other.identityKey)
        && Objects.equal(foo, other.foo);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(identityKey, foo);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("identityKey", identityKey)
        .add("foo", foo)
        .toString();
  }

  public static class Builder
      implements MetadataBuilder<GoodMetadataSimple>
  {
    private String identityKey;
    private String foo;

    public Builder withIdentityKey(String identityKey) {
      this.identityKey = identityKey;
      return this;
    }

    public Builder withFoo(String foo) {
      this.foo = foo;
      return this;
    }

    @Override
    public GoodMetadataSimple build() {
      return new GoodMetadataSimple(identityKey, foo);
    }
  }
}
