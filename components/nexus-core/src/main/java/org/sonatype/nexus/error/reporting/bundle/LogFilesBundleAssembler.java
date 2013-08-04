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

package org.sonatype.nexus.error.reporting.bundle;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.FileBundle;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;

/**
 * Adds nexus.log to the error report bundle.
 */
@Named("nexus.log")
public class LogFilesBundleAssembler
    implements BundleAssembler
{

  private NexusConfiguration nexusConfig;

  @Inject
  public LogFilesBundleAssembler(final NexusConfiguration nexusConfig) {
    this.nexusConfig = nexusConfig;
  }

  @Override
  public boolean isParticipating(IssueSubmissionRequest request) {
    return new File(nexusConfig.getWorkingDirectory("logs"), "nexus.log").exists();
  }

  @Override
  public Bundle assemble(IssueSubmissionRequest request)
      throws IssueSubmissionException
  {
    return new FileBundle(new File(nexusConfig.getWorkingDirectory("logs"), "nexus.log"));
  }

}
