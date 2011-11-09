package de.is24.nexus.yum.service.impl;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.plugin.Managed;
import com.google.inject.Singleton;
import de.is24.nexus.yum.service.AliasMapper;
import de.is24.nexus.yum.service.AliasNotFoundException;
import de.is24.nexus.yum.service.RepositoryCreationTimeoutHolder;


@Managed
@Named(AliasMapper.DEFAULT_BEAN_NAME)
@Singleton
public class YumConfigurationHandler implements AliasMapper, RepositoryCreationTimeoutHolder {
  public static final String YUM_XML = "yum.xml";

  private static final Logger log = LoggerFactory.getLogger(YumConfigurationHandler.class);

  private static final Object LOAD_WRITE_MUTEX = new Object();

  private String filename = YUM_XML;
  private long fileLastModified;

  private NexusConfiguration nexusConfiguration;

  private XmlYumConfiguration xmlYumConfiguration = new XmlYumConfiguration();

  private final ConcurrentHashMap<AliasKey, String> aliasMap = new ConcurrentHashMap<AliasKey, String>();

  private final Unmarshaller unmarshaller;
  private final Marshaller marshaller;

  public YumConfigurationHandler() throws JAXBException {
    final JAXBContext jc = JAXBContext.newInstance(XmlYumConfiguration.class, AliasMapping.class);
    this.marshaller = jc.createMarshaller();
    this.unmarshaller = jc.createUnmarshaller();
  }

  public void load() {
    File configFile = getOrCreateConfigFile();
    synchronized (LOAD_WRITE_MUTEX) {
      try {
        xmlYumConfiguration = (XmlYumConfiguration) unmarshaller.unmarshal(configFile);
        fileLastModified = getConfigFile().lastModified();
        fillAliasMap();
      } catch (JAXBException e) {
        log.warn("can't load config file staing with old config", e);
      }
    }
  }


  public void saveConfig(XmlYumConfiguration configToUse) {
    synchronized (LOAD_WRITE_MUTEX) {
      try {
        marshaller.marshal(configToUse, getConfigFile());
        xmlYumConfiguration = configToUse;
        fileLastModified = getConfigFile().lastModified();
      } catch (JAXBException e) {
        throw new RuntimeException("can't save xmlyumConfig", e);
      }
    }
  }

  public File getConfigFile() {
    return new File(nexusConfiguration.getConfigurationDirectory(), filename);
  }

  public String getVersion(String repositoryId, String alias) throws AliasNotFoundException {
    checkForUpdates();

    final AliasKey aliasKey = new AliasKey(repositoryId, alias);
    final String resultVersion = aliasMap.get(aliasKey);
    if (resultVersion == null) {
      throw new AliasNotFoundException("for " + aliasKey);
    }
    return resultVersion;
  }

  public void setAlias(String repositoryId, String alias, String version) {
    final XmlYumConfiguration newConfig = new XmlYumConfiguration(xmlYumConfiguration);
    final AliasMapping newAliasMapping = new AliasMapping(repositoryId, alias, version);
    newConfig.getAliasMappings().add(newAliasMapping);
    saveConfig(newConfig);
    aliasMap.put(newAliasMapping.getAliasKey(), newAliasMapping.getVersion());
  }

  @Inject
  public void setNexusConfiguration(NexusConfiguration nexusConfiguration) {
    this.nexusConfiguration = nexusConfiguration;
    load();
  }

  public void setRepositoryCreationTimeout(int seconds) {
    final XmlYumConfiguration newConfig = new XmlYumConfiguration(xmlYumConfiguration);
    newConfig.setRepositoryCreationTimeout(seconds);
    saveConfig(newConfig);
  }

  public void setRepositoryOfRepositoryVersionsActive(boolean active) {
    final XmlYumConfiguration newConfig = new XmlYumConfiguration(xmlYumConfiguration);
    newConfig.setRepositoryOfRepositoryVersionsActive(active);
    saveConfig(newConfig);
  }

  public boolean isRepositoryOfRepositoryVersionsActive() {
    checkForUpdates();
    return xmlYumConfiguration.isRepositoryOfRepositoryVersionsActive();
  }

  public int getRepositoryCreationTimeout() {
    checkForUpdates();
    return xmlYumConfiguration.getRepositoryCreationTimeout();
  }

  public XmlYumConfiguration getXmlYumConfiguration() {
    checkForUpdates();
    return xmlYumConfiguration;
  }

  private void checkForUpdates() {
    if (getConfigFile().lastModified() > fileLastModified) {
      load();
    }
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  private File getOrCreateConfigFile() {
    File configFile = getConfigFile();
    if (!configFile.exists()) {
      saveConfig(xmlYumConfiguration);
    }
    return configFile;
  }

  private void fillAliasMap() {
    aliasMap.clear();
    for (AliasMapping aliasMapping : xmlYumConfiguration.getAliasMappings()) {
      aliasMap.put(aliasMapping.getAliasKey(), aliasMapping.getVersion());
    }
  }
}
