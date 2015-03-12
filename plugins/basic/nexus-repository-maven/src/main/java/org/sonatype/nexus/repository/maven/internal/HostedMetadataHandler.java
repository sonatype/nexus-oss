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
package org.sonatype.nexus.repository.maven.internal;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.view.Context;

/**
 * Maven metadata handler.
 *
 * @since 3.0
 */
@Singleton
@Named
public class HostedMetadataHandler
    extends HostedHandlerSupport<Path>
{
  @Override
  protected Path getPath(final Context context) throws IllegalArgumentException {
    return context.getAttributes().require(Path.class);
  }

  @Override
  protected MavenPayload get(final Context context, final Path path) throws IOException {
    return context.getRepository().facet(MavenFacet.class).getMetadata(path);
  }

  @Override
  protected void put(final Context context, final Path path, final MavenPayload payload)
      throws IOException, InvalidContentException
  {
    context.getRepository().facet(MavenFacet.class).putMetadata(path, payload);
  }

  @Override
  protected boolean delete(final Context context, final Path path) throws IOException {
    return context.getRepository().facet(MavenFacet.class).deleteMetadata(path);
  }
}
