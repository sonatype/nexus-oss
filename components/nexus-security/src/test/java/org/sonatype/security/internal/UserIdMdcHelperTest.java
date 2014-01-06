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
package org.sonatype.security.internal;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.security.internal.UserIdMdcHelper.KEY;

/**
 * Tests for {@link UserIdMdcHelper}.
 */
public class UserIdMdcHelperTest
  extends TestSupport
{
  @Before
  public void setUp() throws Exception {
    MDC.remove(KEY);
  }

  @After
  public void tearDown() throws Exception {
    MDC.remove(KEY);
  }

  @Test
  public void isSet() {
    MDC.put(KEY, "test");
    assertThat(UserIdMdcHelper.isSet(), is(true));
  }

  @Test
  public void isSet_withNull() {
    String value = MDC.get(KEY);
    assertThat(value, nullValue());
    assertThat(UserIdMdcHelper.isSet(), is(false));
  }

  @Test
  public void isSet_withBlank() {
    MDC.put(KEY, "");
    assertThat(UserIdMdcHelper.isSet(), is(false));
  }

  @Test
  public void isSet_withUnknown() {
    MDC.put(KEY, UserIdMdcHelper.UNKNOWN);
    assertThat(UserIdMdcHelper.isSet(), is(false));
  }

  @Test(expected = NullPointerException.class)
  public void set_null() {
    UserIdMdcHelper.set(null);
  }

  @Test
  public void set_subject() {
    Subject subject = mock(Subject.class);
    when(subject.getPrincipal()).thenReturn("test");

    UserIdMdcHelper.set(subject);

    assertThat(UserIdMdcHelper.isSet(), is(true));
    assertThat(MDC.get(KEY), is("test"));
  }

  @Test
  public void userId_subject() {
    Subject subject = mock(Subject.class);
    when(subject.getPrincipal()).thenReturn("test");

    assertThat(UserIdMdcHelper.userId(subject), is("test"));
  }

  @Test
  public void userId_nullSubject() {
    assertThat(UserIdMdcHelper.userId(null), is(UserIdMdcHelper.UNKNOWN));
  }

  @Test
  public void userId_nullPrincipal() {
    Subject subject = mock(Subject.class);
    when(subject.getPrincipal()).thenReturn(null);

    assertThat(UserIdMdcHelper.userId(subject), is(UserIdMdcHelper.UNKNOWN));
  }
}
