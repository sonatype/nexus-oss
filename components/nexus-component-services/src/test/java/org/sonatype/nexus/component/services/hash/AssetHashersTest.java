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
package org.sonatype.nexus.component.services.hash;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import org.sonatype.nexus.component.model.Asset;

import com.google.common.io.BaseEncoding;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.commons.io.Charsets;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssetHashersTest
{
  private static final String DATA = "This is a test message for hashing!";

  private static final String MD5_HASH = "b4b91fa27dd64d4f14cd1e22e6a3c714";
  private static final String SHA1_HASH = "410fee1895a6af9449ae1647276259fd69a75b15";
  private static final String SHA512_HASH = "b90de0708205534bf3bc4e478c3718c7bf78b5ec60902dbbea234aadd748c004cdf94deda2034b0fa8bdc559ac59d6ac622211956bf782da33444d29e8d9f160";

  @Test
  public void md5hash() throws Exception {
    assertThat(encode(AssetHashers.MD5.hash(mockAsset())), is(equalTo(MD5_HASH)));
    assertThat(encode(injectHasher("MD5").hash(mockAsset())), is(equalTo(MD5_HASH)));
  }

  @Test
  public void sha1hash() throws Exception {
    assertThat(encode(AssetHashers.SHA1.hash(mockAsset())), is(equalTo(SHA1_HASH)));
    assertThat(encode(injectHasher("SHA1").hash(mockAsset())), is(equalTo(SHA1_HASH)));
  }

  @Test
  public void sha512hash() throws Exception {
    assertThat(encode(AssetHashers.SHA512.hash(mockAsset())), is(equalTo(SHA512_HASH)));
    assertThat(encode(injectHasher("SHA512").hash(mockAsset())), is(equalTo(SHA512_HASH)));
  }

  private static Asset mockAsset() throws Exception {
    Asset asset = mock(Asset.class);
    when(asset.openStream()).thenReturn(new ByteArrayInputStream(DATA.getBytes(Charsets.UTF_8)));
    return asset;
  }

  private static String encode(byte[] hash) {
    return BaseEncoding.base16().encode(hash).toLowerCase(Locale.ENGLISH);
  }

  private static AssetHasher injectHasher(String name) {
    return Guice.createInjector(new AssetHashers.EnumModule()).getInstance(
        Key.get(AssetHasher.class, Names.named(name)));
  }
}
