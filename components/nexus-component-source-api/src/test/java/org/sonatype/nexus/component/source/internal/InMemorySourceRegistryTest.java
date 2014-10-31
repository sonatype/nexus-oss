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
package org.sonatype.nexus.component.source.internal;

import java.io.IOException;

import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemorySourceRegistryTest
{
  private InMemorySourceRegistry registry;

  private final ComponentSourceId sourceId = new ComponentSourceId("testSource", "uuid");

  @Before
  public void setUp() {
    registry = new InMemorySourceRegistry();
  }

  @Test
  public void additionAndRemoval() {
    final PullComponentSource mock = mock(PullComponentSource.class);
    when(mock.getId()).thenReturn(sourceId);

    assertThat(registry.getSource(sourceId), is(equalTo(null)));

    registry.register(mock);

    assertThat(registry.getSource(sourceId).getId(), is(equalTo(sourceId)));

    registry.unregister(mock);

    assertThat(registry.getSource(sourceId), is(equalTo(null)));
  }

  @Test
  public void callingTestConnectionWorks() throws IOException {
    final PullComponentSource mock = mock(PullComponentSource.class);
    when(mock.getId()).thenReturn(sourceId);

    assertThat(registry.getSource(sourceId), is(equalTo(null)));

    registry.register(mock);

    // Calling methods on a registered source should work

    final PullComponentSource source = registry.getSource(sourceId);
    source.testConnection();
  }

  @Test(expected = IllegalStateException.class)
  public void callingTestConnectionFailsAfterUnregistration() throws IOException {
    final PullComponentSource mock = mock(PullComponentSource.class);
    when(mock.getId()).thenReturn(sourceId);

    assertThat(registry.getSource(sourceId), is(equalTo(null)));

    registry.register(mock);
    final PullComponentSource source = registry.getSource(sourceId);

    registry.unregister(mock);
    source.testConnection();
  }
}
