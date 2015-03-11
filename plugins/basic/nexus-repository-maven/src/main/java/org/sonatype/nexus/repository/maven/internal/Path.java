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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.sonatype.nexus.common.hash.HashAlgorithm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven non-artifact repository path. Every item in repository may have hashes, stored on paths with proper
 * suffixes.
 *
 * @see ArtifactPath
 * @since 3.0
 */
@Immutable
public class Path
{
  public static enum HashType
  {
    SHA1("sha1", HashAlgorithm.SHA1),

    MD5("md5", HashAlgorithm.MD5);

    private final String ext;

    private final HashAlgorithm hashAlgorithm;

    HashType(final String ext, final HashAlgorithm hashAlgorithm) {
      this.ext = ext;
      this.hashAlgorithm = hashAlgorithm;
    }

    public String getExt() {
      return ext;
    }

    public HashAlgorithm getHashAlgorithm() {
      return hashAlgorithm;
    }
  }

  protected final String path;

  protected final String fileName;

  protected final HashType hashType;

  public Path(final String path)
  {
    this.path = checkNotNull(path);
    this.fileName = this.path.substring(path.lastIndexOf('/') + 1);
    HashType ht = null;
    for (HashType v : HashType.values()) {
      if (this.fileName.endsWith("." + v.getExt())) {
        ht = v;
        break;
      }
    }
    this.hashType = ht;
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  @Nonnull
  public String getFileName() {
    return fileName;
  }

  @Nullable
  public HashType getHashType() {
    return hashType;
  }

  /**
   * Returns {@code true} if this path is subordinate of another path.
   *
   * @see {@link #subordinateOf()}
   */
  public boolean isSubordinate() {
    return isHash();
  }

  /**
   * Returns {@code true} if this path represents a hash.
   */
  public boolean isHash() {
    return hashType != null;
  }

  /**
   * Returns the "main", non-subordinate path of this path. The "main" path is never a hash.
   */
  public Path main() {
    Path path = this;
    while (path.isSubordinate()) {
      path = path.subordinateOf();
    }
    return path;
  }

  /**
   * Returns the "parent" path, that this path is subordinate of, or {@code null}.
   */
  public Path subordinateOf() {
    if (isHash()) {
      int hashSuffixLen = hashType.getExt().length() + 1; // the dot
      return new Path(
          path.substring(0, path.length() - hashSuffixLen)
      );
    }
    return null;
  }

  /**
   * Returns path of passed in hash type that is subordinate of this path. This path cannot be hash.
   */
  public Path hash(final HashType hashType) {
    checkNotNull(hashType);
    checkArgument(!isHash(), "This path is already a hash: " + this);
    return new Path(
        path + "." + hashType.getExt()
    );
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Path)) {
      return false;
    }

    Path that = (Path) o;

    if (!fileName.equals(that.fileName)) {
      return false;
    }
    if (hashType != that.hashType) {
      return false;
    }
    if (!path.equals(that.path)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = path.hashCode();
    result = 31 * result + fileName.hashCode();
    result = 31 * result + (hashType != null ? hashType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "path='" + path + '\'' +
        ", fileName='" + fileName + '\'' +
        ", hashType=" + hashType +
        '}';
  }
}
