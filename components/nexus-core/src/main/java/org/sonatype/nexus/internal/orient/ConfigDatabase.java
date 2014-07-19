package org.sonatype.nexus.internal.orient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.DatabaseExternalizer;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.supportzip.GeneratedContentSourceSupport;
import org.sonatype.nexus.supportzip.SupportBundle;
import org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Priority;
import org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Type;
import org.sonatype.nexus.supportzip.SupportBundleCustomizer;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Shared {@code config} database components.
 *
 * @since 3.0
 */
public class ConfigDatabase
{
  public static final String NAME = "config";

  /**
   * Instance provider.
   */
  @Named(NAME)
  @Singleton
  public static class ProviderImpl
      implements Provider<DatabaseInstance>
  {
    private final DatabaseManager databaseManager;

    @Inject
    public ProviderImpl(final DatabaseManager databaseManager) {
      this.databaseManager = checkNotNull(databaseManager);
    }

    @Override
    public DatabaseInstance get() {
      return databaseManager.instance(NAME);
    }
  }

  /**
   * Includes export of the database in support-zip.
   */
  @Named
  @Singleton
  public static class SupportBundleCustomizerImpl
      extends ComponentSupport
      implements SupportBundleCustomizer
  {
    private final Provider<DatabaseInstance> databaseInstance;

    @Inject
    public SupportBundleCustomizerImpl(final @Named(NAME) Provider<DatabaseInstance> databaseInstance) {
      this.databaseInstance = checkNotNull(databaseInstance);
    }

    @Override
    public void customize(final SupportBundle supportBundle) {
      String path = String.format("work/%s/%s/%s",
          DatabaseManagerImpl.WORK_PATH,
          databaseInstance.get().getName(),
          DatabaseExternalizer.EXPORT_FILENAME
      );

      supportBundle.add(new GeneratedContentSourceSupport(Type.CONFIG, path, Priority.REQUIRED)
      {
        @Override
        protected void generate(final File file) throws Exception {
          // output non-compressed, no need to double compress contents
          try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            DatabaseExternalizer externalizer = databaseInstance.get().externalizer();
            externalizer.export(output);
          }
        }
      });
    }
  }
}
