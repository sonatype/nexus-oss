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
package org.sonatype.nexus.testsuite.npm.publish;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sonatype.nexus.testsuite.npm.NpmITSupport;

import com.bolyuba.nexus.plugin.npm.client.NpmHostedRepository;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * IT for performing "npm publish".
 */
public class PublishIT
    extends NpmITSupport
{
  public PublishIT(String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  private void copyPackageJson(final String name, final String version, final String publishUrl, final File targetDir)
      throws IOException
  {
    final File packageJson = testData().resolveFile("package.json");
    final String packageJsonString = Files.toString(packageJson, Charsets.UTF_8);
    Files.write(
        packageJsonString.replace("${privateRegistryUrl}", publishUrl).replace("${name}", name)
            .replace("${version}", version),
        new File(targetDir, packageJson.getName()), Charsets.UTF_8);
  }

  /**
   * Test exercise the "npm publish" operation with respect the "allow redeploy" setting of the hosted
   * repository.
   */
  @Test
  public void npmCliPublish() throws Exception {
    // create a NPM hosted repository that accept packages (use defaults)
    final NpmHostedRepository privateRegistry = createNpmHostedRepository(testMethodName());

    final File localDirectory = util.createTempDir();
    final File projectDir = util.createTempDir();
    copyPackageJson(testMethodName(), "0.0.1", privateRegistry.contentUri(), projectDir);
    final File npmrc = testData().resolveFile(".npmrc");
    final String cmd = String
        .format("npm --registry %s --cache %s --userconfig %s publish %s",
            privateRegistry.contentUri(),
            localDirectory.getAbsolutePath(),
            npmrc.getAbsolutePath(),
            projectDir.getAbsolutePath());

    // 1st run: simply publish, should be OK
    {
      log("1st run: CMD: {}", cmd);
      final Runtime rt = Runtime.getRuntime();
      final Process npm = rt.exec(cmd);
      final int exitCode = npm.waitFor();

      // Really no clue why npm CLI uses both for non-error output
      final String stdOut = CharStreams.toString(new InputStreamReader(npm.getInputStream(), Charsets.UTF_8));
      final String stdErr = CharStreams.toString(new InputStreamReader(npm.getErrorStream(), Charsets.UTF_8));

      log("STDRR:");
      log(stdErr);

      log("STDOUT:");
      log(stdOut);

      assertThat(exitCode, equalTo(0)); // exited OK
      assertThat(stdOut, containsString("+ "+testMethodName()+"@0.0.1")); // published
    }

    // 2nd run: re-publishing same version, should fail (default is not allow redeploy)
    {
      log("2nd run: CMD: {}", cmd);
      final Runtime rt = Runtime.getRuntime();
      final Process npm = rt.exec(cmd);
      final int exitCode = npm.waitFor();

      // Really no clue why npm CLI uses both for non-error output
      final String stdOut = CharStreams.toString(new InputStreamReader(npm.getInputStream(), Charsets.UTF_8));
      final String stdErr = CharStreams.toString(new InputStreamReader(npm.getErrorStream(), Charsets.UTF_8));

      log("STDRR:");
      log(stdErr);

      log("STDOUT:");
      log(stdOut);

      assertThat(exitCode, not(equalTo(0))); // exited with error
      assertThat(stdErr, containsString("does not allow updating artifacts")); // error msg
    }

    // repo config change, allow redeploy
    privateRegistry.allowRedeploy().save();

    // 3rd run: re-publishing same version, should be OK, we just allowed that
    {
      log("3rd run: CMD: {}", cmd);
      final Runtime rt = Runtime.getRuntime();
      final Process npm = rt.exec(cmd);
      final int exitCode = npm.waitFor();

      // Really no clue why npm CLI uses both for non-error output
      final String stdOut = CharStreams.toString(new InputStreamReader(npm.getInputStream(), Charsets.UTF_8));
      final String stdErr = CharStreams.toString(new InputStreamReader(npm.getErrorStream(), Charsets.UTF_8));

      log("STDRR:");
      log(stdErr);

      log("STDOUT:");
      log(stdOut);

      assertThat(exitCode, equalTo(0)); // exited OK
      assertThat(stdOut, containsString("+ "+testMethodName()+"@0.0.1")); // published
    }
  }
}