/*
 * Copyright (c) 2007-2014 Sonatype, Inc. and Georgy Bolyuba. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.bolyuba.nexus.plugin.npm.task;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.TaskUtil;

import com.bolyuba.nexus.plugin.npm.hosted.NpmHostedRepository;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Named(NpmHostedMetadataRebuildTaskDescriptor.ID)
@Singleton
public class NpmHostedMetadataRebuildTask
    extends AbstractNexusRepositoriesTask<Void>
{
  private final NexusConfiguration nexusConfiguration;

  @Inject
  public NpmHostedMetadataRebuildTask(final NexusConfiguration nexusConfiguration) {
    this.nexusConfiguration = checkNotNull(nexusConfiguration);
  }

  @Override
  protected Void doRun() throws Exception {
    final List<NpmHostedRepository> repositories = getRepositories();
    if (repositories.isEmpty()) {
      return null; // avoid at least 3 save configs
    }
    final List<RepositoryWritePolicy> writePolicies = setWritePolicyReadOnly(repositories);
    try {
      for (NpmHostedRepository repository : repositories) {
        TaskUtil.checkInterruption();
        try {
          int fileItemCount = repository.recreateNpmMetadata();
          logger.info("Recreated {} item in {}", fileItemCount, repository.getId());
        }
        catch (Exception e) {
          logger.info("Hosted npm repository error during rebuilding metadata of {}", repository, e);
        }
      }
    }
    finally {
      resetWritePolicy(repositories, writePolicies);
    }
    return null;
  }

  protected List<RepositoryWritePolicy> setWritePolicyReadOnly(final List<NpmHostedRepository> repositories)
      throws IOException
  {
    final List<RepositoryWritePolicy> result = Lists.newArrayList();
    for (NpmHostedRepository repository : repositories) {
      result.add(repository.getWritePolicy());
      repository.setWritePolicy(RepositoryWritePolicy.READ_ONLY);
    }
    nexusConfiguration.saveConfiguration();
    return result;
  }

  protected void resetWritePolicy(final List<NpmHostedRepository> repositories,
                                  final List<RepositoryWritePolicy> writePolicies) throws IOException
  {
    checkArgument(repositories.size() == writePolicies.size(), "inconsistent original write policies");
    for (int i = 0; i < repositories.size(); i++) {
      repositories.get(i).setWritePolicy(writePolicies.get(i));
    }
    nexusConfiguration.saveConfiguration();
  }

  @Override
  protected String getAction() {
    return "NPM-MD-REBUILD";
  }

  @Override
  protected String getMessage() {
    final List<String> repositories = Lists.transform(getRepositories(), new Function<NpmHostedRepository, String>()
    {
      @Override
      public String apply(final NpmHostedRepository input) {
        return input.getId();
      }
    });

    return "Rebuild hosted npm metadata of repositories: " + repositories;
  }

  protected List<NpmHostedRepository> getRepositories() {
    if (Strings.isNullOrEmpty(getRepositoryId())) {
      return getRepositoryRegistry().getRepositoriesWithFacet(NpmHostedRepository.class);
    }
    else {
      try {
        return Lists.newArrayList(
            getRepositoryRegistry().getRepositoryWithFacet(getRepositoryId(), NpmHostedRepository.class)
        );
      }
      catch (NoSuchRepositoryException e) {
        throw new IllegalArgumentException(
            "Rebuild hosted npm metadata task misconfiguration: no npm repository with ID " + getRepositoryId(), e);
      }
    }
  }

  @Override
  protected String getRepositoryFieldId() {
    return NpmHostedMetadataRebuildTaskDescriptor.FLD_REPOSITORY_ID;
  }
}
