/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.Recipe;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.config.ConfigurationStore;
import org.sonatype.nexus.repository.manager.RepositoryManager;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.FacetSupport.State.STARTED;
import static org.sonatype.nexus.repository.util.TypeTokens.COLLECTION_STRING;

/**
 * Default {@link GroupFacet} implementation.
 *
 * @since 3.0
 */
@Named
public class GroupFacetImpl
    extends FacetSupport
    implements GroupFacet
{
  public static final String CONFIG_KEY = "group";

  private final RepositoryManager repositoryManager;

  private final ConfigurationStore configurationStore;

  private final Map<String, Recipe> recipes;

  private Config config;
  private static class Config
  {
    @NotNull
    public List<String> memberNames;

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "memberNames='" + memberNames +
          '}';
    }
  }

  @Inject
  public GroupFacetImpl(final RepositoryManager repositoryManager,
                        final ConfigurationStore configurationStore, final Map<String, Recipe> recipes)
  {
    this.repositoryManager = checkNotNull(repositoryManager);
    this.configurationStore = checkNotNull(configurationStore);
    this.recipes = checkNotNull(recipes);
  }

  // TODO: Check for compatibility and cyclic-references

  @Override
  protected void doConfigure() throws Exception {
    config = facet(ConfigurationFacet.class).readObject(CONFIG_KEY, Config.class);

    Map<String, Configuration> memberConfigs = memberConfigs();
    for (String memberName : config.memberNames) {
      ensureMemberExists(memberConfigs, memberName);
      ensureFormatMatches(memberName, getRepository().getFormat(), memberConfigs);
    }
  }

  private void ensureMemberExists(final Map<String, Configuration> memberConfigs, final String memberName) {
    if (!memberConfigs.containsKey(memberName)) {
      throw new ValidationException("Group refers to nonexistent member repository " + memberName);
    }
  }

  private void ensureFormatMatches(final String memberName, final Format requiredFormat,
                                   final Map<String, Configuration> memberConfigs)
  {
    final Configuration memberConfig = memberConfigs.get(memberName);
    final String recipeName = memberConfig.getRecipeName();
    final Format memberFormat = recipes.get(recipeName).getFormat();

    if (!requiredFormat.equals(memberFormat)) {
      throw new ValidationException(String.format("%s-format %s cannot contain %s-format member %s",
          requiredFormat, getRepository().getName(), memberFormat, memberName));
    }
  }

  private Map<String, Configuration> memberConfigs() {
    Map<String, Configuration> memberConfigs = Maps.newHashMap();

    for (Configuration repoConfig : configurationStore.list()) {
      if (config.memberNames.contains(repoConfig.getRepositoryName())) {
        memberConfigs.put(repoConfig.getRepositoryName(), repoConfig);
      }
    }
    return memberConfigs;
  }

  @Override
  protected void doDestroy() throws Exception {
    config.memberNames.clear();
  }

  @Override
  @Guarded(by = STARTED)
  public boolean member(final Repository repository) {
    checkNotNull(repository);
    return config.memberNames.contains(repository.getName());
  }

  @Override
  @Guarded(by = STARTED)
  public List<Repository> members() {
    List<Repository> members = new ArrayList<>(config.memberNames.size());
    for (String name : config.memberNames) {
      Repository repository = repositoryManager.get(name);
      if (repository != null) {
        members.add(repository);
      }
      else {
        log.warn("Ignoring missing member repository: {}", name);
      }
    }
    return members;
  }

  @Override
  public List<Repository> leafMembers() {
    List<Repository> leafMembers = new ArrayList<>();

    for (Repository repository : members()) {
      try {
        final GroupFacet groupFacet = repository.facet(GroupFacet.class);
        leafMembers.addAll(groupFacet.leafMembers());
      }
      catch (MissingFacetException e) {
        leafMembers.add(repository);
      }
    }

    return leafMembers;
  }
}
