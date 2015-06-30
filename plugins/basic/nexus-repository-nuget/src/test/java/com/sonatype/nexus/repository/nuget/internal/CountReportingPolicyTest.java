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
package com.sonatype.nexus.repository.nuget.internal;

import org.junit.Test;

import static com.sonatype.nexus.repository.nuget.internal.proxy.CountReportingPolicy.determineReportedCount;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CountReportingPolicyTest
{

  public static final int PAGE_SIZE = 15;

  @Test
  public void testMatchingCounts() {
    // Hitting nuget.org, with only 43 results cached locally.
    assertThat(determineReportedCount(asList(26123), 15, PAGE_SIZE, page(0)), is(equalTo(26123)));

    // Searching for 'pig', nuget.org has 2 results, but we have none.
    assertThat(determineReportedCount(asList(2), 0, PAGE_SIZE, page(0)), is(equalTo(0)));

    // Searching for 'rf', nuget.org has 3 results, we have three.
    assertThat(determineReportedCount(asList(3), 3, PAGE_SIZE, page(0)), is(equalTo(3)));

    // Searching for 'oz', nuget.org has 12 results, we have 17, and we're looking at page 2.
    assertThat(determineReportedCount(asList(12), 17, PAGE_SIZE, page(1)), is(equalTo(17)));

    // Searching for 'oz', nuget.org has 0 results, we have 17, and we're looking at page 2.
    assertThat(determineReportedCount(asList(0), 17, PAGE_SIZE, page(1)), is(equalTo(17)));

    // Searching for jQuery, nuget.org reports 48, we have 48.
    assertThat(determineReportedCount(asList(48), 48, PAGE_SIZE, page(0)), is(equalTo(48)));
  }

  private int page(int page) {
    return page * PAGE_SIZE;
  }
}