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
package org.sonatype.nexus.component.metadata.api;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonDeserialize(builder = IdentityMetadata.Builder.class)
public class IdentityMetadata
{
  public static final MetadataIndex INDEX_ARTIFACT = new MetadataIndex.Builder()
      .withColumns("artifact")
      .build();

  public static final MetadataType<IdentityMetadata> TYPE = new MetadataType.Builder<IdentityMetadata>()
      .withName("identity")
      .withMetadataClass(IdentityMetadata.class)
      .withIndexes(INDEX_ARTIFACT)
      .build();

  private final String format;

  private final String group;

  private final String artifact;

  private final String version;

  private IdentityMetadata(String format,
                           @Nullable String group,
                           String artifact,
                           @Nullable String version) {
    this.format = checkNotNull(format);
    this.group = group;
    this.artifact = checkNotNull(artifact);
    this.version = version;
  }

  public String getFormat() {
    return format;
  }

  @Nullable
  public String getGroup() {
    return group;
  }

  public String getArtifact() {
    return artifact;
  }

  @Nullable
  public String getVersion() {
    return version;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final IdentityMetadata other = (IdentityMetadata) obj;
    return Objects.equal(format, other.format)
        && Objects.equal(group, other.group)
        && Objects.equal(artifact, other.artifact)
        && Objects.equal(version, other.version);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(format, group, artifact, version);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("format", format)
        .add("group", group)
        .add("artifact", artifact)
        .add("version", version)
        .toString();
  }

  public static class Builder
      implements MetadataBuilder<IdentityMetadata> {
    private String format;
    private String group;
    private String artifact;
    private String version;

    public Builder() { }

    public Builder(IdentityMetadata md) {
      format = md.getFormat();
      group = md.getGroup();
      artifact = md.getArtifact();
      version = md.getVersion();
    }

    public Builder withFormat(String format) {
      this.format = format;
      return this;
    }

    public Builder withGroup(String group) {
      this.group = group;
      return this;
    }

    public Builder withArtifact(String artifact) {
      this.artifact = artifact;
      return this;
    }

    public Builder withVersion(String version) {
      this.version = version;
      return this;
    }

    @Override
    public IdentityMetadata build() {
      return new IdentityMetadata(format, group, artifact, version);
    }
  }
}
