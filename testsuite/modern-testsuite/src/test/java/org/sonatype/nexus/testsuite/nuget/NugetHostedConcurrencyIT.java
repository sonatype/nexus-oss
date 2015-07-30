package org.sonatype.nexus.testsuite.nuget;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.test.concurrent.ConcurrentRunner;
import org.sonatype.nexus.test.concurrent.ConcurrentTask;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * Concurrency tests for nuget hosted repos.
 */
public class NugetHostedConcurrencyIT
    extends NugetITSupport
{
  private static final String HOSTED_REPO = "nuget-test-hosted";

  private NugetClient nuget;

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration config = hostedConfig(HOSTED_REPO);

    final Repository repository = createRepository(config);

    nuget = nugetClient(repository);
  }

  @Test
  public void hostedConcurrency() throws Exception {

    final ConcurrentRunner runner = new ConcurrentRunner(10, 15);

    runner.addTask(1, new ConcurrentTask()
    {
      @Override
      public void run() throws Exception {
        try (CloseableHttpResponse response = nuget.packageContent("SONATYPE.TEST", "1.0")) {
          assertThat(status(response), anyOf(is(HttpStatus.OK), is(HttpStatus.NOT_FOUND)));
        }
      }
    });

    //runner.addTask(1, new ConcurrentTask()
    //{
    //  @Override
    //  public void run() throws Exception {
    //    try (CloseableHttpResponse response = nuget.packageContent("SONATYPE.TEST", "1.1")) {
    //      assertThat(status(response), anyOf(is(HttpStatus.OK), is(HttpStatus.NOT_FOUND)));
    //    }
    //  }
    //});
    //
    //runner.addTask(1, new ConcurrentTask()
    //{
    //  @Override
    //  public void run() throws Exception {
    //    final int status = nuget.vsSearchCount("SONATYPE.TEST");
    //    // There should be between 0 and 2 such packages, depending on whether it's been uploaded yet
    //    assertThat(status, allOf(
    //        is(greaterThanOrEqualTo(0)),
    //        lessThanOrEqualTo(2)));
    //  }
    //});
    //
    //runner.addTask(1, new ConcurrentTask()
    //{
    //  @Override
    //  public void run() throws Exception {
    //    nuget.feedXml(VISUAL_STUDIO_INITIAL_FEED_QUERY);
    //  }
    //});
    //
    //runner.addTask(1, new ConcurrentTask()
    //{
    //  @Override
    //  public void run() throws Exception {
    //    final int status = nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));
    //    assertThat(status, is(HttpStatus.CREATED));
    //  }
    //});

    //runner.addTask(1, new ConcurrentTask()
    //{
    //  @Override
    //  public void run() throws Exception {
    //    final int status = nuget.publish(resolveTestFile("SONATYPE.TEST.1.1.nupkg"));
    //    assertThat(status, is(HttpStatus.CREATED));
    //  }
    //});

    runner.go();

    // Check that everything actually got invoked.
    assertThat(runner.getRunInvocations(), is(runner.getTaskCount() * runner.getIterations()));
  }
}
