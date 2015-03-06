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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven repository artifact coordinates. Every artifact in repository have Maven Coordinates, may have hashes and
 * signatures.
 *
 * @since 3.0
 */
@Immutable
public class ArtifactCoordinates
    extends Coordinates
{
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

  private final String groupId;

  private final String artifactId;

  private final String version;

  private final String baseVersion;

  private final String classifier;

  private final String extension;

  private final SignatureType signatureType;

  public ArtifactCoordinates(final String path,
                             final String fileName,
                             final @Nullable HashType hashType,
                             final String groupId,
                             final String artifactId,
                             final String version,
                             final String baseVersion,
                             final @Nullable String classifier,
                             final String extension,
                             final @Nullable SignatureType signatureType)
  {
    super(path, fileName, hashType);
    this.groupId = checkNotNull(groupId);
    this.artifactId = checkNotNull(artifactId);
    this.version = checkNotNull(version);
    this.baseVersion = checkNotNull(baseVersion);
    this.classifier = classifier;
    this.extension = checkNotNull(extension);
    this.signatureType = signatureType;
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

  /**
   * Returns {@code true} if this coordinate is subordinate of another coordinate.
   *
   * @see {@link #subordinateOf()}
   */
  @Override
  public boolean isSubordinate() {
    return isSignature() || super.isSubordinate();
  }

  /**
   * Returns {@code true} if this coordinate represents a signature.
   */
  public boolean isSignature() {
    return signatureType != null;
  }

  /**
   * Returns the "main" non-subordinate coordinate of this coordinate. The "main" coordinate is not a hash, nor a
   * signature, nor a hash of a signature.
   */
  @Override
  public ArtifactCoordinates main() {
    return (ArtifactCoordinates) super.main();
  }

  /**
   * Returns the "parent" coordinate, that this coordinate is subordinate of, or {@code null}.
   */
  public ArtifactCoordinates subordinateOf() {
    if (isHash()) {
      int hashSuffixLen = hashType.getExt().length() + 1; // the dot
      return new ArtifactCoordinates(
          path.substring(0, path.length() - hashSuffixLen),
          fileName.substring(0, fileName.length() - hashSuffixLen),
          null,
          groupId,
          artifactId,
          version,
          baseVersion,
          classifier,
          extension.substring(0, extension.length() - hashSuffixLen),
          signatureType
      );
    }
    else if (isSignature()) {
      int signatureSuffixLen = signatureType.getExt().length() + 1; // the dot
      return new ArtifactCoordinates(
          path.substring(0, path.length() - signatureSuffixLen),
          fileName.substring(0, fileName.length() - signatureSuffixLen),
          null,
          groupId,
          artifactId,
          version,
          baseVersion,
          classifier,
          extension.substring(0, extension.length() - signatureSuffixLen),
          null
      );
    }
    return null;
  }

  /**
   * Returns coordinates of passed in hash type that is subordinate of this coordinate. This coordinate cannot be hash.
   */
  @Override
  public ArtifactCoordinates hash(final HashType hashType) {
    checkNotNull(hashType);
    checkArgument(!isHash(), "This coordinate is already a hash: " + this);
    return new ArtifactCoordinates(
        path + "." + hashType.getExt(),
        fileName + "." + hashType.getExt(),
        hashType,
        groupId,
        artifactId,
        version,
        baseVersion,
        classifier,
        extension + "." + hashType.getExt(),
        signatureType
    );
  }

  /**
   * Returns coordinates of passed in signature type that is subordinate of this coordinate. This coordinate cannot be
   * hash nor signature.
   */
  public ArtifactCoordinates signature(final SignatureType signatureType) {
    checkNotNull(signatureType);
    checkArgument(!isHash(), "This coordinate is already a hash: " + this);
    checkArgument(!isSignature(), "This coordinate is already a signature: " + this);
    return new ArtifactCoordinates(
        path + "." + signatureType.getExt(),
        fileName + "." + signatureType.getExt(),
        hashType,
        groupId,
        artifactId,
        version,
        baseVersion,
        classifier,
        extension + "." + signatureType.getExt(),
        signatureType
    );
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArtifactCoordinates)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ArtifactCoordinates that = (ArtifactCoordinates) o;

    if (!artifactId.equals(that.artifactId)) {
      return false;
    }
    if (!baseVersion.equals(that.baseVersion)) {
      return false;
    }
    if (classifier != null ? !classifier.equals(that.classifier) : that.classifier != null) {
      return false;
    }
    if (!extension.equals(that.extension)) {
      return false;
    }
    if (!groupId.equals(that.groupId)) {
      return false;
    }
    if (signatureType != that.signatureType) {
      return false;
    }
    if (!version.equals(that.version)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + groupId.hashCode();
    result = 31 * result + artifactId.hashCode();
    result = 31 * result + version.hashCode();
    result = 31 * result + baseVersion.hashCode();
    result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
    result = 31 * result + extension.hashCode();
    result = 31 * result + (signatureType != null ? signatureType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "path='" + path + '\'' +
        ", fileName='" + fileName + '\'' +
        ", hashType=" + hashType +
        "groupId='" + groupId + '\'' +
        ", artifactId='" + artifactId + '\'' +
        ", version='" + version + '\'' +
        ", baseVersion='" + baseVersion + '\'' +
        ", classifier='" + classifier + '\'' +
        ", extension='" + extension + '\'' +
        ", signatureType=" + signatureType +
        '}';
  }
}
