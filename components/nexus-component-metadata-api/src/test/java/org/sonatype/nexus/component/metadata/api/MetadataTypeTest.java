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

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
    assertTrue(checkName("name").equals("name"));
  }

  private static String checkName(String name) {
    return new MetadataType.Builder()
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
  public void goodMetadataClass() {
    assertTrue(checkMetadataClass(GoodMetadata.class).equals(GoodMetadata.class));
  }

  private static <T> Class checkMetadataClass(Class<T> metadataClass) {
    return new MetadataType.Builder()
        .withName("name")
        .withMetadataClass(metadataClass)
        .build()
        .getMetadataClass();
  }
}