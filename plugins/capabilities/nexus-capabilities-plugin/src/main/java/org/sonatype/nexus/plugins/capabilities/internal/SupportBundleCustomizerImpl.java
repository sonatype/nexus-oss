/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.atlas.GeneratedContentSourceSupport;
import org.sonatype.nexus.atlas.SupportBundle;
import org.sonatype.nexus.atlas.SupportBundle.ContentSource.Type;
import org.sonatype.nexus.atlas.SupportBundleCustomizer;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.io.xpp3.NexusCapabilitiesConfigurationXpp3Writer;
import org.sonatype.nexus.plugins.capabilities.internal.storage.CapabilityStorageConverter;
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
  private final CapabilityStorageConverter converter;

  @Inject
  public SupportBundleCustomizerImpl(final CapabilityStorageConverter converter) {
    this.converter = checkNotNull(converter);
  }

  /**
   * Customize the given bundle, adding one or more content sources.
   */
  @Override
  public void customize(final SupportBundle supportBundle) {
    // TODO : replace bellow with direct export from for Kazuki/H2 store
    // for now we generate an capabilities.xml out of kazuki
    supportBundle.add(new GeneratedContentSourceSupport(Type.CONFIG, "work/conf/capabilities.xml")
    {
      @Override
      protected void generate(final File file) throws Exception {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
          new NexusCapabilitiesConfigurationXpp3Writer().write(out, converter.convertFromKazuki());
        }
      }
    });
  }

}
