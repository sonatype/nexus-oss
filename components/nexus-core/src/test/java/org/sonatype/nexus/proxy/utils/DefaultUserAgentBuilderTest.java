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
package org.sonatype.nexus.proxy.utils;

import java.util.List;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link DefaultUserAgentBuilder}
 */
public class DefaultUserAgentBuilderTest
    extends TestSupport
{

  /**
   * Properties we want to see in the UA.
   */
  private static List<String> sysProps = Lists.newArrayList("os.name", "os.arch", "os.version", "java.version");

  @Mock
  private RemoteStorageContext ctx;

  @Mock
  private RemoteConnectionSettings settings;

  @Mock
  private SystemStatus status;

  private DefaultUserAgentBuilder underTest;

  @Before
  public void setup() {
    this.underTest = new DefaultUserAgentBuilder(Providers.of(status));

    when(status.getVersion()).thenReturn("2.1-FAKE");
    when(status.getEditionShort()).thenReturn("FAKENEXUS");

    when(ctx.getRemoteConnectionSettings()).thenReturn(settings);
    when(settings.getUserAgentCustomizationString()).thenReturn("SETTINGS_CUSTOMIZATION");
  }

  @Test
  public void testGlobalUA() {
    final String ua = underTest.formatUserAgentString(ctx);

    assertThat(ua, containsString("2.1-FAKE"));
    assertThat(ua, containsString("FAKENEXUS"));
    for (String prop : sysProps) {
      assertThat(ua, containsString(System.getProperty(prop)));
    }

    assertThat(ua, containsString("SETTINGS_CUSTOMIZATION"));
  }
}
