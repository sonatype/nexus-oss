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
package org.sonatype.nexus.blobstore.file;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.configuration.application.ApplicationDirectoriesImpl;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * A simple Guice module that hooks the test up to temp directories.
 */
public class TempDirectoryModule
    extends AbstractModule
{
  @Override
  protected void configure() {
  }

  @Provides
  public ApplicationDirectories applicationDirectories() {
    return new ApplicationDirectoriesImpl(Files.createTempDir(), Files.createTempDir(), Files.createTempDir());
  }
}
