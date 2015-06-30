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
package org.sonatype.nexus.timeline.feeds.subscribers;

import org.sonatype.nexus.security.ClientInfo;
import org.sonatype.nexus.security.anonymous.AnonymousConfiguration;
import org.sonatype.nexus.security.anonymous.AnonymousManager;
import org.sonatype.nexus.security.authc.NexusAuthenticationEvent;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AuthSubscriber}.
 */
public class AuthSubscriberTest
    extends TestSupport
{
  private static final String ANONYMOUS = "anonymous";

  private AuthSubscriber underTest;

  private DummyFeedRecorder feedRecorder = new DummyFeedRecorder();

  @Mock
  private AnonymousManager anonymousManager;

  @Before
  public void setUp() throws Exception {
    AnonymousConfiguration anonymousConfiguration = new AnonymousConfiguration();
    anonymousConfiguration.setUserId(ANONYMOUS);
    when(anonymousManager.getConfiguration()).thenReturn(anonymousConfiguration);

    underTest = new AuthSubscriber(anonymousManager, feedRecorder);
  }

  public void perform(final String username, final int expected) throws Exception {
    final ClientInfo authSuccess = new ClientInfo(username, "192.168.0.1", "Foo/Bar");
    final ClientInfo authFailed = new ClientInfo(username, "192.168.0.1", "Foo/Bar");

    NexusAuthenticationEvent naeSuccess = new NexusAuthenticationEvent(authSuccess, true);
    NexusAuthenticationEvent naeFailed = new NexusAuthenticationEvent(authFailed, false);

    // we send same event 5 times, but only one of them should be recorded since the rest 4 are "similar" and within
    // 2 sec
    for (int i = 0; i < 5; i++) {
      underTest.on(naeSuccess);
    }
    // we send another event 5 times, but only one of them should be recorded since it is not "similar" to previous
    // sent ones, but the rest 4 are "similar" and within 2 sec
    for (int i = 0; i < 5; i++) {
      underTest.on(naeFailed);
    }
    // we sleep a bit over two seconds
    Thread.sleep(2100L);
    // and we send again the second event, but this one should be recorded, since the gap between last sent and this
    // is more than 2 seconds
    underTest.on(naeFailed);

    // total 11 events "fired", but 3 recorded due to "similarity filtering"
    assertThat(feedRecorder.getReceivedEventCount(), is(expected));
  }

  @Test
  public void testNonAnon() throws Exception {
    perform("test", 3);
  }

  @Test
  public void testAnon() throws Exception {
    perform(ANONYMOUS, 0);
  }
}
