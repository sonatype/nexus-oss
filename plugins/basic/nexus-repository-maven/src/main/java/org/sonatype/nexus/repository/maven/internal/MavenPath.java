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
 * Maven repository path. Every item in repository may have hashes, stored on paths with proper
 * suffixes, and artifact paths have coordinates.
 *
 * @since 3.0
 */
@Immutable
public class MavenPath
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

  public static enum SignatureType
  {
    GPG("asc");

    private final String ext;

    SignatureType(final String ext) {
      this.ext = ext;
    }

    public String getExt() {
      return ext;
    }
  }

  public static class Coordinates
  {
    private final boolean snapshot;

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String baseVersion;

    private final String classifier;

    private final String extension;

    private final SignatureType signatureType;

    public Coordinates(final boolean snapshot,
                       final String groupId,
                       final String artifactId,
                       final String version,
                       final String baseVersion,
                       final String classifier,
                       final String extension,
                       final SignatureType signatureType)
    {
      this.snapshot = snapshot;
      this.groupId = checkNotNull(groupId);
      this.artifactId = checkNotNull(artifactId);
      this.version = checkNotNull(version);
      this.baseVersion = checkNotNull(baseVersion);
      this.classifier = classifier;
      this.extension = checkNotNull(extension);
      this.signatureType = signatureType;
    }

    public boolean isSnapshot() {
      return snapshot;
    }

    @Nonnull
    public String getGroupId() {
      return groupId;
    }

    @Nonnull
    public String getArtifactId() {
      return artifactId;
    }

    @Nonnull
    public String getVersion() {
      return version;
    }

    @Nonnull
    public String getBaseVersion() {
      return baseVersion;
    }

    @Nullable
    public String getClassifier() {
      return classifier;
    }

    @Nonnull
    public String getExtension() {
      return extension;
    }

    @Nullable
    public SignatureType getSignatureType() {
      return signatureType;
    }
  }

  private final String path;

  private final String fileName;

  private final HashType hashType;

  private final Coordinates coordinates;

  public MavenPath(final String path, final Coordinates coordinates)
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
    this.coordinates = coordinates;
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

  @Nullable
  public Coordinates getCoordinates() { return coordinates; }

  /**
   * Returns {@code true} if this path is subordinate of another path.
   *
   * @see {@link #subordinateOf()}
   */
  public boolean isSubordinate() {
    return isHash() || isSignature();
  }

  /**
   * Returns {@code true} if this path represents a hash.
   */
  public boolean isHash() {
    return hashType != null;
  }

  /**
   * Returns {@code true} if this path represents a signature.
   */
  public boolean isSignature() {
    return getCoordinates() != null && getCoordinates().getSignatureType() != null;
  }

  /**
   * Returns the "main", non-subordinate path of this path. The "main" path is never a hash.
   */
  @Nonnull
  public MavenPath main() {
    MavenPath mavenPath = this;
    while (mavenPath.isSubordinate()) {
      mavenPath = mavenPath.subordinateOf();
    }
    return mavenPath;
  }

  /**
   * Returns the "parent" path, that this path is subordinate of, or {@code null}.
   */
  @Nullable
  public MavenPath subordinateOf() {
    Coordinates mainCoordinates = null;
    if (isHash()) {
      int hashSuffixLen = hashType.getExt().length() + 1; // the dot
      if (getCoordinates() != null) {
        mainCoordinates = new Coordinates(
            getCoordinates().isSnapshot(),
            getCoordinates().getGroupId(),
            getCoordinates().getArtifactId(),
            getCoordinates().getVersion(),
            getCoordinates().getBaseVersion(),
            getCoordinates().getClassifier(),
            getCoordinates().getExtension().substring(0, getCoordinates().getExtension().length() - hashSuffixLen),
            getCoordinates().getSignatureType()
        );
      }
      return new MavenPath(
          path.substring(0, path.length() - hashSuffixLen),
          mainCoordinates
      );
    }
    else if (isSignature()) { // coordinates != null && coordinates.signatureType != null
      int signatureSuffixLen = getCoordinates().getSignatureType().getExt().length() + 1; // the dot
      mainCoordinates = new Coordinates(
          getCoordinates().isSnapshot(),
          getCoordinates().getGroupId(),
          getCoordinates().getArtifactId(),
          getCoordinates().getVersion(),
          getCoordinates().getBaseVersion(),
          getCoordinates().getClassifier(),
          getCoordinates().getExtension().substring(0, getCoordinates().getExtension().length() - signatureSuffixLen),
          null
      );
      return new MavenPath(
          path.substring(0, path.length() - signatureSuffixLen),
          mainCoordinates
      );
    }
    return null;
  }

  /**
   * Returns path of passed in hash type that is subordinate of this path. This path cannot be hash.
   */
  @Nonnull
  public MavenPath hash(final HashType hashType) {
    checkNotNull(hashType);
    checkArgument(!isHash(), "This path is already a hash: " + this);
    Coordinates hashCoordinates = null;
    if (getCoordinates() != null) {
      hashCoordinates = new Coordinates(
          getCoordinates().isSnapshot(),
          getCoordinates().getGroupId(),
          getCoordinates().getArtifactId(),
          getCoordinates().getVersion(),
          getCoordinates().getBaseVersion(),
          getCoordinates().getClassifier(),
          getCoordinates().getExtension() + "." + hashType.getExt(),
          getCoordinates().getSignatureType()
      );
    }
    return new MavenPath(
        path + "." + hashType.getExt(),
        hashCoordinates
    );
  }

  /**
   * Returns path of passed in signature type that is subordinate of this path. This path cannot be
   * hash nor signature.
   */
  @Nonnull
  public MavenPath signature(final SignatureType signatureType) {
    checkNotNull(signatureType);
    checkArgument(!isHash(), "This path is already a hash: " + this);
    checkArgument(!isSignature(), "This path is already a signature: " + this);
    checkArgument(getCoordinates() != null, "Only artifact paths may have signatures: " + this);
    Coordinates signatureCoordinates = null;
    if (getCoordinates() != null) {
      signatureCoordinates = new Coordinates(
          getCoordinates().isSnapshot(),
          getCoordinates().getGroupId(),
          getCoordinates().getArtifactId(),
          getCoordinates().getVersion(),
          getCoordinates().getBaseVersion(),
          getCoordinates().getClassifier(),
          getCoordinates().getExtension() + "." + signatureType.getExt(),
          signatureType
      );
    }
    return new MavenPath(
        path + "." + signatureType.getExt(),
        signatureCoordinates
    );
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MavenPath)) {
      return false;
    }
    MavenPath that = (MavenPath) o;
    if (!path.equals(that.path)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return path.hashCode();
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
