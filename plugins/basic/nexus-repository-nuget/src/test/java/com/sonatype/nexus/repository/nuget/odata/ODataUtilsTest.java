/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.odata;

import java.util.Map;

import com.sonatype.nexus.repository.nuget.internal.ComponentQuery;

import com.google.common.collect.Maps;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ODataUtilsTest
{
  @Test
  public void testQuery() throws Exception {

    Map<String, String> query = Maps.newHashMap();
    query.put("$filter", "IsLatestVersion");
    query.put("$orderby", "DownloadCount desc,Id");
    query.put("$skip", "0");
    query.put("$top", "30");
    query.put("searchTerm", "'jilted'");
    query.put("targetFramework", "'net45'");
    query.put("includePrerelease", "false");

    final ComponentQuery foo = ODataUtils.query(query, false);

    System.err.println(foo.getWhere());
    System.err.println(foo.getQuerySuffix());
    System.err.println(foo.getParameters());
  }

  @Test
  public void singleTokenFilterNamesAreExpandedToBooleanExpressions() {
    Map<String, String> query = Maps.newHashMap();
    query.put("$filter", "IsLatestVersion");

    final ComponentQuery foo = ODataUtils.query(query, false);

    assertThat(foo.getWhere(), is("((attributes.nuget.is_latest_version = true))"));
  }

  @Test
  public void testBackReference() {

    System.err.println(
        "$filter=IsLatestVersion".replaceAll("(?i)(IsLatestVersion|IsAbsoluteLatestVersion)", "($1 eq true)"));
  }

}