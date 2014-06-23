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

import org.sonatype.nexus.component.metadata.testsupport.GoodMetadataComplex;
import org.sonatype.nexus.component.metadata.testsupport.GoodMetadataSimple;
import org.sonatype.nexus.component.metadata.testsupport.TestMetadataTypeRegistry;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link MetadataTypeRegistry}.
 */
public class MetadataTypeRegistryTest
    extends TestSupport
{
  private TestMetadataTypeRegistry instance = new TestMetadataTypeRegistry();

  private MetadataType<GoodMetadataSimple> goodMetadataType = new MetadataType.Builder<GoodMetadataSimple>()
          .withName("goodName")
          .withMetadataClass(GoodMetadataSimple.class)
          .build();

  @Test(expected = NullPointerException.class)
  public void registerTypeBadNull() {
    checkRegisterType(null);
  }

  @Test(expected = IllegalStateException.class)
  public void registerTypeBadClassAlreadyBound() {
    checkRegisterType(goodMetadataType);
    checkRegisterType(new MetadataType.Builder<GoodMetadataSimple>()
        .withName("uniqueName")
        .withMetadataClass(GoodMetadataSimple.class)
        .build());
  }

  @Test(expected = IllegalStateException.class)
  public void registerTypeBadNameAlreadyBound() {
    checkRegisterType(goodMetadataType);
    checkRegisterType(new MetadataType.Builder<GoodMetadataComplex>()
        .withName("goodName")
        .withMetadataClass(GoodMetadataComplex.class)
        .build());
  }

  @Test
  public void registerTypeGood() {
    checkRegisterType(goodMetadataType);
  }

  private void checkRegisterType(MetadataType type) {
    instance.registerType(type);
    assertThat(instance.getType(type.getMetadataClass()), is(type));
    assertThat(Iterables.getOnlyElement(instance), is(type));
    assertThat(instance.getLastType(), is(type));
  }

  @Test(expected = NullPointerException.class)
  public void getTypeBadNull() {
    instance.getType(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getTypeBadNotRegistered() {
    instance.getType(GoodMetadataSimple.class);
  }

  @Test
  public void getTypeGood() {
    instance.registerType(goodMetadataType);
    assertThat(instance.getType(GoodMetadataSimple.class), is(goodMetadataType));
  }
}