package org.sonatype.nexus.component.services.internal.id;

import java.util.UUID;

import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.id.ComponentIdFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple, uuid-based factory for {@link ComponentId}s.
 *
 * @since 3.0
 */
public class DefaultComponentIdFactory
    implements ComponentIdFactory
{
  @Override
  public ComponentId newId() {
    return new DefaultComponentId(UUID.randomUUID().toString());
  }

  @Override
  public ComponentId fromUniqueString(final String uniqueString) {
    return new DefaultComponentId(uniqueString);
  }

  private static class DefaultComponentId
      implements ComponentId
  {
    private final String uniqueString;

    private DefaultComponentId(final String uniqueString) {
      this.uniqueString = checkNotNull(uniqueString);
    }

    @Override
    public String asUniqueString() {
      return uniqueString;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      DefaultComponentId that = (DefaultComponentId) o;

      return uniqueString.equals(that.uniqueString);
    }

    @Override
    public int hashCode() {
      return uniqueString.hashCode();
    }

    @Override
    public String toString() {
      return ComponentId.class.getSimpleName() + "[" + uniqueString + "]";
    }
  }
}
