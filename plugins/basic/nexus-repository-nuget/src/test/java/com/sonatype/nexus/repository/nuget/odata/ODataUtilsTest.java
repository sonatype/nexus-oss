/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import com.google.common.collect.ImmutableMap;
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

    final ComponentQuery componentQuery = ODataUtils.query(query, false);

    assertThat(componentQuery.getWhere(),
        is("(attributes.nuget.keywords LIKE :p0) AND  attributes.nuget.is_prerelease=false  " +
            "AND ((attributes.nuget.is_latest_version = true))"));

    assertThat(componentQuery.getQuerySuffix(), is(
        "ORDER BY attributes.nuget.download_count DESC, attributes.nuget.id ASC, id asc, version asc LIMIT 30 OFFSET 0"));

    assertThat(ImmutableMap.of("p0", (Object) "%jilted%").equals(componentQuery.getParameters()), is(true));
  }

  @Test
  public void singleTokenFilterNamesAreExpandedToBooleanExpressions() {
    Map<String, String> query = Maps.newHashMap();
    query.put("$filter", "IsLatestVersion");

    final ComponentQuery componentQuery = ODataUtils.query(query, false);

    assertThat(componentQuery.getWhere(), is("((attributes.nuget.is_latest_version = true))"));
  }
}