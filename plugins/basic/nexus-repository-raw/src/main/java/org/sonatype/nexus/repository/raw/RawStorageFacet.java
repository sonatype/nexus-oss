package org.sonatype.nexus.repository.raw;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;

/**
 * Provides persistent storage for {@link RawContent}.
 *
 * @since 3.0
 */
public interface RawStorageFacet
    extends Facet
{
  @Nullable
  RawContent get(String path) throws IOException;

  @Nullable
  RawContent put(String path, RawContent content) throws IOException;

  @Nullable
  boolean delete(String path) throws IOException;
}
