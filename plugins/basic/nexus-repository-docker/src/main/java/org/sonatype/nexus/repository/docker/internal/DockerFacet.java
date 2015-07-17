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
package org.sonatype.nexus.repository.docker.internal;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

/**
 * @since 3.0
 */
@Facet.Exposed
public interface DockerFacet
    extends Facet
{
  Content getImagesIndex(String namespace, String name) throws IOException;

  void putImagesIndex(String namespace, String name, Payload payload) throws IOException;

  Content getMetadata(String imageId) throws IOException;

  void putMetadata(String imageId, Payload payload) throws IOException;

  Content getLayer(String imageId) throws IOException;

  void putLayer(String imageId, Payload payload) throws IOException;

  void putChecksum(String imageId, String checksum) throws IOException;

  void putTag(String namespace, String name, String tag, Payload payload) throws IOException;
}
