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

package org.sonatype.nexus.testsuite.ruby;

import java.io.IOException;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.client.core.condition.NexusStatusConditions.any27AndLater;

public class DownloadsOnEmptyRepositoriesIT
    extends RubyITSupport
{
  public DownloadsOnEmptyRepositoriesIT(String repoId) {
    super(repoId);
  }

  @Test
  public void download() throws Exception {
    download("gemshost");
    download("gemsproxy");
    download("gemshostgroup");
    download("gemsproxygroup");
    download("gemsgroup");
  }

  private void download(String repoId) throws Exception {
    log("== START {}", repoId);
    assertAllSpecsIndexDownload(repoId);
    // on an empty repo these directories still missing
    assertFileDownload(repoId, "/gems", is(true));
    assertFileDownload(repoId, "/quick", is(true));
    assertFileDownload(repoId, "/api", is(true));
    assertFileDownload(repoId, "/maven", is(true));
    log("== END {}", repoId);
  }

  private void assertAllSpecsIndexDownload(String repoId) throws IOException {
    assertSpecsIndexdownload(repoId, "specs");
    assertSpecsIndexdownload(repoId, "prerelease_specs");
    assertSpecsIndexdownload(repoId, "latest_specs");
  }

  private void assertSpecsIndexdownload(String repoId, String name) throws IOException {
    if (any27AndLater().isSatisfiedBy(client().getNexusStatus())) {
      // skip this test for 2.6.x nexus :
      // something goes wrong but that is a formal feature not used by any ruby client
      assertFileDownload(repoId, "/" + name + ".4.8", is(true));
    }
    assertFileDownload(repoId, "/" + name + ".4.8.gz", is(true));
  }
}