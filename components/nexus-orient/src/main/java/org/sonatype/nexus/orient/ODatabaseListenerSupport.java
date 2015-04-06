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
package org.sonatype.nexus.orient;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseListener;

/**
 * Support for {@link ODatabaseListener} implementations.
 *
 * @since 3.0
 */
public class ODatabaseListenerSupport
    implements ODatabaseListener
{

  @Override
  public void onCreate(final ODatabase iDatabase) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void onDelete(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public void onOpen(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public void onBeforeTxBegin(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public void onBeforeTxRollback(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public void onAfterTxRollback(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public void onBeforeTxCommit(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public void onAfterTxCommit(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public void onClose(final ODatabase iDatabase) {
    // do nothing
  }

  @Override
  public boolean onCorruptionRepairDatabase(final ODatabase iDatabase,
                                            final String iReason,
                                            final String iWhatWillbeFixed)
  {
    return true;
  }
}
