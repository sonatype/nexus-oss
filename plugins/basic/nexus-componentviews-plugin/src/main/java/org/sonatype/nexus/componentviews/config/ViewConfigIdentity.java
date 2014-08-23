package org.sonatype.nexus.componentviews.config;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An identifier for {@link ViewConfig} in conjunction with {@link ViewConfigStore}
 *
 * @since 3.0
 */
public class ViewConfigIdentity
{
  private final String value;

  public ViewConfigIdentity(final String value) {
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

    ViewConfigIdentity that = (ViewConfigIdentity) o;

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
