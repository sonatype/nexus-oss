package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.nexus.common.io.DirSupport;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.jetbrains.annotations.NotNull;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Deploys a maven site to a raw repository.
 */
@ExamReactorStrategy(PerClass.class)
public class RawMavenSiteIT
    extends RawITSupport
{
  protected void mvn(final String project, final String version, final String deployRepositoryName)
      throws Exception
  {
    List<String> goals = Arrays.asList("clean", "site:deploy");

    final File mavenBaseDir = resolveBaseFile("target/raw-mvn-site/" + project).getAbsoluteFile();
    DirSupport.mkdir(mavenBaseDir.toPath());

    final File mavenPom = new File(mavenBaseDir, "pom.xml").getAbsoluteFile();

    final File mavenSettings = createMavenSettings(mavenBaseDir);

    final File projectDir = resolveTestFile(project);
    DirSupport.copy(projectDir.toPath(), mavenBaseDir.toPath());

    writeModifiedFile(new File(projectDir, "pom.xml"),
        mavenPom,
        ImmutableMap.of("${project.version}", version));

    Verifier verifier = buildMavenVerifier(deployRepositoryName, mavenBaseDir, mavenSettings);
    verifier.executeGoals(goals);
    verifier.verifyErrorFreeLog();
  }

  @NotNull
  private Verifier buildMavenVerifier(final String deployRepositoryName, final File mavenBaseDir,
                                      final File mavenSettings) throws VerificationException
  {
    Verifier verifier = new Verifier(mavenBaseDir.getAbsolutePath());
    verifier.addCliOption("-s " + mavenSettings.getAbsolutePath());
    verifier.addCliOption(
        // Verifier replaces // -> /
        "-DaltDeploymentRepository=local-nexus-admin::default::http:////localhost:" + nexusUrl.getPort() +
            "/repository/" + deployRepositoryName);
    return verifier;
  }

  /**
   * Produces a maven settings file, pointing to the test Nexus instance.
   */
  private File createMavenSettings(final File mavenBaseDir) throws IOException {
    // set settings NX port
    return writeModifiedFile(resolveTestFile("settings.xml"),
        new File(mavenBaseDir, "settings.xml").getAbsoluteFile(),
        ImmutableMap.of("${nexus.port}", String.valueOf(nexusUrl.getPort())));
  }

  @NotNull
  private File writeModifiedFile(final File source, final File target, final Map<String, String> replacements)
      throws IOException
  {
    String content = Files.toString(source, Charsets.UTF_8);
    for (Entry<String, String> entry : replacements.entrySet()) {
      content = content.replace(entry.getKey(), entry.getValue());
    }

    Files.write(content, target, Charsets.UTF_8);

    return target;
  }
}
