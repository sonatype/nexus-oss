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
package org.sonatype.nexus.integrationtests;

import java.io.File;

import org.sonatype.nexus.test.utils.TestProperties;

import com.google.common.base.Preconditions;

/**
 * A simple descriptor object describing the maven runtime and maven project you want to run Verifier against.
 *
 * @since 2.1
 *
 * @deprecated Only used by {@link MavenVerifierHelper}, avoid any other use.
 */
@Deprecated
class MavenDeployment
{
  private final File mavenHomeFile;

  private final File localRepositoryFile;

  private final String logFileName;

  private final File settingsXmlFile;

  private final File mavenProjectFile;

  public MavenDeployment(final File mavenHomeFile, final File localRepositoryFile, final String logFileName,
                         final File settingsXmlFile, final File mavenProjectFile)
  {
    this.mavenHomeFile = Preconditions.checkNotNull(mavenHomeFile);
    this.localRepositoryFile = Preconditions.checkNotNull(localRepositoryFile);
    this.logFileName = Preconditions.checkNotNull(logFileName);
    this.settingsXmlFile = Preconditions.checkNotNull(settingsXmlFile);
    this.mavenProjectFile = Preconditions.checkNotNull(mavenProjectFile);
  }

  public File getMavenHomeFile() {
    return mavenHomeFile;
  }

  public File getLocalRepositoryFile() {
    return localRepositoryFile;
  }

  public String getLogFileName() {
    return logFileName;
  }

  public File getSettingsXmlFile() {
    return settingsXmlFile;
  }

  public File getMavenProjectFile() {
    return mavenProjectFile;
  }

  public static MavenDeployment defaultDeployment(final String logFileName, final File settingsXmlFile,
                                                  final File mavenProject)
  {
    return new MavenDeployment(new File(TestProperties.getString("maven.instance")), new File(
        TestProperties.getString("maven.local.repo")), logFileName, settingsXmlFile, mavenProject);
  }
}
