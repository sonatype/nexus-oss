package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.io.IOException;
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

import static java.util.Arrays.asList;

/**
 * Deploys a maven site to a raw repository.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenSiteTestSupport
    extends RawITSupport
{
  protected void mvn(final String project, final String projectVersion, final String repositoryName,
                     final String... goals) throws IOException, VerificationException
  {
    final ImmutableMap<String, String> replacements = ImmutableMap.of(
        "${project.version}", projectVersion,
        "${reponame}", repositoryName,
        "${nexus.port}", String.valueOf(nexusUrl.getPort())
    );

    final File mavenBaseDir = resolveBaseFile("target/raw-mvn-site/" + project).getAbsoluteFile();
    DirSupport.mkdir(mavenBaseDir.toPath());

    final File mavenSettings = createMavenSettings(mavenBaseDir, replacements);

    final File projectDir = resolveTestFile(project);
    DirSupport.copy(projectDir.toPath(), mavenBaseDir.toPath());

    writePom(mavenBaseDir, projectDir, replacements);

    Verifier verifier = buildMavenVerifier(repositoryName, mavenBaseDir, mavenSettings);
    verifier.executeGoals(asList(goals));
    verifier.verifyErrorFreeLog();
  }

  private void writePom(final File mavenBaseDir, final File projectDir, final ImmutableMap<String, String> replacements)
      throws IOException
  {
    writeModifiedFile(new File(projectDir, "pom.xml"),
        new File(mavenBaseDir, "pom.xml").getAbsoluteFile(),
        replacements);
  }


  /**
   * Produces a maven settings file, pointing to the test Nexus instance.
   */
  @NotNull
  private File createMavenSettings(final File mavenBaseDir, final ImmutableMap<String, String> replacements)
      throws IOException
  {
    return writeModifiedFile(resolveTestFile("settings.xml"),
        new File(mavenBaseDir, "settings.xml").getAbsoluteFile(),
        replacements);
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

  @NotNull
  private Verifier buildMavenVerifier(final String repositoryName, final File mavenBaseDir,
                                      final File mavenSettings) throws VerificationException
  {
    Verifier verifier = new Verifier(mavenBaseDir.getAbsolutePath());
    verifier.addCliOption("-s " + mavenSettings.getAbsolutePath());
    verifier.addCliOption(
        // Verifier replaces // -> /
        "-DaltDeploymentRepository=local-nexus-admin::default::http:////localhost:" + nexusUrl.getPort() +
            "/repository/" + repositoryName);
    return verifier;
  }
}
