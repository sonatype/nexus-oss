package de.is24.nexus.yum.service;

import java.io.File;
import de.is24.nexus.yum.service.impl.XmlYumConfiguration;


public interface YumConfiguration {
  String getVersion(String repositoryId, String alias) throws AliasNotFoundException;

  void setAlias(String repositoryId, String alias, String version);

  void setRepositoryOfRepositoryVersionsActive(boolean active);

  XmlYumConfiguration getXmlYumConfiguration();

  void setFilename(String testConfFilename);

  void saveConfig(XmlYumConfiguration confToWrite);

  void load();

  File getConfigFile();

  boolean isRepositoryOfRepositoryVersionsActive();
}
