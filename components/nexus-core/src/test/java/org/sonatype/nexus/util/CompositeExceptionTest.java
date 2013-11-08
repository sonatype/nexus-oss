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

package org.sonatype.nexus.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link CompositeException}.
 */
public class CompositeExceptionTest
    extends TestSupport
{
  /**
   * Sanity check, is this class actually doing what is meant to do using vararg accepting constructor.
   */
  @Test
  public void simpleUseVarargs() {
    final Throwable re = new RuntimeException("runtime");
    final Throwable io = new IOException("io");

    final CompositeException ce = new CompositeException("composite", re, io);

    assertThat(ce.getSuppressed(), arrayWithSize(2));
    assertThat(ce.getSuppressed()[0], is(re));
    assertThat(ce.getSuppressed()[1], is(io));
  }

  /**
   * Sanity check, is this class actually doing what is meant to do using list accepting constructor.
   */
  @Test
  public void simpleUseList() {
    final Throwable re = new RuntimeException("runtime");
    final Throwable io = new IOException("io");

    final CompositeException ce = new CompositeException("composite", Arrays.asList(re, io));

    assertThat(ce.getSuppressed(), arrayWithSize(2));
    assertThat(ce.getSuppressed()[0], is(re));
    assertThat(ce.getSuppressed()[1], is(io));
  }

  @Test
  public void multiplePrintStackTrace() {
    StringWriter buff = new StringWriter();
    CompositeException exception = new CompositeException("test",
        new Exception("foo"),
        new Exception("bar"),
        new Exception("baz")
    );
    exception.printStackTrace(new PrintWriter(buff));

    log("printed:");
    log(buff);

    // NOTE: There appears to be a bug in logback 1.0.13 which is not handling suppressed exceptions.
    // NOTE: This appears to have been fixed but is not yet released.
    log("logged:");
    logger.info("EXCEPTION", exception);

    log("nested printed:");
    new IOException("nested", exception).printStackTrace(System.out);

    log("default printed:");
    exception.printStackTrace();
  }
}
