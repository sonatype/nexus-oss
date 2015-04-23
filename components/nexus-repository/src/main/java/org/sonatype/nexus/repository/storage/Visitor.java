/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.storage;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * A generic node visitor.  No assumptions should be made agains tx passed in, it may be same or
 * different than passed into other methods.
 *
 * @since 3.0
 */
public class Visitor<T>
    extends ComponentSupport
{
  /**
   * Always invoked before visiting begins.
   */
  public void before(final StorageTx tx) {}

  /**
   * Invoked for each visited node.
   */
  public void visit(final StorageTx tx, final T node) {}

  /**
   * Always invoked in case of visiting is finished. If exception thrown even by this same visitor, this method will be
   * invoked.
   */
  public void after(final StorageTx tx, final @Nullable Exception e) {}
}
