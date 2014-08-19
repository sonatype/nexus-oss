package org.sonatype.nexus.componentviews.config;

import java.util.Collection;
import java.util.Iterator;

/**
 * @since 3.0
 */
public interface UserSuppliedViewConfigStore
{
  UserSuppliedViewConfig get(String viewName);

  Collection<UserSuppliedViewConfig> getAll();

  void save(UserSuppliedViewConfig config);
}
