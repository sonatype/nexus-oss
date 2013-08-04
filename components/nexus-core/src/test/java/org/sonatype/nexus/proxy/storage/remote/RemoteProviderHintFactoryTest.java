/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.storage.remote;

import org.sonatype.nexus.proxy.storage.remote.httpclient.HttpClientRemoteStorage;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

import org.junit.Assert;
import org.junit.Test;

public class RemoteProviderHintFactoryTest
    extends PlexusTestCaseSupport
{
  private static final String FAKE_VALUE = "Foo-Bar";

  protected void tearDown()
      throws Exception
  {
    super.tearDown();

    // clear the property
    System.clearProperty(DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY);
  }

  @Test
  public void testIt()
      throws Exception
  {
    RemoteProviderHintFactory hintFactory = this.lookup(RemoteProviderHintFactory.class);

    // clear the property
    System.clearProperty(DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY);

    // nothing set
    Assert.assertEquals(HttpClientRemoteStorage.PROVIDER_STRING, hintFactory.getDefaultHttpRoleHint());

    System.setProperty(DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY, FAKE_VALUE);
    Assert.assertEquals(FAKE_VALUE, hintFactory.getDefaultHttpRoleHint());
  }
}
