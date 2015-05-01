package org.sonatype.nexus.testsuite.nuget;

import java.net.URL;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * @since 3.0
 */
public class NugetITSupport
    extends NexusHttpsITSupport
{
  //protected NugetClient nugetClient(final Repository repository) throws Exception {
  //
  //  final CloseableHttpClient httpClient = clientBuilder().setDefaultCredentialsProvider(credentialsProvider()).build();
  //  final URL url = resolveUrl(nexusUrl, "/repository/" + repository.getName() + "/");
  //  return new NugetClient(httpClient, clientContext(), url.toURI());
  //}
}
