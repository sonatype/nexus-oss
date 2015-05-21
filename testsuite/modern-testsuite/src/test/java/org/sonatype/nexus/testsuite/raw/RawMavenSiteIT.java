package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.util.Arrays;

import org.sonatype.nexus.common.io.DirSupport;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.maven.it.Verifier;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Deploys a maven site to a raw repository.
 */
@ExamReactorStrategy(PerClass.class)
public class RawMavenSiteIT
    extends RawITSupport
{
  protected void mvnDeploy(final String project, final String version, final String deployRepositoryName)
      throws Exception
  {
    // TODO: Rule TestName does not work due to PAX ITs?
    final File mavenBaseDir = resolveBaseFile("target/maven-it-support/" + project).getAbsoluteFile();
    final File mavenSettings = new File(mavenBaseDir, "settings.xml").getAbsoluteFile();
    final File mavenPom = new File(mavenBaseDir, "pom.xml").getAbsoluteFile();

    DirSupport.mkdir(mavenBaseDir.toPath());

    {
      // set settings NX port
      final String settingsXml = Files.toString(resolveTestFile("settings.xml"), Charsets.UTF_8).replace(
          "${nexus.port}", String.valueOf(nexusUrl.getPort()));
      Files.write(settingsXml, mavenSettings, Charsets.UTF_8);
    }

    final File projectDir = resolveTestFile(project);
    DirSupport.copy(projectDir.toPath(), mavenBaseDir.toPath());

    {
      // set POM version
      final String pomXml = Files.toString(new File(projectDir, "pom.xml"), Charsets.UTF_8).replace(
          "${project.version}", version);
      Files.write(pomXml, mavenPom, Charsets.UTF_8);
    }

    Verifier verifier = new Verifier(mavenBaseDir.getAbsolutePath());
    verifier.addCliOption("-s " + mavenSettings.getAbsolutePath());
    verifier.addCliOption(
        // Verifier replaces // -> /
        "-DaltDeploymentRepository=local-nexus-admin::default::http:////localhost:" + nexusUrl.getPort() +
            "/repository/" + deployRepositoryName);
    verifier.executeGoals(Arrays.asList("clean", "deploy"));
    verifier.verifyErrorFreeLog();
  }
}
