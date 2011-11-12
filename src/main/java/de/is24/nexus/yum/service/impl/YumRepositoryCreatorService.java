package de.is24.nexus.yum.service.impl;

import org.sonatype.scheduling.ScheduledTask;
import de.is24.nexus.yum.repository.YumMetadataGenerationTask;
import de.is24.nexus.yum.repository.YumRepository;


/**
 * Created by IntelliJ IDEA.
 * User: BVoss
 * Date: 28.07.11
 * Time: 18:08
 * To change this template use File | Settings | File Templates.
 */
public interface YumRepositoryCreatorService {
  String DEFAULT_BEAN_NAME = "yumRepositoryCreatorService";

  boolean isShutdown();

  ScheduledTask<YumRepository> submit(YumMetadataGenerationTask yumMetadataGenerationTask);

  void shutdown();

  void activate();

  int getActiveWorkerCount();

  int size();

  <T> T createTaskInstance(Class<T> taskClass);
}
