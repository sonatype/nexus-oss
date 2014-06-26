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
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = BadAttributesUnsupportedType.Builder.class)
public class BadAttributesUnsupportedType
    extends SecondaryMetadata
{
  private final Object foo;

  private BadAttributesUnsupportedType(String identityKey, @Nullable Object foo) {
    super(identityKey);
    this.foo = foo;
  }

  @Nullable
  public Object getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<BadAttributesUnsupportedType>
  {
    private String identityKey;
    private Object foo;

    public Builder withIdentityKey(String identityKey) {
      this.identityKey = identityKey;
      return this;
    }

    public Builder withFoo(Object foo) {
      this.foo = foo;
      return this;
    }

    @Override
    public BadAttributesUnsupportedType build() {
      return new BadAttributesUnsupportedType(identityKey, foo);
    }
  }
}
