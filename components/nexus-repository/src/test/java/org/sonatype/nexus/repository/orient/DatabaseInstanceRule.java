package org.sonatype.nexus.repository.orient;

import java.util.List;

import javax.inject.Provider;

import org.sonatype.nexus.internal.orient.MemoryDatabaseManager;
import org.sonatype.nexus.internal.orient.MinimalDatabaseServer;
import org.sonatype.nexus.orient.DatabaseInstance;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import org.junit.rules.ExternalResource;
import org.junit.runners.model.MultipleFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * JUnit rule to provide {@link DatabaseInstance} and related components.
 */
public class DatabaseInstanceRule
  extends ExternalResource
{
  private static final Logger log = LoggerFactory.getLogger(DatabaseInstanceRule.class);

  private final String name;

  private MinimalDatabaseServer server;

  private MemoryDatabaseManager manager;

  private DatabaseInstance instance;

  public DatabaseInstanceRule(final String name) {
    this.name = checkNotNull(name);
  }

  public MinimalDatabaseServer getServer() {
    checkState(server != null);
    return server;
  }

  public MemoryDatabaseManager getManager() {
    checkState(manager != null);
    return manager;
  }

  public DatabaseInstance getInstance() {
    checkState(instance != null);
    return instance;
  }

  public Provider<DatabaseInstance> getInstanceProvider() {
    return Providers.of(getInstance());
  }

  @Override
  protected void before() throws Throwable {
    log.info("Preparing database instance: {}", name);

    server = new MinimalDatabaseServer();
    server.start();

    manager = new MemoryDatabaseManager();
    manager.start();

    instance = manager.instance(name);

    log.info("Database instance prepared");
  }

  @Override
  protected void after() {
    log.info("Cleaning up database instance: {}", name);

    instance = null;

    // capture any errors so we can propagate to test harness
    List<Throwable> errors = Lists.newArrayListWithCapacity(2);

    if (manager != null) {
      try {
        manager.stop();
      }
      catch (Exception e) {
        log.error("Failed to stop database manager", e);
        errors.add(e);
      }
      manager = null;
    }

    if (server != null) {
      try {
        server.stop();
      }
      catch (Exception e) {
        log.error("Failed to stop database server", e);
        errors.add(e);
      }
      server = null;
    }

    if (!errors.isEmpty()) {
      log.error("Failed to clean up database instance");
      throw Throwables.propagate(new MultipleFailureException(errors));
    }

    log.info("Database instance cleaned up");
  }
}
