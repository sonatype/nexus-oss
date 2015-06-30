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

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Storage TX hooks, helper internal class to dispatch asset and component CUD events to hooks.
 *
 * @since 3.0
 */
class StorageTxHooks
    extends StorageTxHook
{
  private final List<StorageTxHook> hooks;

  public StorageTxHooks(final List<StorageTxHook> hooks) {
    this.hooks = checkNotNull(hooks);
  }

  @Override
  public void createComponent(final Component... components) {
    for (StorageTxHook hook : hooks) {
      hook.createComponent(components);
    }
  }

  @Override
  public void updateComponent(final Component... components) {
    for (StorageTxHook hook : hooks) {
      hook.updateComponent(components);
    }
  }

  @Override
  public void deleteComponent(final Component... components) {
    for (StorageTxHook hook : hooks) {
      hook.deleteComponent(components);
    }
  }

  @Override
  public void createAsset(final Asset... assets) {
    for (StorageTxHook hook : hooks) {
      hook.createAsset(assets);
    }
  }

  @Override
  public void updateAsset(final Asset... assets) {
    for (StorageTxHook hook : hooks) {
      hook.updateAsset(assets);
    }
  }

  @Override
  public void deleteAsset(final Asset... assets) {
    for (StorageTxHook hook : hooks) {
      hook.deleteAsset(assets);
    }
  }

  @Override
  public void postCommit() {
    for (StorageTxHook hook : hooks) {
      hook.postCommit();
    }
  }

  @Override
  public void postRollback() {
    for (StorageTxHook hook : hooks) {
      hook.postRollback();
    }
  }
}
