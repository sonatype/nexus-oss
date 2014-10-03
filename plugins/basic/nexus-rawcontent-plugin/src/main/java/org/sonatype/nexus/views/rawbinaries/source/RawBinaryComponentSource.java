package org.sonatype.nexus.views.rawbinaries.source;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;

/**
 * @since 3.0
 */
public class RawBinaryComponentSource
    implements PullComponentSource
{
  private final ComponentSourceId sourceName;

  private final String urlPrefix;

  public RawBinaryComponentSource(final ComponentSourceId sourceName, final String urlPrefix) {
    this.sourceName = sourceName;
    this.urlPrefix = urlPrefix;
  }

  @Nullable
  @Override
  public <T> Iterable<ComponentEnvelope<T>> fetchComponents(final Map<String, String> query) throws IOException {
    // TODO: Implement

    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public ComponentSourceId getId() {
    return sourceName;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
}
