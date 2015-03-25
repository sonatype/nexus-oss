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

import java.io.InputStream;
import java.util.Map;

import org.sonatype.nexus.common.hash.HashAlgorithm;
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
  String E_CONTAINS_COMPONENTS_WITH_LABEL = "contains_components_with_label";

  String E_HAS_LABEL = "has_label";

  String E_OWNS_ASSET = "owns_asset";

  String E_OWNS_COMPONENT = "owns_component";

  String E_PART_OF_COMPONENT = "part_of_component";

  String P_ATTRIBUTES = "attributes";

  String P_BLOB_REF = "blob_ref";

  String P_CHECKSUM = "checksum";

  String P_CONTENT_TYPE = "content_type";

  String P_FORMAT = "format";

  String P_GROUP = "group";

  String P_LAST_UPDATED = "last_updated";

  String P_NAME = "name";

  String P_PATH = "path";

  String P_REPOSITORY_NAME = "repository_name";

  String P_SIZE = "size";

  String P_VERSION = "version";

  String V_ASSET = "asset";

  String V_COMPONENT = "component";

  String V_LABEL = "label";

  String V_BUCKET = "bucket";

  /**
   * Creates a new Blob and returns handle to it. Blobs created but not attached to any asset are "orphans", and they
   * will be eventually GCed from blob store, no need for cleanup on behalf of caller, still, it is possible if
   * use case allows clear solution to it, see {@link #deleteBlob(BlobHandle)}.
   */
  BlobHandle createBlob(InputStream inputStream, Map<String, String> headers,
                        Iterable<HashAlgorithm> hashAlgorithms, String contentType);

  /**
   * Deletes the blob created but not attached. Attached blobs must not be deleted using this method.
   */
  boolean deleteBlob(BlobHandle blobHandle);

  /**
   * Opens a transaction.
   */
  StorageTx openTx();
}
