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

import java.util.List;

import org.sonatype.sisu.goodies.common.Throwables2;

import com.google.common.collect.ImmutableList;

/**
 * A composite {@link Exception} helper to attach suppressed throwables.
 *
 * @since 2.2
 *
 * @deprecated To be removed. Use {@link Throwables2#composite(Throwable, Throwable...)} helpers instead.
 */
@Deprecated
public class CompositeException
    extends Exception
{
  private static final long serialVersionUID = 2L;

  public CompositeException(final Throwable... causes) {
    this(null, causes);
  }

  public CompositeException(final String message, final Throwable... causes) {
    super(message);
    for (Throwable cause : causes) {
      addSuppressed(cause);
    }
  }

  public CompositeException(final List<? extends Throwable> causes) {
    this(null, causes);
  }

  public CompositeException(final String message, final List<? extends Throwable> causes) {
    super(message);
    for (Throwable cause : causes) {
      addSuppressed(cause);
    }
  }

  public List<Throwable> getCauses() {
    return ImmutableList.copyOf(getSuppressed());
  }
}
