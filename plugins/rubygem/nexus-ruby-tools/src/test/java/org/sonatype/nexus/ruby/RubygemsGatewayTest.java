/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RubygemsGatewayTest
    extends TestSupport
{
  @Rule
  public TestJRubyContainerRule testJRubyContainerRule = new TestJRubyContainerRule();

  @Test
  public void testGenerateGemspecRz() throws Exception {
    String gem = "src/test/resources/gems/n/nexus-0.1.0.gem";

    GemspecHelper spec;
    try (InputStream is = new FileInputStream(gem)) {
      spec = testJRubyContainerRule.getRubygemsGateway().newGemspecHelperFromGem(is);
    }

    File gemspecPath = new File("target/nexus-0.1.0.gemspec.rz");
    try (InputStream is = spec.getRzInputStream()) {
      dumpStream(is, gemspecPath);
    }

    assertThat("generated gemspec matches reference", FileUtils.readFileToByteArray(gemspecPath), 
        equalTo(FileUtils.readFileToByteArray(new File("src/test/resources/nexus-0.1.0.gemspec.rz"))));
  }

  @Test
  public void testPom() throws Exception {
    File some = new File("src/test/resources/rb-fsevent-0.9.4.gemspec.rz");

    String pom;
    try (InputStream is = new FileInputStream(some)) {
      pom = testJRubyContainerRule.getRubygemsGateway().newGemspecHelper(is).pom(false);
    }
    assertThat(pom.replace("\n", "").replaceAll("<developers>.*$", "").replaceAll("^.*<name>|</name>.*$", ""),
        equalTo("Very simple &amp; usable FSEvents API"));
  }

  @Test
  public void testEmptyDependencies() throws Exception {
    File empty = new File("target/empty");

    // create empty dependencies file
    DependencyHelper deps = testJRubyContainerRule.getRubygemsGateway().newDependencyHelper();
    try (InputStream is = deps.getInputStream(false)) {
      dumpStream(is, empty);
    }

    assertThat("spec from stream equal spec from gem", FileUtils.readFileToByteArray(empty), 
        equalTo(FileUtils.readFileToByteArray(new File("src/test/resources/empty_dependencies"))));
  }

  private void dumpStream(final InputStream is, File target)
      throws IOException
  {
    try (FileOutputStream output = new FileOutputStream(target)) {
      IOUtils.copy(is, output);
    }
  }
}
