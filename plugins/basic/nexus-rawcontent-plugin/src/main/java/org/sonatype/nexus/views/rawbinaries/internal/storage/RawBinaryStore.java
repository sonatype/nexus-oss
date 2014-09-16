/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.views.rawbinaries.internal.storage;

import java.io.InputStream;
import java.util.List;

import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

/**
 * If 'RawBinary' is a repository format, then this is an interface that represents the operations defined by the
 * protocol, maps them to component metadata and artifact storage operations.
 *
 * The real purpose of this class is just to provide an example of binary-oriented use of the view framework, however,
 * it does raise some issues:
 *
 * @since 3.0
 */
public interface RawBinaryStore
    extends Lifecycle
{
  List<RawBinary> getForPath(String prefix);

  /**
   * Returns false if the creation was disallowed because another binary already exists at that path.
   */
  boolean create(String path, String mimeType, InputStream inputStream);

  /**
   * Returns true if an artifact was deleted, false if there was no artifact to delete.
   */
  boolean delete(String path);
}
