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

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Supplier;

/**
 * Storage {@link Facet}, providing component and asset storage for a repository.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface StorageFacet
    extends Facet
{

  static String P_ATTRIBUTES = "attributes";

  static String P_BLOB_REF = "blob_ref";

  static String P_BUCKET = "bucket";

  static String P_CHECKSUM = "checksum";

  static String P_COMPONENT = "component";

  static String P_CONTENT_TYPE = "content_type";

  static String P_FORMAT = "format";

  static String P_GROUP = "group";

  static String P_LAST_UPDATED = "last_updated";

  static String P_NAME = "name";

  static String P_PATH = "path";

  static String P_REPOSITORY_NAME = "repository_name";

  static String P_SIZE = "size";

  static String P_VERSION = "version";

  /**
   * Registers a supplier for {@link StorageTxHook}. Only possible while this facet is
   * being initialised, most appropriate in a recipe or alike.
   */
  void registerHookSupplier(Supplier<StorageTxHook> hookSupplier);

  /**
   * Unregisters a supplier for {@link StorageTxHook}. Only possible while this facet is
   * stopped.
   */
  void unregisterHookSupplier(Supplier<StorageTxHook> hookSupplier);

  /**
   * Registers format specific selector for {@link WritePolicy}. If not set, the {@link
   * WritePolicySelector#DEFAULT} is used which returns the configured write policy.
   */
  void registerWritePolicySelector(WritePolicySelector writePolicySelector);

  /**
   * Supplies transactions for use in {@link UnitOfWork}.
   */
  Supplier<StorageTx> txSupplier();
}
