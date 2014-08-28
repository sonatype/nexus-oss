package org.sonatype.nexus.views.rawbinaries;

import java.io.InputStream;
import java.util.List;

import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

/**
 * If 'RawBinary' is a repository format, then this is an interface that represents the operations defined by the
 * protocol, maps them to component metadata and artifact storage operations.
 *
 * The real purpose of this class is just to provide an example of binary-oriented use of the view framework, however,
 * it does raise some issues:
 *
 * @since 3.0
 */
public interface RawBinaryStore
    extends Lifecycle
{
  List<RawBinary> getForPath(String prefix);

  /**
   * Returns false if the creation was disallowed because another binary already exists at that path.
   */
  boolean create(String path, String mimeType, InputStream inputStream);

  /**
   * Returns true if an artifact was deleted, false if there was no artifact to delete.
   */
  boolean delete(String path);
}
