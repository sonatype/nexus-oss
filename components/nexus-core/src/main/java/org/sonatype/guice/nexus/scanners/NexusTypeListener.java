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

package org.sonatype.guice.nexus.scanners;

import org.sonatype.guice.plexus.scanners.PlexusTypeListener;
import org.sonatype.nexus.plugins.RepositoryType;

/**
 * {@link PlexusTypeListener} that also listens for Nexus metadata.
 */
public interface NexusTypeListener
    extends PlexusTypeListener
{
  /**
   * Invoked when the {@link NexusTypeListener} finds a public/exported class.
   *
   * @param clazz The fully-qualified class name
   */
  void hear(String clazz);

  /**
   * Invoked when the {@link NexusTypeListener} finds a {@link RepositoryType}.
   *
   * @param repositoryType The repository type
   */
  void hear(RepositoryType repositoryType);
}
