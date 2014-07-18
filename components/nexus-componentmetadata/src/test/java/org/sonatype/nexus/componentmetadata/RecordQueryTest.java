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
package org.sonatype.nexus.componentmetadata;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class RecordQueryTest
    extends TestSupport
{
  private final RecordType goodType = new RecordType("Test");

  private final String goodKey = "key";

  private final String goodValue = "value";

  private final int goodSkip = 1;

  private final RecordId goodSkipRecord = new RecordId("Test", "skipRecord");

  private final String goodOrderBy = "orderBy";

  private final int goodLimit = 2;

  // ctor/getType

  @Test(expected = NullPointerException.class)
  public void typeBadNull() {
    new RecordQuery(null);
  }

  @Test
  public void typeGood() {
    assertThat(new RecordQuery(goodType).getType(), is(goodType));
  }

  // withEqual/getFilter

  @Test(expected = NullPointerException.class)
  public void equalBadKeyNull() {
    new RecordQuery(goodType).withEqual(null, goodValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void equalBadKeyEmpty() {
    new RecordQuery(goodType).withEqual("", goodValue);
  }

  @Test(expected = NullPointerException.class)
  public void equalBadValueNull() {
    new RecordQuery(goodType).withEqual(goodKey, null);
  }

  @Test
  public void equalGood() {
    RecordQuery query = new RecordQuery(goodType);
    assertThat(query.getFilter().size(), is(0));

    query.withEqual(goodKey, goodValue);
    assertThat(query.getFilter().size(), is(1));
    assertThat(query.getFilter().get(goodKey), is((Object) goodValue));
  }

  // withSkip/getSkip

  @Test(expected = NullPointerException.class)
  public void skipBadNull() {
    new RecordQuery(goodType).withSkip(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void skipBadNegative() {
    new RecordQuery(goodType).withSkip(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void skipBadSkipRecordSpecified() {
    new RecordQuery(goodType).withSkipRecord(goodSkipRecord).withSkip(goodSkip);
  }

  @Test
  public void skipGood() {
    assertThat(new RecordQuery(goodType).getSkip(), nullValue());
    assertThat(new RecordQuery(goodType).withSkip(goodSkip).getSkip(), is(goodSkip));
  }

  // withSkipRecord/getSkipRecord

  @Test(expected = NullPointerException.class)
  public void skipRecordBadNull() {
    new RecordQuery(goodType).withSkipRecord(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void skipRecordBadSkipSpecified() {
    new RecordQuery(goodType).withSkip(goodSkip).withSkipRecord(goodSkipRecord);
  }

  @Test(expected = IllegalArgumentException.class)
  public void skipRecordBadOrderBySpecified() {
    new RecordQuery(goodType).withOrderBy(goodOrderBy).withSkipRecord(goodSkipRecord);
  }

  @Test
  public void skipRecordGood() {
    assertThat(new RecordQuery(goodType).getSkipRecord(), is(nullValue()));
    assertThat(new RecordQuery(goodType).withSkipRecord(goodSkipRecord).getSkipRecord(), is(goodSkipRecord));
  }

  // withOrderBy/getOrderBy

  @Test(expected = NullPointerException.class)
  public void orderByBadNull() {
    new RecordQuery(goodType).withOrderBy(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void orderByBadEmpty() {
    new RecordQuery(goodType).withOrderBy("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void orderByBadSkipRecordSpecified() {
    new RecordQuery(goodType).withSkipRecord(goodSkipRecord).withOrderBy(goodOrderBy);
  }

  @Test
  public void orderByGood() {
    assertThat(new RecordQuery(goodType).getOrderBy(), is(nullValue()));
    assertThat(new RecordQuery(goodType).withOrderBy(goodOrderBy).getOrderBy(), is(goodOrderBy));
  }

  // withLimit/getLimit

  @Test(expected = NullPointerException.class)
  public void limitBadNull() {
    new RecordQuery(goodType).withLimit(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void limitBadNonPositive() {
    new RecordQuery(goodType).withLimit(0);
  }

  @Test
  public void limitGood() {
    assertThat(new RecordQuery(goodType).getLimit(), is(nullValue()));
    assertThat(new RecordQuery(goodType).withLimit(goodLimit).getLimit(), is(goodLimit));
  }

  // withDescending/isDescending

  @Test
  public void withDescendingGood() {
    assertThat(new RecordQuery(goodType).isDescending(), is(false));
    assertThat(new RecordQuery(goodType).withDescending(true).isDescending(), is(true));
  }
}