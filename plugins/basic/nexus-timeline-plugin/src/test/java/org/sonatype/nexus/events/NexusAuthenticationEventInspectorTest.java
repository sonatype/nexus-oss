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

package org.sonatype.nexus.events;

import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.nexus.auth.ClientInfo;
import org.sonatype.nexus.auth.NexusAuthenticationEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.record.NexusAuthenticationEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class NexusAuthenticationEventInspectorTest
    extends PlexusTestCaseSupport
{
  @Before
  public void manglePlexus()
      throws Exception
  {
    // this is "our" feed recorder
    final ComponentDescriptor<DummyFeedRecorder> fakeFeedRecorder =
        new ComponentDescriptor<DummyFeedRecorder>(DummyFeedRecorder.class, getContainer().getLookupRealm());
    fakeFeedRecorder.setRoleClass(FeedRecorder.class);
    getContainer().addComponentDescriptor(fakeFeedRecorder);

    // mocking configuration
    final NexusConfiguration fakeNexusConfiguration = Mockito.mock(NexusConfiguration.class);
    Mockito.when(fakeNexusConfiguration.getAnonymousUsername()).thenReturn("anonymous");
    getContainer().addComponent(fakeNexusConfiguration, NexusConfiguration.class, Hints.DEFAULT_HINT);
  }

  public void perform(final String username, final int expected)
      throws Exception
  {
    final DummyFeedRecorder feedRecorder = (DummyFeedRecorder) lookup(FeedRecorder.class);

    final NexusAuthenticationEventInspector naei =
        (NexusAuthenticationEventInspector) lookup(EventInspector.class,
            NexusAuthenticationEventInspector.class.getSimpleName());

    final ClientInfo authSuccess = new ClientInfo(username, "192.168.0.1", "Foo/Bar");
    final ClientInfo authFailed = new ClientInfo(username, "192.168.0.1", "Foo/Bar");

    NexusAuthenticationEvent naeSuccess = new NexusAuthenticationEvent(this, authSuccess, true);
    NexusAuthenticationEvent naeFailed = new NexusAuthenticationEvent(this, authFailed, false);

    // we send same event 5 times, but only one of them should be recorded since the rest 4 are "similar" and within
    // 2 sec
    for (int i = 0; i < 5; i++) {
      naei.inspect(naeSuccess);
    }
    // we send another event 5 times, but only one of them should be recorded since it is not "similar" to previous
    // sent ones, but the rest 4 are "similar" and within 2 sec
    for (int i = 0; i < 5; i++) {
      naei.inspect(naeFailed);
    }
    // we sleep a bit over two seconds
    Thread.sleep(2100L);
    // and we send again the second event, but this one should be recorded, since the gap between last sent and this
    // is more than 2 seconds
    naei.inspect(naeFailed);

    // total 11 events "fired", but 3 recorded due to "similarity filtering"
    MatcherAssert.assertThat(feedRecorder.getReceivedEventCount(), CoreMatchers.equalTo(expected));
  }

  @Test
  public void testNonAnon()
      throws Exception
  {
    perform("test", 3);
  }

  @Test
  public void testAnon()
      throws Exception
  {
    perform("anonymous", 0);
  }
}
