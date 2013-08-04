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

package org.sonatype.appcontext.publisher;

import java.io.PrintStream;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A EntryPublisher the publishes the contexts to supplied {@code java.io.PrintStream}, or to {@code System.out}.
 *
 * @author cstamas
 */
public class PrintStreamEntryPublisher
    extends AbstractStringDumpingEntryPublisher
    implements EntryPublisher
{
  /**
   * The PrintStream to be used for publishing.
   */
  private final PrintStream printStream;

  /**
   * Constructs publisher the publishes to {@code System.out}.
   */
  public PrintStreamEntryPublisher() {
    this(System.out);
  }

  /**
   * Constructs publisher to use supplied print stream.
   *
   * @throws NullPointerException if {@code preintStream} is null
   */
  public PrintStreamEntryPublisher(final PrintStream printStream) {
    this.printStream = Preconditions.checkNotNull(printStream);
  }

  public void publishEntries(final AppContext context) {
    printStream.println(getDumpAsString(context));
  }
}
