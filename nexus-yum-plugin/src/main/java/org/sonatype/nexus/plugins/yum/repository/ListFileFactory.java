package org.sonatype.nexus.plugins.yum.repository;

import java.io.File;


public interface ListFileFactory {
  File getRpmListFile(String id);

  File getRpmListFile(String id, String version);

}
