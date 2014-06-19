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

import static com.google.common.base.Preconditions.checkNotNull;

@JsonDeserialize(builder = ContentMetadata.Builder.class)
public class ContentMetadata
    implements SecondaryMetadata
{
  public static final MetadataIndex INDEX_IDENTITYKEY = new MetadataIndex.Builder()
      .withColumns("identityKey")
      .build();

  public static final MetadataType type = new MetadataType.Builder()
      .withName("content")
      .withMetadataClass(ContentMetadata.class)
      .withIndexes(INDEX_IDENTITYKEY)
      .build();

  private final String identityKey;

  private final List<String> blobRefs;

  private final String mimeType;

  private final long size;

  private final Map<String, String> checksums;

  private final String path;

  private ContentMetadata(String identityKey,
                          List<String> blobRefs,
                          String mimeType,
                          long size,
                          Map<String, String> checksums,
                          String path) {
    this.identityKey = checkNotNull(identityKey);
    this.blobRefs = checkNotNull(blobRefs);
    this.mimeType = checkNotNull(mimeType);
    this.size = size;
    this.checksums = checksums;
    this.path = path;
  }

  @Override
  public String getIdentityKey() {
    return identityKey;
  }

  public List<String> getBlobRefs() {
    return blobRefs;
  }

  public String getMimeType() {
    return mimeType;
  }

  public long getSize() {
    return size;
  }

  public Map<String, String> getChecksums() {
    return checksums;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "identityKey=" + identityKey +
        ", blobRefs=" + blobRefs +
        ", mimeType=" + mimeType +
        ", size=" + size +
        ", checksums=" + checksums +
        ", path=" + path +
        "}";
  }

  public static class Builder {
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

    public ContentMetadata build() {
      return new ContentMetadata(identityKey, blobRefs, mimeType, size, checksums, path);
    }
  }
}
