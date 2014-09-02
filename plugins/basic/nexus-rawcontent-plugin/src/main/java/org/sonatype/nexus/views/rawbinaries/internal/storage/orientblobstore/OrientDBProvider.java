package org.sonatype.nexus.views.rawbinaries.internal.storage.orientblobstore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named(OrientDBProvider.NAME)
@Singleton
public class OrientDBProvider
    implements Provider<DatabaseInstance>
{
  public static final String NAME = "rawBinaryMetadata";

  private final DatabaseManager databaseManager;

  @Inject
  public OrientDBProvider(final DatabaseManager databaseManager) {
    this.databaseManager = checkNotNull(databaseManager);
  }

  @Override
  public DatabaseInstance get() {
    return databaseManager.instance(NAME);
  }
}
