package org.sonatype.nexus.plugins.yum.repository;

import static java.io.File.pathSeparator;
import static java.lang.System.getenv;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FilenameFilter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CheckCreateRepoAvailable {
  private static final Logger log = LoggerFactory.getLogger(CheckCreateRepoAvailable.class);

  @Test
  public void shouldHaveCreaterepoInPath() throws Exception {
    String[] paths = getenv("PATH").split(pathSeparator);
    for (String path : paths) {
      log.info("Search for createrepo in {} ...", path);

      String[] files = new File(path).list(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return "createrepo".equals(name);
          }
        });
      if (files.length > 0) {
        log.info("Found createrepo in {} !", path);
        return;
      }
    }
    fail("Createrepo not found.");
  }

}
