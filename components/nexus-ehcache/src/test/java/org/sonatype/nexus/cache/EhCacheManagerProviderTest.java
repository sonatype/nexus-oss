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
package org.sonatype.nexus.cache;

import java.net.URI;

import javax.cache.CacheManager;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link EhCacheManagerProvider}.
 */
public class EhCacheManagerProviderTest
    extends TestSupport
{
  private EhCacheManagerProvider underTest;

  @Before
  public void setUp() throws Exception {
    // Use the default configuration for testing
    underTest = new EhCacheManagerProvider((URI)null);
  }

  @After
  public void tearDown() throws Exception {
    // safety check that we always cleanup
    if (underTest != null) {
      underTest.destroy();
    }
  }

  @Test
  public void basicLifecycle() throws Exception {
    // get should return non-null
    CacheManager cacheManager = underTest.get();
    assertThat(cacheManager, notNullValue());

    // repeated get should return same object
    CacheManager cacheManager2 = underTest.get();
    assertThat(cacheManager2, is(cacheManager));

    // after destroy get should fail
    underTest.destroy();
    try {
      underTest.get();
      fail();
    }
    catch (IllegalStateException e) {
      // expected
    }
  }
}
