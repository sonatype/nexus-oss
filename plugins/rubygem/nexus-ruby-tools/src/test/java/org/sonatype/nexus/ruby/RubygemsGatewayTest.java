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
package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.sonatype.nexus.ruby.DependencyHelper;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.commons.io.IOUtils;
import org.jruby.embed.PathType;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class RubygemsGatewayTest
    extends TestSupport
{
  @Rule
  public TestScriptingContainerRule testScriptingContainerRule = new TestScriptingContainerRule();

  private RubygemsGateway gateway;

  private IRubyObject check;

  @Before
  public void setUp() throws Exception {
    // share the TestSCriptingContainer over all tests to have a uniform ENV setup
    gateway = new DefaultRubygemsGateway(testScriptingContainerRule.get());
    check = testScriptingContainerRule.get().parse(PathType.CLASSPATH, "nexus/check.rb").run();
  }

  @Test
  public void testGenerateGemspecRz() throws Exception {
    String gem = "src/test/resources/gems/n/nexus-0.1.0.gem";

    GemspecHelper spec = gateway.newGemspecHelperFromGem(new FileInputStream(gem));
    InputStream is = spec.getRzInputStream();
    int c = is.read();
    String gemspecPath = "target/nexus-0.1.0.gemspec.rz";
    FileOutputStream out = new FileOutputStream(gemspecPath);
    while (c != -1) {
      out.write(c);
      c = is.read();
    }
    out.close();
    is.close();

    boolean equalSpecs = testScriptingContainerRule.get().callMethod(check,
        "check_gemspec_rz",
        new Object[]{gem, gemspecPath},
        Boolean.class);
    assertThat("spec from stream equal spec from gem", equalSpecs, equalTo(true));
  }

  @Test
  public void testListAllVersions() throws Exception {
    File some = new File("src/test/resources/some_specs");

    List<String> versions = gateway.listAllVersions("bla_does_not_exist",
        new FileInputStream(some),
        0,
        false);
    assertThat("versions size", versions, hasSize(0));

    versions = gateway.listAllVersions("activerecord",
        new FileInputStream(some),
        0,
        false);
    assertThat("versions size", versions, hasSize(1));
    assertThat("version", versions.get(0), equalTo("3.2.11-ruby"));
  }

  @Test
  public void testPom() throws Exception {
    File some = new File("src/test/resources/rb-fsevent-0.9.4.gemspec.rz");

    String pom = gateway.newGemspecHelper(new FileInputStream(some)).pom(false);
    assertThat(pom.replace("\n", "").replaceAll("<developers>.*$", "").replaceAll("^.*<name>|</name>.*$", ""),
        equalTo("Very simple &amp; usable FSEvents API"));
  }

  @Test
  public void testEmptyDependencies() throws Exception {
    File empty = new File("target/empty");

    // create empty dependencies file
    DependencyHelper deps = gateway.newDependencyHelper();
    dumpStream(deps.getInputStream(false), empty);

    int size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        empty.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(0));
  }

  @Test
  public void testEmptySpecs() throws Exception {
    File empty = new File("target/empty");

    dumpStream(gateway.emptyIndex(), empty);

    int size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        empty.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(0));
  }

  @Test
  public void testAddLatestGemToSpecs() throws Exception {
    File empty = new File("src/test/resources/empty_specs");
    File target = new File("target/test_specs");
    File gem = new File("src/test/resources/gems/n/nexus-0.1.0.gem");

    GemspecHelper spec1 = gateway.newGemspecHelperFromGem(new FileInputStream(gem));

    // add gem
    InputStream is = gateway.addSpec(spec1.gemspec(),
        new FileInputStream(empty),
        SpecsIndexType.LATEST);

    // add another gem with different platform
    gem = new File("src/test/resources/gems/n/nexus-0.1.0-java.gem");
    GemspecHelper specJ = gateway.newGemspecHelperFromGem(new FileInputStream(gem));
    is = gateway.addSpec(specJ.gemspec(), is, SpecsIndexType.LATEST);

    dumpStream(is, target);

    int size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(2));

    // add a gem with newer version
    gem = new File("src/test/resources/gems/n/nexus-0.2.0.gem");
    GemspecHelper spec = gateway.newGemspecHelperFromGem(new FileInputStream(gem));
    is = gateway.addSpec(spec.gemspec(),
        new FileInputStream(target),
        SpecsIndexType.LATEST);

    dumpStream(is, target);

    size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(2));

    // add both the gems with older version
    is = gateway.addSpec(spec1.gemspec(),
        new FileInputStream(target),
        SpecsIndexType.LATEST);
    assertThat("no change", is, nullValue());
    is = gateway.addSpec(specJ.gemspec(),
        new FileInputStream(target),
        SpecsIndexType.LATEST);
    assertThat("no change", is, nullValue());
  }

  @Test
  public void testDeleteLatestGemToSpecs() throws Exception {
    File empty = new File("src/test/resources/empty_specs");
    File target = new File("target/test_specs");
    File targetRef = new File("target/test_ref_specs");
    File gem = new File("src/test/resources/gems/n/nexus-0.1.0.gem");

    GemspecHelper spec = gateway.newGemspecHelperFromGem(new FileInputStream(gem));

    // add gem
    InputStream isRef = gateway.addSpec(spec.gemspec(), new FileInputStream(empty), SpecsIndexType.RELEASE);

    // add another gem with different platform
    gem = new File("src/test/resources/gems/n/nexus-0.1.0-java.gem");
    spec = gateway.newGemspecHelperFromGem(new FileInputStream(gem));
    isRef = gateway.addSpec(spec.gemspec(), isRef, SpecsIndexType.RELEASE);

    dumpStream(isRef, targetRef);

    // add a gem with newer version
    gem = new File("src/test/resources/gems/n/nexus-0.2.0.gem");
    GemspecHelper s = gateway.newGemspecHelperFromGem(new FileInputStream(gem));
    InputStream is = gateway.addSpec(s.gemspec(), new FileInputStream(empty), SpecsIndexType.LATEST);

    is = gateway.deleteSpec(s.gemspec(), is, new FileInputStream(targetRef));

    dumpStream(is, target);

    int size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(2));

    is = gateway.deleteSpec(spec.gemspec(), new FileInputStream(target),
        new FileInputStream(targetRef));

    dumpStream(is, target);

    size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(1));
  }

  @Test
  public void testAddDeleteReleasedGemToSpecs() throws Exception {
    File empty = new File("src/test/resources/empty_specs");
    File target = new File("target/test_specs");
    File gem = new File("src/test/resources/gems/n/nexus-0.1.0.gem");

    GemspecHelper spec = gateway.newGemspecHelperFromGem(new FileInputStream(gem));

    // add released gem
    InputStream is = gateway.addSpec(spec.gemspec(), new FileInputStream(empty), SpecsIndexType.RELEASE);

    dumpStream(is, target);

    int size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(1));

    // delete gem
    is = gateway.deleteSpec(spec.gemspec(), new FileInputStream(target));

    dumpStream(is, target);

    size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);

    assertThat("specsfile size", size, equalTo(0));

    // try adding released gem as prereleased
    is = gateway.addSpec(spec.gemspec(), new FileInputStream(empty), SpecsIndexType.PRERELEASE);

    assertThat("no change", is, nullValue());

    // adding to latest
    is = gateway.addSpec(spec.gemspec(), new FileInputStream(empty), SpecsIndexType.LATEST);

    dumpStream(is, target);

    size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(1));
  }

  @Test
  public void testAddDeletePrereleasedGemToSpecs() throws Exception {
    File empty = new File("src/test/resources/empty_specs");
    File target = new File("target/test_specs");
    File gem = new File("src/test/resources/gems/n/nexus-0.1.0.pre.gem");

    GemspecHelper spec = gateway.newGemspecHelperFromGem(new FileInputStream(gem));

    // add prereleased gem
    InputStream is = gateway.addSpec(spec.gemspec(), new FileInputStream(empty), SpecsIndexType.PRERELEASE);

    dumpStream(is, target);

    int size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(1));

    // delete gem
    is = gateway.deleteSpec(spec.gemspec(), new FileInputStream(target));

    dumpStream(is, target);

    size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);

    assertThat("specsfile size", size, equalTo(0));

    // try adding prereleased gem as released
    is = gateway.addSpec(spec.gemspec(), new FileInputStream(empty), SpecsIndexType.RELEASE);

    assertThat("no change", is, nullValue());

    // adding to latest
    is = gateway.addSpec(spec.gemspec(), new FileInputStream(empty), SpecsIndexType.LATEST);

    dumpStream(is, target);

    size = testScriptingContainerRule.get().callMethod(check,
        "specs_size",
        target.getAbsolutePath(),
        Integer.class);
    assertThat("specsfile size", size, equalTo(1));
  }

  private void dumpStream(final InputStream is, File target)
      throws IOException
  {
    try {
      FileOutputStream output = new FileOutputStream(target);
      try {
        IOUtils.copy(is, output);
      }
      finally {
        IOUtils.closeQuietly(output);
      }
    }
    finally {
      IOUtils.closeQuietly(is);
    }
  }
}
