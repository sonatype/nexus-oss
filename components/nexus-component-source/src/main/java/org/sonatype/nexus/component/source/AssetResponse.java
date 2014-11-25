package org.sonatype.nexus.component.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

/**
 * Asset content returned by a component source.
 *
 * @since 3.0
 */
public interface AssetResponse
{
  /**
   * @return length of this asset in bytes, {@code -1} if unknown
   */
  long getContentLength();

  /**
   * @return content type of this asset, {@code null} if unknown
   */
  @Nullable
  String getContentType();

  /**
   * @return when this asset's content was last modified, {@code null} if unknown
   */
  @Nullable
  DateTime getLastModified();

  /**
   * @return whatever metadata is available, depending on the format.
   */
  Map<String, Object> getMetadata();

  /**
   * @return input stream for reading the content of this asset.
   */
  InputStream openStream() throws IOException;
}
