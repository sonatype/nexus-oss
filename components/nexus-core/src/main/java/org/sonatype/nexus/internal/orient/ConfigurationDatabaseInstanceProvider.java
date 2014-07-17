package org.sonatype.nexus.internal.orient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Shared {@code configuration} {@link DatabaseInstance} provider.
 *
 * @since 3.0
 */
@Named(ConfigurationDatabaseInstanceProvider.NAME)
@Singleton
public class ConfigurationDatabaseInstanceProvider
  implements Provider<DatabaseInstance>
{
  public static final String NAME = "configuration";

  private final DatabaseManager databaseManager;

  @Inject
  public ConfigurationDatabaseInstanceProvider(final DatabaseManager databaseManager) {
    this.databaseManager = checkNotNull(databaseManager);
  }

  @Override
  public DatabaseInstance get() {
    return databaseManager.instance(NAME);
  }
}
