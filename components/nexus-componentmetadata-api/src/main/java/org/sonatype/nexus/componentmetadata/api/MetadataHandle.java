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
package org.sonatype.nexus.componentmetadata.api;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class MetadataHandle<T>
{
  private final String key;

  private final String version;

  private final T metadata;

  public MetadataHandle(String key, String version, T metadata) {
    this.key = checkNotNull(key);
    this.version = checkNotNull(version);
    this.metadata = checkNotNull(metadata);
  }

  public String getKey() {
    return key;
  }

  public String getVersion() {
    return version;
  }

  public T getMetadata() {
    return metadata;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final MetadataHandle other = (MetadataHandle) obj;
    return Objects.equal(key, other.key)
        && Objects.equal(version, other.version)
        && Objects.equal(metadata, other.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, version, metadata);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("key", key)
        .add("version", version)
        .add("metadata", metadata)
        .toString();
  }
}
