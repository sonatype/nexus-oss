package org.sonatype.nexus.plugins.yum.repository.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sonatype.nexus.plugins.yum.repository.YumRepository;


public class YumRepositoryCache {
  private final Map<String, YumRepository> cache = new ConcurrentHashMap<String, YumRepository>();

  public YumRepository lookup(String id, String version) {
    YumRepository yumRepository = cache.get(hash(id, version));

    if ((yumRepository != null) && !yumRepository.getBaseDir().exists()) {
      yumRepository.setDirty();
    }

    return yumRepository;
  }

  public void cache(YumRepository yumRepository) {
    cache.put(hash(yumRepository.getId(), yumRepository.getVersion()), yumRepository);
  }

  public void markDirty(String id, String version) {
    YumRepository repository = cache.get(hash(id, version));
    if (repository != null) {
      repository.setDirty();
    }
  }

  private String hash(String id, String version) {
    return id + "/" + version;
  }

}
