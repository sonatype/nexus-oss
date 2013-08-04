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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A composite {@link Exception} descendant, that is able to collect multiple causes to have them throw at the end of
 * some batch processing for example. Inspired by code from <a href=
 * "http://stackoverflow.com/questions/12481583/exception-composition-in-java-when-both-first-strategy-and-recovery-strategy-fai"
 * >Stack Overflow</a>. Note: this exception merely serves the purpose to hold multiple causes, but it not quite usable
 * to log them. As today, Nexus uses SLF4J as logging API, that might be backed by any backend out of many existing
 * (think WAR, but today logback is used) the overridden methods are not used. Hence, in case of logging
 * {@link CompositeException}, the multiple causes will not be logged, you still need to manually log them, or process
 * in any other way, if needed.
 *
 * @author cstamas
 * @since 2.2
 */
public class CompositeException
    extends Exception
{
  private static final long serialVersionUID = 1386505977462170509L;

  private final List<Throwable> causes;

  // ==

  public CompositeException(final Throwable... causes) {
    this(null, causes);
  }

  public CompositeException(final String message, final Throwable... causes) {
    this(message, Arrays.asList(causes));
  }

  public CompositeException(final List<? extends Throwable> causes) {
    this(null, causes);
  }

  public CompositeException(final String message, final List<? extends Throwable> causes) {
    super(message);
    final ArrayList<Throwable> c = new ArrayList<Throwable>();
    if (causes != null && !causes.isEmpty()) {
      c.addAll(causes);
    }
    this.causes = Collections.unmodifiableList(c);
  }

  public List<Throwable> getCauses() {
    return causes;
  }

  // ==

  @Override
  public void printStackTrace() {
    if (causes.isEmpty()) {
      super.printStackTrace();
      return;
    }
    for (Throwable cause : causes) {
      cause.printStackTrace();
    }
  }

  @Override
  public void printStackTrace(final PrintStream s) {
    if (causes.isEmpty()) {
      super.printStackTrace(s);
      return;
    }
    for (Throwable cause : causes) {
      cause.printStackTrace(s);
    }
  }

  @Override
  public void printStackTrace(final PrintWriter s) {
    if (causes.isEmpty()) {
      super.printStackTrace(s);
      return;
    }
    for (Throwable cause : causes) {
      cause.printStackTrace(s);
    }
  }
}
