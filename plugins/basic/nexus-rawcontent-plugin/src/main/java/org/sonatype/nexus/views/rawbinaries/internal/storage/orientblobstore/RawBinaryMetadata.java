package org.sonatype.nexus.views.rawbinaries.internal.storage.orientblobstore;

/**
 * I suppose this is a fledgling prototypical component metadata.
 *
 * @since 3.0
 */
public class RawBinaryMetadata
{
  private String path;

  private String blobId;

  private String mimeType;

  public RawBinaryMetadata(final String path, final String blobId, final String mimeType) {
    this.path = path;
    this.blobId = blobId;
    this.mimeType = mimeType;
  }

  public String getPath() {
    return path;
  }

  public String getBlobId() {
    return blobId;
  }

  public String getMimeType() {
    return mimeType;
  }
}
