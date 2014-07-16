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
package org.sonatype.nexus.componentmetadata.internal;

import org.sonatype.nexus.componentmetadata.RecordId;
import org.sonatype.nexus.componentmetadata.RecordQuery;
import org.sonatype.nexus.componentmetadata.RecordType;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrientQueryBuilderTest
    extends TestSupport
{
  private final String typeName = "Test";

  private final RecordType goodType = new RecordType(typeName);

  private final String fakeRidString = "#11:34";

  private final ORID fakeRid = new ORecordId(fakeRidString);

  private RecordIdObfuscator recordIdObfuscator;

  private OClass oClass;

  private RecordQuery goodQuery;

  @Before
  public void setup() {
    recordIdObfuscator = mock(RecordIdObfuscator.class);
    when(recordIdObfuscator.encode(any(OClass.class), any(ORID.class))).thenReturn(fakeRidString);
    when(recordIdObfuscator.decode(any(OClass.class), any(String.class))).thenReturn(new ORecordId(fakeRidString));

    oClass = mock(OClass.class);
    goodQuery = new RecordQuery(goodType);
  }

  @Test(expected = NullPointerException.class)
  public void badQueryNull() {
    new OrientQueryBuilder(null, oClass, recordIdObfuscator);
  }

  @Test(expected = NullPointerException.class)
  public void badOrientClassNull() {
    new OrientQueryBuilder(goodQuery, null, recordIdObfuscator);
  }

  @Test(expected = NullPointerException.class)
  public void badIdObfuscatorNull() {
    new OrientQueryBuilder(goodQuery, oClass, null);
  }

  @Test
  public void buildGoodQuery() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery);
    assertThat(builder.build(), is("SELECT FROM Test"));
  }

  @Test
  public void buildGoodQueryWithCount() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery);
    assertThat(builder.withCount(true).build(), is("SELECT COUNT(*) FROM Test"));
  }

  @Test
  public void buildGoodQueryWithEqualOne() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withEqual("arg1", "val1"));
    assertThat(builder.build(), is("SELECT FROM Test WHERE arg1 = :arg1"));
  }

  @Test
  public void buildGoodQueryWithEqualOneAndSkipRecord() {
    final RecordId skipRecord = new RecordId(typeName, recordIdObfuscator.encode(oClass, fakeRid));
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withEqual("arg1", "val1")
        .withSkipRecord(skipRecord));
    assertThat(builder.build(), is("SELECT FROM Test WHERE @rid > #11:34 AND arg1 = :arg1"));
  }

  @Test
  public void buildGoodQueryWithEqualTwo() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withEqual("arg1", "val1")
        .withEqual("arg2", "val2"));
    assertThat(builder.build(), is("SELECT FROM Test WHERE arg1 = :arg1 AND arg2 = :arg2"));
  }

  @Test
  public void buildGoodQueryWithSkip() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withSkip(1));
    assertThat(builder.build(), is("SELECT FROM Test SKIP 1"));
  }

  @Test
  public void buildGoodQueryWithSkipRecord() {
    final ORID rid = new ORecordId("#11:34");
    final RecordId skipRecord = new RecordId(typeName, recordIdObfuscator.encode(oClass, rid));
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withSkipRecord(skipRecord));
    assertThat(builder.build(), is("SELECT FROM Test WHERE @rid > #11:34"));
  }

  @Test
  public void buildGoodQueryWithOrderBy() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withOrderBy("orderBy"));
    assertThat(builder.build(), is("SELECT FROM Test ORDER BY orderBy"));
  }

  @Test
  public void buildGoodQueryWithOrderByAndDescending() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withOrderBy("orderBy")
        .withDescending(true));
    assertThat(builder.build(), is("SELECT FROM Test ORDER BY orderBy DESC"));
  }

  @Test
  public void buildGoodQueryWithLimit() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withLimit(2));
    assertThat(builder.build(), is("SELECT FROM Test LIMIT 2"));
  }

  @Test
  public void buildGoodQueryWithDescending() {
    OrientQueryBuilder builder = goodBuilderWith(goodQuery
        .withDescending(true));
    assertThat(builder.build(), is("SELECT FROM Test"));
  }

  private OrientQueryBuilder goodBuilderWith(RecordQuery query) {
    return new OrientQueryBuilder(query, oClass, recordIdObfuscator);
  }
}