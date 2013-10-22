package org.sonatype.nexus.plugins.capabilities.internal;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.atlas.FileContentSourceSupport;
import org.sonatype.nexus.atlas.SupportBundle;
import org.sonatype.nexus.atlas.SupportBundle.ContentSource.Type;
import org.sonatype.nexus.atlas.SupportBundleCustomizer;
import org.sonatype.nexus.plugins.capabilities.internal.storage.DefaultCapabilityStorage;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Capabilities {@link SupportBundleCustomizer}.
 *
 * @since 2.7
 */
@Named
@Singleton
public class SupportBundleCustomizerImpl
    extends ComponentSupport
    implements SupportBundleCustomizer
{
  private final DefaultCapabilityStorage capabilityStorage;

  @Inject
  public SupportBundleCustomizerImpl(final DefaultCapabilityStorage capabilityStorage) {
    this.capabilityStorage = checkNotNull(capabilityStorage);
  }

  /**
   * Customize the given bundle, adding one or more content sources.
   */
  @Override
  public void customize(final SupportBundle supportBundle) {
    File file = capabilityStorage.getConfigurationFile();
    if (!file.exists()) {
      log.debug("skipping non-existent file: {}", file);
    }

    // capabilities.xml
    supportBundle.add(
        new FileContentSourceSupport(Type.CONFIG, "work/conf/capabilities.xml", file)
    );
  }
}
