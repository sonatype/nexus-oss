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
package org.sonatype.nexus.internal.blobstore;

import org.sonatype.nexus.blobstore.api.configuration.BlobStoreConfiguration;
import org.sonatype.nexus.orient.entity.Entity;

/**
 * @since 3.0
 */
public class BlobStoreConfigurationEntity
    extends Entity
    implements BlobStoreConfiguration
{
  /**
   * The name for the BlobStore.
   */
  private String name;

  /**
   * The path to the root of the BlobStore.
   */
  private String path;

  public BlobStoreConfigurationEntity() {
  }
  
  public BlobStoreConfigurationEntity(final String name, final String path) {
    this.name = name;
    this.path = path;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public void setPath(final String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "BlobStoreConfiguration{" +
        "name='" + name + '\'' +
        ", path='" + path + '\'' +
        '}';
  }
}
