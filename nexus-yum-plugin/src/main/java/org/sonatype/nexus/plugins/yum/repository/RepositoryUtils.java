package org.sonatype.nexus.plugins.yum.repository;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.sonatype.nexus.proxy.repository.Repository;


public final class RepositoryUtils {
  private RepositoryUtils() {
  }

  public static File getBaseDir(Repository repository) throws URISyntaxException, MalformedURLException {
    String localUrl = repository.getLocalUrl();
    if (isFile(localUrl)) {
      return new File(localUrl);
    }
    return new File(new URL(localUrl).toURI());
  }

  private static boolean isFile(String localUrl) {
    return localUrl.startsWith("/");
  }
}
