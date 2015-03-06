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
 * Maven repository coordinates. Every item in repository may have hashes, stored on paths with proper suffixes.
 *
 * @since 3.0
 */
@Immutable
public class Coordinates
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

  public Coordinates(final String path,
                     final String fileName,
                     final @Nullable HashType hashType)
  {
    this.path = checkNotNull(path);
    this.fileName = checkNotNull(fileName);
    this.hashType = hashType;
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
   * Returns {@code true} if this coordinate is subordinate of another coordinate.
   *
   * @see {@link #subordinateOf()}
   */
  public boolean isSubordinate() {
    return isHash();
  }

  /**
   * Returns {@code true} if this coordinate represents a hash.
   */
  public boolean isHash() {
    return hashType != null;
  }

  /**
   * Returns the "main", non-subordinate coordinate of this coordinate. The "main" coordinate is not a hash.
   */
  public Coordinates main() {
    Coordinates coordinates = this;
    while (coordinates.isSubordinate()) {
      coordinates = coordinates.subordinateOf();
    }
    return coordinates;
  }

  /**
   * Returns the "parent" coordinate, that this coordinate is subordinate of, or {@code null}.
   */
  public Coordinates subordinateOf() {
    if (isHash()) {
      int hashSuffixLen = hashType.getExt().length() + 1; // the dot
      return new Coordinates(
          path.substring(0, path.length() - hashSuffixLen),
          fileName.substring(0, fileName.length() - hashSuffixLen),
          null
      );
    }
    return null;
  }

  /**
   * Returns coordinates of passed in hash type that is subordinate of this coordinate. This coordinate cannot be hash.
   */
  public Coordinates hash(final HashType hashType) {
    checkNotNull(hashType);
    checkArgument(!isHash(), "This coordinate is already a hash: " + this);
    return new Coordinates(
        path + "." + hashType.getExt(),
        fileName + "." + hashType.getExt(),
        hashType
    );
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Coordinates)) {
      return false;
    }

    Coordinates that = (Coordinates) o;

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
