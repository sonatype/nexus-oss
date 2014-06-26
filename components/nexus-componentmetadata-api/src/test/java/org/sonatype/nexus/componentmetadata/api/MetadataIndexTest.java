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

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link MetadataIndex}.
 */
public class MetadataIndexTest
    extends TestSupport
{
  @Test(expected = IllegalArgumentException.class)
  public void badColumnsEmpty() {
    checkColumns();
  }

  @Test(expected = IllegalArgumentException.class)
  public void badColumnsDuplicate() {
    checkColumns("foo", "foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void badColumnsIllegalName() {
    checkColumns(" ");
  }

  @Test
  public void goodColumns() {
    List<String> result = checkColumns("foo", "bar");
    assertThat(checkColumns("foo", "bar"), Matchers.<List<String>>is(Lists.newArrayList("foo", "bar")));
  }

  @Test
  public void isUnique() {
    assertThat(new MetadataIndex.Builder().withColumns("foo").build().isUnique(), is(false));
    assertThat(new MetadataIndex.Builder().withColumns("foo").unique().build().isUnique(), is(true));
  }

  @Test
  public void isDescending() {
    assertThat(new MetadataIndex.Builder().withColumns("foo").build().isDescending(), is(false));
    assertThat(new MetadataIndex.Builder().withColumns("foo").descending().build().isDescending(), is(true));
  }

  private static List<String> checkColumns(String... columns) {
    return new MetadataIndex.Builder()
        .withColumns(columns)
        .build()
        .getColumns();
  }
}
