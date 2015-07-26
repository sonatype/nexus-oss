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
package org.sonatype.nexus.cache.internal;

import java.net.URL;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.CacheManagerBuilder;
import org.ehcache.config.xml.XmlConfiguration;
import org.junit.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Trails of basic EhCache 3 usage.
 */
public class EhCache3Trial
    extends TestSupport
{
  @Test
  public void plain() throws Exception {
    CacheManager underTest = CacheManagerBuilder.newCacheManagerBuilder().build();
    assertThat(underTest, notNullValue());
    underTest.close();
  }

  @Test
  public void testXml() throws Exception {
    URL url = getClass().getResource("ehcache3-test.xml");
    XmlConfiguration xmlConfig = new XmlConfiguration(url, getClass().getClassLoader());
    CacheManager underTest = CacheManagerBuilder.newCacheManager(xmlConfig);
    try {
      Cache<Object, Object> cache = underTest.getCache("test", Object.class, Object.class);
    }
    finally {
      underTest.close();
    }
  }

  @Test
  public void testJsr107() throws Exception {
    URL url = getClass().getResource("ehcache3-test.xml");
    log(url);

    CachingProvider provider = Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
    log(provider);

    javax.cache.CacheManager manager = provider.getCacheManager(url.toURI(), getClass().getClassLoader());
    log(manager);
  }
}
