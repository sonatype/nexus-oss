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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonDeserialize(builder = ContentMetadata.Builder.class)
public class ContentMetadata
    extends SecondaryMetadata
{
  public static final MetadataIndex INDEX_IDENTITYKEY = new MetadataIndex.Builder()
      .withColumns("identityKey")
      .build();

  public static final MetadataType TYPE = new MetadataType.Builder()
      .withName("content")
      .withMetadataClass(ContentMetadata.class)
      .withIndexes(INDEX_IDENTITYKEY)
      .build();

  private final List<String> blobRefs;

  private final String mimeType;

  private final Long size;

  private final Map<String, String> checksums;

  private final String path;

  private ContentMetadata(String identityKey,
                          List<String> blobRefs,
                          String mimeType,
                          Long size,
                          Map<String, String> checksums,
                          String path) {
    super(identityKey);
    this.blobRefs = checkNotNull(blobRefs);
    this.mimeType = checkNotNull(mimeType);
    this.size = checkNotNull(size);
    this.checksums = checkNotNull(checksums);
    this.path = checkNotNull(path);
  }

  public List<String> getBlobRefs() {
    return blobRefs;
  }

  public String getMimeType() {
    return mimeType;
  }

  public Long getSize() {
    return size;
  }

  public Map<String, String> getChecksums() {
    return checksums;
  }

  public String getPath() {
    return path;
  }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final ContentMetadata other = (ContentMetadata) obj;
      return Objects.equal(identityKey, other.identityKey)
        && Objects.equal(blobRefs, other.blobRefs)
        && Objects.equal(mimeType, other.mimeType)
        && Objects.equal(size, other.size)
        && Objects.equal(checksums, other.checksums)
        && Objects.equal(path, other.path);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(identityKey, blobRefs, mimeType, size, checksums, path);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("identityKey", identityKey)
        .add("blobRefs", blobRefs)
        .add("mimeType", mimeType)
        .add("size", size)
        .add("checksums", checksums)
        .add("path", path)
        .toString();
  }

  public static class Builder implements MetadataBuilder<ContentMetadata> {
    private String identityKey;
    private List<String> blobRefs;
    private String mimeType;
    private Long size;
    private Map<String, String> checksums;
    private String path;

    public Builder() { }

    public Builder(ContentMetadata md) {
      identityKey = md.getIdentityKey();
      blobRefs = md.getBlobRefs();
      mimeType = md.getMimeType();
      size = md.getSize();
      checksums = md.getChecksums();
      path = md.getPath();
    }

    public Builder withIdentityKey(String identityKey) {
      this.identityKey = identityKey;
      return this;
    }

    public Builder withBlobRefs(List<String> blobRefs) {
      this.blobRefs = blobRefs;
      return this;
    }

    public Builder withMimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public Builder withSize(Long size) {
      this.size = size;
      return this;
    }

    public Builder withChecksums(Map<String, String> checksums) {
      this.checksums = checksums;
      return this;
    }

    public Builder withPath(String path) {
      this.path = path;
      return this;
    }

    @Override
    public ContentMetadata build() {
      return new ContentMetadata(identityKey, blobRefs, mimeType, size, checksums, path);
    }
  }
}
