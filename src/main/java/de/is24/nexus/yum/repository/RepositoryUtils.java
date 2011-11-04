package de.is24.nexus.yum.repository;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.sonatype.nexus.proxy.repository.Repository;


public final class RepositoryUtils {
  private RepositoryUtils() {
  }

  public static File getBaseDir(Repository repository) throws URISyntaxException, MalformedURLException {
    return new File(new URL(repository.getLocalUrl()).toURI());
  }
}
