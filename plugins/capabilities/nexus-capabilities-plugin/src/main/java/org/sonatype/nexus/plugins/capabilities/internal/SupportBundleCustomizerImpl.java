/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
        new FileContentSourceSupport(Type.CONFIG, "work/conf/" + file.getName(), file)
    );
  }
}
