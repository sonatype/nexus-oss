package org.sonatype.nexus.testsuite.nuget;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.test.concurrent.ConcurrentRunner;
import org.sonatype.nexus.test.concurrent.ConcurrentTask;
import org.sonatype.sisu.litmus.testsupport.group.Perf;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.bytes;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * A concurrency test focused on Nuget Proxy repositories. Does not measure throughput, only correctness.
 */
@Category(Perf.class)
public class NugetProxyConcurrencyIT
    extends NugetProxyITSupport
{
  public static final String PROXY_REPO_NAME = "nuget-test-proxy";

  protected Repository repo;

  @Before
  public void createProxyRepo()
      throws Exception
  {
    configureProxyResources();

    final String remoteStorageUrl = proxyServer.getUrl().toExternalForm() + "/" + REMOTE_NUGET_REPO_PATH;

    repo = createRepository(proxyConfig(PROXY_REPO_NAME, remoteStorageUrl));
  }

  @Test
  public void concurrentProxyTest() throws Exception {

    final int terminationWaitTimeSeconds = 100;
    final int iterations = 5;

    final ConcurrentRunner runner = new ConcurrentRunner(iterations, terminationWaitTimeSeconds);

    // Some threads will be requesting a specific package
    final int packageReaderThreads = 10;
    runner.addTask(packageReaderThreads, new ConcurrentTask()
    {
      private NugetClient nuget = nugetClient(repo);

      @Override
      public void run() throws Exception {
        try (CloseableHttpResponse httpResponse = nuget.packageContent("SONATYPE.TEST", "1.0")) {
          assertThat(status(httpResponse), is(200));
          bytes(httpResponse);
        }
      }
    });

    final int odataReaderThreads = 10;
    runner.addTask(odataReaderThreads, new ConcurrentTask()
    {
      private NugetClient nuget = nugetClient(repo);

      @Override
      public void run() throws Exception {
        nuget.feedXml(NugetClient.VS_INITIAL_FEED_QUERY);
        nuget.vsSearchCount("SONATYPE.TEST");
      }
    });

    runner.go();

    assertThat(runner.getRunInvocations(), is(runner.getTaskCount() * runner.getIterations()));
  }
}
