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

import org.sonatype.nexus.internal.orient.DatabaseManagerImpl;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.plugins.capabilities.internal.storage.OrientCapabilityStorage;
import org.sonatype.nexus.supportzip.GeneratedContentSourceSupport;
import org.sonatype.nexus.supportzip.SupportBundle;
import org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Type;
import org.sonatype.nexus.supportzip.SupportBundleCustomizer;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Capabilities {@link SupportBundleCustomizer} to include exported capability database in support-zips.
 *
 * @since 2.7
 */
@Named
@Singleton
public class SupportBundleCustomizerImpl
    extends ComponentSupport
    implements SupportBundleCustomizer
{
  private final DatabaseManager databaseManager;

  @Inject
  public SupportBundleCustomizerImpl(final DatabaseManager databaseManager) {
    this.databaseManager = checkNotNull(databaseManager);
  }

  @Override
  public void customize(final SupportBundle supportBundle) {
    String path = String.format("work/%s/%s/%s",
        DatabaseManagerImpl.WORK_PATH,
        OrientCapabilityStorage.DB_NAME,
        DatabaseManager.EXPORT_FILENAME
    );
    supportBundle.add(new GeneratedContentSourceSupport(Type.CONFIG, path)
    {
      {
        // capabilities are critical to system operation, ensure this always gets into the zip
        setPriority(Priority.REQUIRED);
      }

      @Override
      protected void generate(final File file) throws Exception {
        // output non-compressed, no need to double compress contents
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
          databaseManager.export(OrientCapabilityStorage.DB_NAME, out);
        }
      }
    });
  }
}
