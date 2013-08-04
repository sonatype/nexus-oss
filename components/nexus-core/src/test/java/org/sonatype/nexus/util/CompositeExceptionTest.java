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
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link CompositeException} unit tests.
 *
 * @since 2.2
 */
public class CompositeExceptionTest
{
  /**
   * All constructors should work with {@code null}s. But the constructor exception's causes list should never be
   * {@code null}.
   */
  @Test
  public void constructorWithNull() {
    final CompositeException c1 = new CompositeException((Throwable) null);
    Assert.assertNotNull(c1.getCauses());
    final CompositeException c2 = new CompositeException((String) null, (Throwable) null);
    Assert.assertNotNull(c2.getCauses());
    final CompositeException c3 = new CompositeException((List<Throwable>) null);
    Assert.assertNotNull(c3.getCauses());
    final CompositeException c4 = new CompositeException((String) null, (List<Throwable>) null);
    Assert.assertNotNull(c4.getCauses());
  }

  /**
   * Sanity check, is this class actually doing what is meant to do using vararg accepting constructor.
   */
  @Test
  public void simpleUseVarargs() {
    final RuntimeException re = new RuntimeException("runtime");
    final IOException io = new IOException("io");

    final CompositeException ce = new CompositeException("composite", re, io);

    Assert.assertEquals(2, ce.getCauses().size());
    Assert.assertEquals(re, ce.getCauses().get(0));
    Assert.assertEquals(io, ce.getCauses().get(1));
  }

  /**
   * Sanity check, is this class actually doing what is meant to do using list accepting constructor.
   */
  @Test
  public void simpleUseList() {
    final RuntimeException re = new RuntimeException("runtime");
    final IOException io = new IOException("io");

    final CompositeException ce = new CompositeException("composite", Arrays.asList(re, io));

    Assert.assertEquals(2, ce.getCauses().size());
    Assert.assertEquals(re, ce.getCauses().get(0));
    Assert.assertEquals(io, ce.getCauses().get(1));
  }
}
