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

package org.sonatype.nexus.testsuite.rapture;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.sisu.filetasks.builder.FileRef;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.runners.Parameterized;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.EACH_TEST;

/**
 * @since 2.8
 */
@NexusStartAndStopStrategy(EACH_TEST)
public class SiestaStandardUiITSupport
    extends NexusRunningParametrizedITSupport
{

  private final String executable;

  private final String[] options;

  @Parameterized.Parameters
  public static List<Object[]> drivers() {
    return Lists.newArrayList(
        new Object[]{"webdriver", new String[]{"--browser=firefox"}},
        //new Object[]{"webdriver", new String[]{"--browser=chrome"}},
        new Object[]{"phantomjs", new String[]{}},
        new Object[]{
            "webdriver", new String[]{
            "--browser=chrome", "--host=http://adreghiciu:13a9920f-5910-42e8-9f54-854f638470b0@ondemand.saucelabs.com",
            "--port=80"
        }
        }
    );
  }

  public SiestaStandardUiITSupport(final String executable, final String[] options) {
    super("${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle");
    this.executable = executable;
    this.options = options;
  }

  @Override
  protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
    return configuration.addPlugins(
        artifactResolver().resolvePluginFromDependencyManagement(
            "org.sonatype.nexus", "nexus-testsuite-ui-plugin"
        )
    );
  }

  protected void run(final String test) throws Exception {
    File siestaDir = prepare();
    runTest(test, siestaDir);
    assertResults(siestaDir);
  }

  private void assertResults(final File siestaDir) throws JSONException, IOException {
    File[] reports = siestaDir.listFiles(new FilenameFilter()
    {
      @Override
      public boolean accept(final File dir, final String name) {
        return name.startsWith("report") && name.endsWith(".json");
      }
    });

    assertThat("Siesta results file not found. Check test output for details.", reports.length > 0);
    assertThat("Too many Siesta results files found. Check test output for details.", reports.length == 1);

    File report = reports[0];
    JSONObject jsonObject = new JSONObject(FileUtils.readFileToString(report));
    JSONArray assertions = jsonObject.getJSONArray("testCases").getJSONObject(0).getJSONArray("assertions");
    for (int i = 0; i < assertions.length(); i++) {
      JSONObject assertion = assertions.getJSONObject(i);
      if (assertion.has("passed") && !assertion.getBoolean("passed")) {
        assertThat(assertion.getString("annotation"), false);
      }
    }
  }

  private File prepare() {
    File siestaJar = artifactResolver().resolveFromDependencyManagement(
        "org.sonatype.nexus", "nexus-testsuite-ui-siesta", null, null, null, null
    );
    File siestaDir = testIndex().getDirectory("siesta");
    tasks().expand(FileRef.file(siestaJar)).to().directory(FileRef.file(siestaDir)).run();
    if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
      tasks().chmod(FileRef.file(siestaDir)).include("**/" + executable).permissions("u+x").run();
    }
    return siestaDir;
  }

  private void runTest(final String test, final File siestaDir) throws Exception {
    logger.info("Running {}", test);

    System.out.println("----------------------------------------");

    List<String> command = Lists.newArrayList();
    command.add(new File(siestaDir, executable + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "")).getAbsolutePath());
    command.add(nexus().getUrl() + "static/rapture/nexus-ui-tests.html");
    if (options != null) {
      Collections.addAll(command, options);
    }
    command.add("--include=" + test);
    command.add("--verbose");
    command.add("--report-format=JSON");
    command.add("--report-file=report.json");

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(siestaDir);
    processBuilder.redirectErrorStream(true);
    processBuilder.redirectOutput(Redirect.INHERIT);

    int exitCode = processBuilder.start().waitFor();

    System.out.println("----------------------------------------");

    logger.debug("Siesta exit code: {}", exitCode);
  }

}
