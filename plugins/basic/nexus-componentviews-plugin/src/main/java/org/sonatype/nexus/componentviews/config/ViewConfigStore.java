package org.sonatype.nexus.componentviews.config;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

/**
 * @since 3.0
 */
public interface ViewConfigStore
    extends Lifecycle
{
  /**
   * Adds a config
   *
   * @param item to be added
   * @throws IOException IOException If any problem encountered while read/store of config storage
   */
  public ViewConfigIdentity add(ViewConfig item)
      throws IOException;

  /**
   * Updates stored config if exists.
   *
   * @param item to be updated
   * @throws IOException If any problem encountered while read/store of config storage
   */
  public void update(ViewConfigIdentity id, ViewConfig item)
      throws IOException;

  /**
   * Deletes stored config if exists.
   *
   * @param id of config to be deleted
   * @return false if config to be deleted does not exist in storage, true otherwise
   * @throws IOException If any problem encountered while read/store of config storage
   */
  public void remove(ViewConfigIdentity id)
      throws IOException;

  /**
   * Retrieves stored configs.
   *
   * @return configs (never null)
   * @throws IOException If any problem encountered while read/store of config storage
   */
  public Map<ViewConfigIdentity, ViewConfig> getAll()
      throws IOException;


  /**
   * Retrieves a config based on its view name.
   *
   * @return null if there's no view by that name.
   */
  @Nullable
  ViewConfig get(String viewName) throws IOException;
}
