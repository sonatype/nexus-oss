/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.AbstractLastingConfigurable;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryGroupingCoreConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping.MappingType;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Class PathBasedRequestRepositoryMapper filters repositories to search using supplied list of filter expressions.
 * It is parametrized by java,util.Map, the contents: </p> <tt>
 * regexp1=repo1,repo2...
 * regexp2=repo3,repo4...
 * ...
 * </tt>
 * <p>
 * An example (with grouped Router and two repositories, one for central and one for inhouse in same group):
 * </p>
 * <tt>
 * /com/company/=inhouse
 * /org/apache/=central
 * </tt>
 *
 * @author cstamas
 */
@Singleton
@Named
public class DefaultRequestRepositoryMapper
    extends AbstractLastingConfigurable<CRepositoryGrouping>
    implements RequestRepositoryMapper
{
  private final RepositoryRegistry repositoryRegistry;

  private final List<RepositoryPathMapping> blockings = Lists.newCopyOnWriteArrayList();

  private final List<RepositoryPathMapping> inclusions = Lists.newCopyOnWriteArrayList();

  private final List<RepositoryPathMapping> exclusions = Lists.newCopyOnWriteArrayList();

  /**
   * The compiled flag.
   */
  private volatile boolean compiled = false;

  @Inject
  public DefaultRequestRepositoryMapper(final EventBus eventBus,
                                        final ApplicationConfiguration applicationConfiguration,
                                        final RepositoryRegistry repositoryRegistry)
  {
    super("Repository Grouping Configuration", eventBus, applicationConfiguration);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
  }

  // == Config

  @Override
  protected void initializeConfiguration()
      throws ConfigurationException
  {
    if (getApplicationConfiguration().getConfigurationModel() != null) {
      configure(getApplicationConfiguration());
    }
  }

  @Override
  protected CoreConfiguration<CRepositoryGrouping> wrapConfiguration(Object configuration)
      throws ConfigurationException
  {
    if (configuration instanceof ApplicationConfiguration) {
      return new CRepositoryGroupingCoreConfiguration((ApplicationConfiguration) configuration);
    }
    else {
      throw new ConfigurationException("The passed configuration object is of class \""
          + configuration.getClass().getName() + "\" and not the required \""
          + ApplicationConfiguration.class.getName() + "\"!");
    }
  }

  @Override
  public boolean commitChanges()
      throws ConfigurationException
  {
    boolean wasDirty = super.commitChanges();
    if (wasDirty) {
      compiled = false;
    }
    return wasDirty;
  }

  // == Public API

  /**
   * Side effect: configuration framework will mark this component "dirty" after method returns.
   */
  @Override
  public boolean addMapping(final RepositoryPathMapping mapping)
      throws ConfigurationException
  {
    try {
      final CPathMappingItem model = convert(mapping);
      removeMapping(model.getId());
      getCurrentConfiguration(true).addPathMapping(model);
      return true;
    }
    catch (IllegalArgumentException e) {
      throw new InvalidConfigurationException("Mapping to be added is invalid", e);
    }
  }

  /**
   * Side effect: configuration framework will mark this component "dirty" after method returns.
   */
  @Override
  public boolean removeMapping(final String id) {
    for (Iterator<CPathMappingItem> i = getCurrentConfiguration(true).getPathMappings().iterator(); i.hasNext(); ) {
      CPathMappingItem mapping = i.next();
      if (mapping.getId().equals(id)) {
        i.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public Map<String, RepositoryPathMapping> getMappings() {
    final HashMap<String, RepositoryPathMapping> result = Maps.newHashMap();
    final CRepositoryGrouping config = getCurrentConfiguration(false);
    if (config != null) {
      final List<CPathMappingItem> items = config.getPathMappings();
      for (CPathMappingItem item : items) {
        RepositoryPathMapping mapping = convert(item);
        result.put(mapping.getId(), mapping);
      }
    }
    return Collections.unmodifiableMap(result);
  }

  @Override
  public List<Repository> getMappedRepositories(final Repository repository, final ResourceStoreRequest request,
                                                final List<Repository> resolvedRepositories)
      throws NoSuchRepositoryException
  {
    if (!compiled) {
      compile();
    }

    // NEXUS-2852: to make our life easier, we will work with repository IDs,
    // and will fill the result with Repositories at the end
    final LinkedHashSet<String> reposIdSet = Sets.newLinkedHashSetWithExpectedSize(resolvedRepositories.size());
    for (Repository resolvedRepositorty : resolvedRepositories) {
      reposIdSet.add(resolvedRepositorty.getId());
    }

    for (RepositoryPathMapping mapping : blockings) {
      if (mapping.matches(repository, request)) {
        log.debug(
            "The request path [{}] is blocked by rule {}", request.getRequestPath(), mapping);
        request.addAppliedMappingsList(repository, Collections.singletonList(mapping.toString()));
        return Collections.emptyList();
      }
    }

    // if include found, add it to the list.
    boolean firstAdd = true;
    // for tracking what is applied
    final ArrayList<RepositoryPathMapping> appliedMappings = Lists.newArrayList();

    // include, if found a match
    // NEXUS-2852: watch to not add multiple times same repository
    // ie. you have different inclusive rules that are triggered by same request
    // and contains some repositories. This is now solved using LinkedHashSet and using repo IDs.
    for (RepositoryPathMapping mapping : inclusions) {
      if (mapping.matches(repository, request)) {
        appliedMappings.add(mapping);
        if (firstAdd) {
          reposIdSet.clear();
          firstAdd = false;
        }

        // add only those that are in initial resolvedRepositories list
        // (and preserve ordering)
        if (mapping.getMappedRepositories().size() == 1
            && "*".equals(mapping.getMappedRepositories().get(0))) {
          for (Repository repo : resolvedRepositories) {
            reposIdSet.add(repo.getId());
          }
        }
        else {
          for (Repository repo : resolvedRepositories) {
            if (mapping.getMappedRepositories().contains(repo.getId())) {
              reposIdSet.add(repo.getId());
            }
          }
        }
      }
    }

    // then, if exclude found, remove those
    for (RepositoryPathMapping mapping : exclusions) {
      if (mapping.matches(repository, request)) {
        appliedMappings.add(mapping);

        if (mapping.getMappedRepositories().size() == 1
            && "*".equals(mapping.getMappedRepositories().get(0))) {
          reposIdSet.clear();
          break;
        }

        for (String repositoryId : mapping.getMappedRepositories()) {
          reposIdSet.remove(repositoryId);
        }
      }
    }

    // store the applied mappings to request context
    final ArrayList<String> appliedMappingsList = Lists.newArrayListWithCapacity(appliedMappings.size());
    for (RepositoryPathMapping mapping : appliedMappings) {
      appliedMappingsList.add(mapping.toString());
    }
    request.addAppliedMappingsList(repository, appliedMappingsList);

    // log it if needed
    if (log.isDebugEnabled()) {
      if (appliedMappings.isEmpty()) {
        log.debug("No mapping exists for request path [{}]", request.getRequestPath());
      }
      else {
        final StringBuilder sb =
            new StringBuilder("Request for path \"")
                .append(request.getRequestPath())
                .append("\" with the initial list of processable repositories of \"")
                .append(resolvedRepositories)
                .append("\" got these mappings applied:\n");

        for (RepositoryPathMapping mapping : appliedMappings) {
          sb.append(" * ").append(mapping.toString()).append("\n");
        }
        log.debug(sb.toString());
        if (reposIdSet.size() == 0) {
          log.debug(
              "Mapping for path [{}] excluded all repositories from servicing the request", request.getRequestPath());
        }
        else {
          log.debug(
              "Request for path [{}] is MAPPED to reposes: {}", request.getRequestPath(), reposIdSet);
        }
      }
    }

    // build up the response list with Repositories
    final ArrayList<Repository> result = Lists.newArrayListWithCapacity(reposIdSet.size());
    try {
      for (String repoId : reposIdSet) {
        result.add(repositoryRegistry.getRepository(repoId));
      }
    }
    catch (NoSuchRepositoryException e) {
      log.warn(
          "Some of the Routes contains references to non-existent repositories! Please check the following mappings: \"{}\".",
          appliedMappingsList);
      throw e;
    }
    return result;
  }

  // == Internal

  protected synchronized void compile()
      throws NoSuchRepositoryException
  {
    if (compiled) {
      return;
    }
    blockings.clear();
    inclusions.clear();
    exclusions.clear();

    if (getCurrentConfiguration(false) == null) {
      log.debug("No Routes defined, have nothing to compile.");
      return;
    }

    final Map<String, RepositoryPathMapping> mappings = getMappings();
    for (RepositoryPathMapping mapping : mappings.values()) {
      switch (mapping.getMappingType()) {
        case BLOCKING:
          blockings.add(mapping);
          break;
        case INCLUSION:
          inclusions.add(mapping);
          break;
        case EXCLUSION:
          exclusions.add(mapping);
          break;
        default:
          log.warn("Unknown mapping type: {}", mapping.getMappingType());
          throw new IllegalArgumentException("Unknown mapping type: " + mapping.getMappingType());
      }
    }
    compiled = true;
  }

  protected RepositoryPathMapping convert(final CPathMappingItem item) throws IllegalArgumentException {
    validate(item);
    MappingType type;
    switch (item.getRouteType()) {
      case CPathMappingItem.BLOCKING_RULE_TYPE:
        type = MappingType.BLOCKING;
        break;
      case CPathMappingItem.INCLUSION_RULE_TYPE:
        type = MappingType.INCLUSION;
        break;
      case CPathMappingItem.EXCLUSION_RULE_TYPE:
        type = MappingType.EXCLUSION;
        break;
      default:
        log.warn("Unknown route type: {}", item.getRouteType());
        throw new IllegalArgumentException("Unknown route type: " + item.getRouteType());
    }
    return new RepositoryPathMapping(item.getId(), type, item.getGroupId(), item.getRoutePatterns(),
        item.getRepositories());
  }

  protected CPathMappingItem convert(final RepositoryPathMapping item) throws IllegalArgumentException {
    validate(item);
    String routeType;
    switch (item.getMappingType()) {
      case BLOCKING:
        routeType = CPathMappingItem.BLOCKING_RULE_TYPE;
        break;
      case INCLUSION:
        routeType = CPathMappingItem.INCLUSION_RULE_TYPE;
        break;
      case EXCLUSION:
        routeType = CPathMappingItem.EXCLUSION_RULE_TYPE;
        break;
      default:
        log.warn("Unknown route type: {}", item.getMappingType());
        throw new IllegalArgumentException("Unknown route type: " + item.getMappingType());
    }
    final CPathMappingItem result = new CPathMappingItem();
    result.setId(item.getId());
    result.setGroupId(item.getGroupId());
    result.setRepositories(item.getMappedRepositories());
    result.setRouteType(routeType);
    final ArrayList<String> patterns = Lists.newArrayListWithCapacity(item.getPatterns().size());
    for (Pattern pattern : item.getPatterns()) {
      patterns.add(pattern.toString());
    }
    result.setRoutePatterns(patterns);
    return result;
  }

  private final Random rand = new Random(System.currentTimeMillis());

  protected boolean isBlank(final String str) {
    return (str == null || str.trim().length() == 0);
  }

  protected void validate(final CPathMappingItem pathItem)
      throws IllegalArgumentException
  {
    if (pathItem == null) {
      throw new IllegalArgumentException("CPathMappingItem cannot be null");
    }
    if (isBlank(pathItem.getId()) || "0".equals(pathItem.getId())) {
      // fix it
      pathItem.setId(Long.toHexString(System.nanoTime() + rand.nextInt(2008)));
    }
    if (isBlank(pathItem.getGroupId())) {
      // fix it
      pathItem.setGroupId(CPathMappingItem.ALL_GROUPS);
    }
    if (pathItem.getRoutePatterns() == null || pathItem.getRoutePatterns().isEmpty()) {
      throw new IllegalArgumentException("CPathMappingItem has no route patterns defined");
    }
    for (String regexp : pathItem.getRoutePatterns()) {
      try {
        Pattern.compile(regexp);
      }
      catch (PatternSyntaxException e) {
        throw new IllegalArgumentException("CPathMappingItem contains invalid pattern", e);
      }
    }
    if (!CPathMappingItem.INCLUSION_RULE_TYPE.equals(pathItem.getRouteType())
        && !CPathMappingItem.EXCLUSION_RULE_TYPE.equals(pathItem.getRouteType())
        && !CPathMappingItem.BLOCKING_RULE_TYPE.equals(pathItem.getRouteType())) {
      throw new IllegalArgumentException("CPathMappingItem has invalid type: " + pathItem.getRouteType());
    }
  }

  protected void validate(final RepositoryPathMapping pathItem)
      throws IllegalArgumentException
  {
    if (pathItem == null) {
      throw new IllegalArgumentException("RepositoryPathMapping cannot be null");
    }
    // 'id' is created when mapping added
    // 'type'
    if (pathItem.getMappingType() == null) {
      throw new IllegalArgumentException("RepositoryPathMapping 'type' cannot be null");
    }
    // 'groupId' is fixed when mapping added
    // 'patterns'
    if (pathItem.getPatterns() == null || pathItem.getPatterns().isEmpty()) {
      throw new IllegalArgumentException("RepositoryPathMapping 'patterns' cannot be null or empty");
    }
    // 'repositories'
    if (pathItem.getMappingType() != MappingType.BLOCKING &&
        (pathItem.getMappedRepositories() == null || pathItem.getMappedRepositories().isEmpty())) {
      throw new IllegalArgumentException("RepositoryPathMapping has no 'repositories' defined");
    }
  }

  /**
   * Handler for repository removal: if repository is removed, path mappings assigned to that repository
   * are removed too, and repository references from removed from all other mappings.
   */
  @Subscribe
  public void onEvent(final RepositoryRegistryEventRemove evt) {
    final String repoId = evt.getRepository().getId();
    final List<CPathMappingItem> pathMappings = getCurrentConfiguration(true).getPathMappings();
    for (Iterator<CPathMappingItem> iterator = pathMappings.iterator(); iterator.hasNext(); ) {
      final CPathMappingItem item = iterator.next();
      if (item.getGroupId().equals(repoId)) {
        iterator.remove();
      }
      else {
        item.removeRepository(repoId);
      }
    }
  }
}
