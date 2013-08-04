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

package org.sonatype.nexus.error.reporting;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.sisu.pr.SystemEnvironmentContributor;

/**
 * Additional system environment info (Nexus version + edition)
 *
 * @since 2.1
 */
@Named("nexus")
public class NexusSystemEnvironment
    implements SystemEnvironmentContributor
{
  private static String LINE_SEPERATOR = System.getProperty("line.separator");

  ApplicationStatusSource applicationStatus;

  @Inject
  public NexusSystemEnvironment(final ApplicationStatusSource applicationStatus) {
    this.applicationStatus = applicationStatus;
  }

  @Override
  public String asDiagnosticsFormat() {
    StringBuilder sb = new StringBuilder();
    sb.append("Nexus Version: ");
    sb.append(applicationStatus.getSystemStatus().getVersion());
    sb.append(LINE_SEPERATOR);

    sb.append("Nexus Edition: ");
    sb.append(applicationStatus.getSystemStatus().getEditionLong());
    sb.append(LINE_SEPERATOR);

    return sb.toString();
  }

}
