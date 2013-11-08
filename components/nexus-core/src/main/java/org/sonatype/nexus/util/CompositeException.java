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

import java.util.Collection;
import java.util.List;

import org.sonatype.sisu.goodies.common.Internal;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A composite {@link Exception} helper to attach suppressed throwables.
 *
 * @since 2.2
 */
@Internal
public class CompositeException
    extends Exception
{
  private static final long serialVersionUID = 2L;

  @Deprecated
  public CompositeException(final Throwable... causes) {
    this(null, causes);
  }

  @Deprecated
  public CompositeException(final String message, final Throwable... causes) {
    super(message);
    for (Throwable cause : causes) {
      addSuppressed(cause);
    }
  }

  @Deprecated
  public CompositeException(final List<? extends Throwable> causes) {
    this(null, causes);
  }

  @Deprecated
  public CompositeException(final String message, final List<? extends Throwable> causes) {
    super(message);
    for (Throwable cause : causes) {
      addSuppressed(cause);
    }
  }

  /**
   * @deprecated use {@link #getSuppressed()}.
   */
  @Deprecated
  public List<Throwable> getCauses() {
    return ImmutableList.copyOf(getSuppressed());
  }

  //
  // FIXME: Replace with Throwables2 impl from goodies-common once that library is based on Java7
  //

  /**
   * Helper to composite suppressed exceptions onto given throwable and throw.
   */
  @Internal
  public static <T extends Throwable> T composite(final T root, final Throwable... suppressed)
      throws T
  {
    checkNotNull(suppressed);
    for (Throwable t : suppressed) {
      root.addSuppressed(t);
    }
    throw root;
  }

  /**
   * Helper to composite suppressed exceptions onto given throwable and throw.
   */
  @Internal
  public static <T extends Throwable> T composite(final T root, final Collection<? extends Throwable> suppressed)
      throws T
  {
    checkNotNull(suppressed);
    for (Throwable t : suppressed) {
      root.addSuppressed(t);
    }
    throw root;
  }
}
