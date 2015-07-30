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
package org.sonatype.nexus.repository.docker;

import java.io.IOException;
import java.util.Map;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

/**
 * Provides persistence for Docker V1 protocol.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface DockerFacet
    extends Facet
{
  /**
   * Retrieve images index.
   *
   * @param name docker repository name
   * @return image index JSON
   */
  Content getImagesIndex(String name) throws IOException;

  /**
   * Store images index.
   *
   * @param name    docker repository name
   * @param payload image index JSON
   */
  void putImagesIndex(String name, Payload payload) throws IOException;

  /**
   * Retrieve layer metadata.
   *
   * @param layerId layer id
   * @return layer metadata (JSON)
   */
  Content getLayerMetadata(String layerId) throws IOException;

  /**
   * Store layer metadata.
   *
   * @param layerId layer id
   * @param payload layer metadata (JSON)
   */
  void putLayerMetadata(String layerId, Payload payload) throws IOException;

  /**
   * Retrieve layer content.
   *
   * @param layerId layer id
   * @return layer content
   */
  Content getLayerContent(String layerId) throws IOException;

  /**
   * Store layer content.
   *
   * @param layerId layer id
   * @param payload layer content
   */
  void putLayerContent(String layerId, Payload payload) throws IOException;

  /**
   * Store layer checksum (validates storage of layer metadata/content)
   *
   * @param layerId  layer id
   * @param checksum checksum of layer metadata + content
   */
  void putLayerChecksum(String layerId, String checksum) throws IOException;

  /**
   * Retrieve layer ancestry (list of all parents)
   *
   * @param layerId layer id
   * @return layer ancestry (JSON)
   */
  Content getLayerAncestry(String layerId) throws IOException;

  /**
   * Retrieve ilayer id corresponding to an image.
   *
   * @param name image name
   * @param tag  image tag
   * @return layer id (JSON)
   */
  Content getTag(String name, String tag) throws IOException;

  /**
   * Retrieve mapping between all known tags and corresponding layer ids.
   *
   * @param name image name
   * @return mapping (JSON)
   */
  Content getTags(String name) throws IOException;

  /**
   * Stores mapping between a tag and its layer id.
   *
   * @param name           image name
   * @param tag            image tag
   * @param daemonMetadata metadata about used docker daemon (versions, os, arch)
   * @param payload        layer id (JSON
   */
  void putTag(String name, String tag, Map<String, String> daemonMetadata, Payload payload)
      throws IOException;
}
