package org.sonatype.nexus.plugins.yum.repository;

import java.io.File;


public interface FileDirectoryStructure {
  File getFile(String path);

}
