/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

/**
 * Storage TX hook. It shares same lifecycle as {@link StorageTx}.
 *
 * @since 3.0
 */
public abstract class StorageTxHook
{
  /**
   * Called when {@link StorageTx#saveComponent(Component)} invoked with created component.
   */
  public void createComponent(final Component... components) {}

  /**
   * Called when {@link StorageTx#saveComponent(Component)} invoked with updated component.
   */
  public void updateComponent(final Component... components) {}

  /**
   * Called when {@link StorageTx#deleteComponent(Component)} invoked.
   */
  public void deleteComponent(final Component... components) {}

  /**
   * Called when {@link StorageTx#saveAsset(Asset)} invoked with created asset. Component parameter is {@code null} for
   * standalone assets.
   */
  public void createAsset(final Asset... assets) {}

  /**
   * Called when {@link StorageTx#saveAsset(Asset)} invoked with updated asset. Component parameter is {@code null} for
   * standalone assets.
   */
  public void updateAsset(final Asset... assets) {}

  /**
   * Called when {@link StorageTx#deleteAsset(Asset)} invoked. Component parameter is {@code null} for standalone
   * assets.
   */
  public void deleteAsset(final Asset... assets) {}

  /**
   * Called after {@link StorageTx#commit()} successfully executed.
   */
  public void postCommit() {}

  /**
   * Called after {@link StorageTx#rollback()} executed.
   */
  public void postRollback() {}
}
