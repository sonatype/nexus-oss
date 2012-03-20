package de.is24.nexus.yum.config;

import java.io.File;

import de.is24.nexus.yum.config.domain.XmlYumConfiguration;
import de.is24.nexus.yum.version.alias.AliasNotFoundException;


public interface YumConfiguration {
  public String getVersion(String repositoryId, String alias) throws AliasNotFoundException;

  public void setAlias(String repositoryId, String alias, String version);

  public XmlYumConfiguration getXmlYumConfiguration();

  public void setFilename(String testConfFilename);

  public void saveConfig(XmlYumConfiguration confToWrite);

  public void load();

  public File getConfigFile();

  public void setRepositoryOfRepositoryVersionsActive(boolean active);

  public boolean isRepositoryOfRepositoryVersionsActive();

  public boolean isDeleteProcessing();

  public void setDeleteProcessing(boolean active);

  public long getDelayAfterDeletion();

  public void setDelayAfterDeletion(long timeout);

  public File getBaseTempDir();
}
