package de.is24.nexus.yum.service.impl;

import java.util.concurrent.Future;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.YumRepositoryGeneratorJob;


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

  Future<YumRepository> submit(YumRepositoryGeneratorJob yumRepositoryGeneratorJob);

  void shutdown();

  void activate();

  int getActiveWorkerCount();

  int size();
}
