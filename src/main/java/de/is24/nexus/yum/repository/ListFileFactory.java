package de.is24.nexus.yum.repository;

import java.io.File;


public interface ListFileFactory {
  File getRpmListFile(String id);

  File getRpmListFile(String id, String version);

}
