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
 * Maven repository artifact path, a path that conforms Maven repository layout and can be "reverse engineered" into
 * artifact GAV coordinates. Every artifact in repository have Maven GAV, may have hashes or
 * signatures.
 *
 * @see Path
 * @since 3.0
 */
@Immutable
public class ArtifactPath
    extends Path
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

  private final boolean snapshot;

  private final String groupId;

  private final String artifactId;

  private final String version;

  private final String baseVersion;

  private final String classifier;

  private final String extension;

  private final SignatureType signatureType;

  public ArtifactPath(final String path,
                      final boolean snapshot,
                      final String groupId,
                      final String artifactId,
                      final String version,
                      final String baseVersion,
                      final @Nullable String classifier,
                      final String extension,
                      final @Nullable SignatureType signatureType)
  {
    super(path);
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

  /**
   * Returns {@code true} if this path is subordinate of another path.
   *
   * @see {@link #subordinateOf()}
   */
  @Override
  public boolean isSubordinate() {
    return isSignature() || super.isSubordinate();
  }

  /**
   * Returns {@code true} if this path represents a signature.
   */
  public boolean isSignature() {
    return signatureType != null;
  }

  /**
   * Returns the "main" non-subordinate path of this path. The "main" path is not a hash, nor a
   * signature, nor a hash of a signature.
   */
  @Override
  public ArtifactPath main() {
    return (ArtifactPath) super.main();
  }

  /**
   * Returns the "parent" path, that this path is subordinate of, or {@code null}.
   */
  public ArtifactPath subordinateOf() {
    if (isHash()) {
      int hashSuffixLen = hashType.getExt().length() + 1; // the dot
      return new ArtifactPath(
          path.substring(0, path.length() - hashSuffixLen),
          snapshot,
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
      return new ArtifactPath(
          path.substring(0, path.length() - signatureSuffixLen),
          snapshot,
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
   * Returns path of passed in hash type that is subordinate of this path. This path cannot be hash.
   */
  @Override
  public ArtifactPath hash(final HashType hashType) {
    checkNotNull(hashType);
    checkArgument(!isHash(), "This path is already a hash: " + this);
    return new ArtifactPath(
        path + "." + hashType.getExt(),
        snapshot,
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
   * Returns path of passed in signature type that is subordinate of this path. This path cannot be
   * hash nor signature.
   */
  public ArtifactPath signature(final SignatureType signatureType) {
    checkNotNull(signatureType);
    checkArgument(!isHash(), "This path is already a hash: " + this);
    checkArgument(!isSignature(), "This path is already a signature: " + this);
    return new ArtifactPath(
        path + "." + signatureType.getExt(),
        snapshot,
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
    if (!(o instanceof ArtifactPath)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ArtifactPath that = (ArtifactPath) o;

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
        ", snapshot=" + snapshot +
        ", groupId='" + groupId + '\'' +
        ", artifactId='" + artifactId + '\'' +
        ", version='" + version + '\'' +
        ", baseVersion='" + baseVersion + '\'' +
        ", classifier='" + classifier + '\'' +
        ", extension='" + extension + '\'' +
        ", signatureType=" + signatureType +
        '}';
  }
}
