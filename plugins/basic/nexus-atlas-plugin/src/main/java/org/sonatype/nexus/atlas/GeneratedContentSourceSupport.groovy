/**
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

package org.sonatype.nexus.atlas

import groovy.transform.ToString
import org.sonatype.sisu.goodies.common.ComponentSupport

import java.nio.file.Files

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Support for generated {@link SupportBundle.ContentSource} implementations.
 *
 * @since 2.7
 */
@ToString(includePackage=false, includeFields=true, includes='type,path')
abstract class GeneratedContentSourceSupport
extends ComponentSupport
implements SupportBundle.ContentSource
{
  private final SupportBundle.ContentSource.Type type

  private final String path

  private File file

  GeneratedContentSourceSupport(final SupportBundle.ContentSource.Type type, final String path) {
    this.type = checkNotNull(type)
    this.path = checkNotNull(path)
  }

  @Override
  SupportBundle.ContentSource.Type getType() {
    return type
  }

  @Override
  String getPath() {
    return path
  }

  @Override
  void prepare() {
    assert file == null
    file = File.createTempFile(path.replaceAll('/','-') + '-', '.tmp').canonicalFile
    log.trace 'Preparing: {}', file
    generate(file)
  }

  protected abstract void generate(File file)

  @Override
  int getSize() {
    assert file.exists()
    return file.length()
  }

  @Override
  InputStream getContent() {
    assert file.exists()
    return file.newInputStream()
  }

  @Override
  void cleanup() {
    if (file != null) {
      log.trace 'Cleaning: {}', file
      Files.delete(file.toPath())
      file = null
    }
  }
}