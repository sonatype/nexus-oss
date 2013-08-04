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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.error.report.ErrorReportBundleContentContributor;
import org.sonatype.nexus.error.report.ErrorReportBundleEntry;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.BundleAssembler;
import org.sonatype.sisu.pr.bundle.DirBundle;
import org.sonatype.sisu.pr.bundle.ManagedBundle;
import org.sonatype.sisu.pr.bundle.StorageManager;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.util.IOUtil;

/**
 * Adds the content of legacy {@link ErrorReportBundleContentContributor} instances to the error report bundle.
 */
@Named("extra-content")
public class LegacyBundleContributorsAssembler
    implements BundleAssembler
{

  private Map<String, ErrorReportBundleContentContributor> bundleExtraContent;

  private StorageManager storageManager;

  @Inject
  public LegacyBundleContributorsAssembler(
      final Map<String, ErrorReportBundleContentContributor> bundleExtraContent, final StorageManager storageManager)
  {
    this.bundleExtraContent = bundleExtraContent;
    this.storageManager = storageManager;
  }

  @Override
  public boolean isParticipating(IssueSubmissionRequest request) {
    return !bundleExtraContent.isEmpty();
  }

  @Override
  public Bundle assemble(IssueSubmissionRequest request)
      throws IssueSubmissionException
  {
    DirBundle main = new DirBundle("extra");

    try {
      Set<Entry<String, ErrorReportBundleContentContributor>> bundleExtraContent = this.bundleExtraContent.entrySet();
      for (Entry<String, ErrorReportBundleContentContributor> extraContent : bundleExtraContent) {
        DirBundle dir = new DirBundle(extraContent.getKey());
        ErrorReportBundleEntry[] entries = extraContent.getValue().getEntries();
        for (ErrorReportBundleEntry errorReportBundleEntry : entries) {
          String entryName = errorReportBundleEntry.getEntryName();
          InputStream content = errorReportBundleEntry.getContent();

          ManagedBundle bundle = storageManager.createBundle(entryName, "application/octet-stream");
          dir.addSubBundle(bundle);

          OutputStream out = bundle.getOutputStream();
          IOUtil.copy(content, out);
          out.close();

          errorReportBundleEntry.releaseEntry();
        }
        main.addSubBundle(dir);
      }
    }
    catch (IOException e) {
      throw new IssueSubmissionException("Could not assemble extra bundle contributions: " + e.getMessage(), e);
    }

    return main;
  }

}
