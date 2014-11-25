package org.sonatype.nexus.component.source;

import java.util.Map;

/**
 * A container for asset content, as returned by {@link ComponentSource#fetchComponents(ComponentRequest)}.
 *
 * @since 3.0
 */
public interface ComponentResponse
{
  /**
   * @return the component-level metadata available from the response
   */
  Map<String, Object> getMetadata();

  /**
   * @return any assets returned as part of the response
   */
  Iterable<AssetResponse> getAssets();
}
