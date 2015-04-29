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

import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;

/**
 * Storage {@link Facet}, providing component and asset storage for a repository.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface StorageFacet
    extends Facet
{
  String P_ATTRIBUTES = "attributes";

  String P_BLOB_REF = "blob_ref";

  String P_BUCKET = "bucket";

  String P_CHECKSUM = "checksum";

  String P_COMPONENT = "component";

  String P_CONTENT_TYPE = "content_type";

  String P_FORMAT = "format";

  String P_GROUP = "group";

  String P_LAST_UPDATED = "last_updated";

  String P_NAME = "name";

  String P_PATH = "path";

  String P_REPOSITORY_NAME = "repository_name";

  String P_SIZE = "size";

  String P_VERSION = "version";

  /**
   * Opens a transaction.
   */
  StorageTx openTx();

  /**
   * Invokes passed in {@link Processor}s in context of this storage facet with optional initial context, if any.
   */
  void process(@Nullable Map<String, Object> initialContext, Processor... processors);
}
