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

package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.util.Collection;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;

/**
 * A metadata updater that offers simple metadata management services.
 *
 * @author cstamas
 */
public interface MetadataUpdater
{
  //
  // "Single shot" methods, used from Nexus to maintain metadata on-the-fly
  //

  /**
   * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
   */
  void deployArtifact(ArtifactStoreRequest request)
      throws IOException;

  /**
   * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
   */
  void undeployArtifact(ArtifactStoreRequest request)
      throws IOException;

  //
  // "Multi shot" methods, used from Nexus/CLI tools to maintain metadata in batch/scanning mode
  //

  /**
   * Calling this method <b>replaces</b> the GAV, GA and G metadatas accordingly.
   */
  void deployArtifacts(Collection<ArtifactStoreRequest> requests)
      throws IOException;

  /**
   * Calling this method <b>replaces</b> the GAV, GA and G metadatas accordingly.
   */
  void undeployArtifacts(Collection<ArtifactStoreRequest> requests)
      throws IOException;

  /**
   * Give me a coll, and i will createate the metadata.
   */
  void recreateMetadata(StorageCollectionItem coll)
      throws IOException;
}
