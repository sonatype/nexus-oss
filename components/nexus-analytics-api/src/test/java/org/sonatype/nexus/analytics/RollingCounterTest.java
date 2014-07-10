package org.sonatype.nexus.analytics;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link RollingCounter}.
 */
public class RollingCounterTest
  extends TestSupport
{
  @Test
  public void basics() {
    RollingCounter underTest = new RollingCounter(5);
    assertThat(underTest.next(), is(0L));
    assertThat(underTest.next(), is(1L));
    assertThat(underTest.next(), is(2L));
    assertThat(underTest.next(), is(3L));
    assertThat(underTest.next(), is(4L));
    assertThat(underTest.next(), is(5L));

    // rolls
    assertThat(underTest.next(), is(0L));
    assertThat(underTest.next(), is(1L));
    assertThat(underTest.next(), is(2L));
    assertThat(underTest.next(), is(3L));
    assertThat(underTest.next(), is(4L));
    assertThat(underTest.next(), is(5L));

    // rolls
    assertThat(underTest.next(), is(0L));
    assertThat(underTest.next(), is(1L));
    assertThat(underTest.next(), is(2L));
    assertThat(underTest.next(), is(3L));
    assertThat(underTest.next(), is(4L));
    assertThat(underTest.next(), is(5L));
  }

  @Test(expected = IllegalArgumentException.class)
  public void maxTooSmall() {
    new RollingCounter(0);
  }
}
