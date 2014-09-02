package org.sonatype.nexus.views.rawbinaries.internal.storage.orientblobstore;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An identifier for {@link RawBinaryMetadata} in conjunction with {@link OrientRawBinaryStore}
 *
 * @since 3.0
 */
public class RawBinaryMetadataIdentity
{
  private final String value;

  public RawBinaryMetadataIdentity(final String value) {
    this.value = checkNotNull(value);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RawBinaryMetadataIdentity that = (RawBinaryMetadataIdentity) o;

    if (!value.equals(that.value)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}
