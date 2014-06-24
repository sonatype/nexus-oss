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

import org.sonatype.nexus.componentmetadata.api.MetadataBuilder;
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = BadBuilderNoCorrespondingAttribute.Builder.class)
public class BadBuilderNoCorrespondingAttribute
    extends SecondaryMetadata
{
  private final int foo;

  private BadBuilderNoCorrespondingAttribute(String identityKey, int foo) {
    super(identityKey);
    this.foo = foo;
  }

  public int getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<BadBuilderNoCorrespondingAttribute>
  {
    private String identityKey;
    private Integer bar;

    public Builder withIdentityKey(String identityKey) {
      this.identityKey = identityKey;
      return this;
    }

    public Builder withBar(Integer bar) {
      this.bar = bar;
      return this;
    }

    @Override
    public BadBuilderNoCorrespondingAttribute build() {
      return new BadBuilderNoCorrespondingAttribute(identityKey, bar);
    }
  }
}
