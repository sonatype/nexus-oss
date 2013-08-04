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

package org.sonatype.nexus.testsuite.p2.meclipse1299;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.testsuite.p2.AbstractNexusProxyP2IT;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

public class MECLIPSE1299P2CompositeRepoMergeRulesIT
    extends AbstractNexusProxyP2IT
{

  public MECLIPSE1299P2CompositeRepoMergeRulesIT() {
    super("meclipse1299");
  }

  @Test
  public void test()
      throws Exception
  {
    final File artifactsXmlFile = new File("target/downloads/meclipse1299/artifacts.xml");
    if (artifactsXmlFile.exists()) {
      assertThat(artifactsXmlFile.delete(), is(true));
    }

    downloadFile(
        new URL(getRepositoryUrl(getTestRepositoryId()) + "/artifacts.xml"),
        artifactsXmlFile.getAbsolutePath()
    );
    assertThat(artifactsXmlFile, exists());

    assertThat(artifactsXmlFile, contains(
        "<mappings size=\"5\">",
        "<rule output=\"${repoUrl}/plugins/${id}_${version}.jar\" filter=\"(&amp; (classifier=osgi.bundle))\" />",
        "<rule output=\"${repoUrl}/binary/${id}_${version}\" filter=\"(&amp; (classifier=binary))\" />",
        "<rule output=\"${repoUrl}/features/${id}_${version}.jar\" filter=\"(&amp; (classifier=org.eclipse.update.feature))\" />",
        "<rule output=\"foo.bar\" filter=\"(&amp; (classifier=foo))\" />",
        "<rule output=\"bar.foo\" filter=\"(&amp; (classifier=bar))\" />"
    ));
  }

}
