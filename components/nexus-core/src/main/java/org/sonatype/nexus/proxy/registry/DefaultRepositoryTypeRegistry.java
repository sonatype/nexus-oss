/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.maven.maven1.M1GroupRepository;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepository;
import org.sonatype.nexus.proxy.maven.maven1.M1Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = RepositoryTypeRegistry.class)
public class DefaultRepositoryTypeRegistry
    implements RepositoryTypeRegistry
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Requirement
  private PlexusContainer container;

  @Requirement(role = ContentClass.class)
  private Map<String, ContentClass> contentClasses;

  private Map<String, ContentClass> repoCachedContentClasses = new HashMap<String, ContentClass>();

  private Multimap<Class<? extends Repository>, RepositoryTypeDescriptor> repositoryTypeDescriptorsMap;

  protected Multimap<Class<? extends Repository>, RepositoryTypeDescriptor> getRepositoryTypeDescriptors() {
    synchronized (this) {
      // maybe the previous who was blocking us already did the job
      if (repositoryTypeDescriptorsMap == null) {
        Multimap<Class<? extends Repository>, RepositoryTypeDescriptor> result =
            ArrayListMultimap.create();

        // fill in the defaults
        Class<? extends Repository> role = null;

        role = Repository.class;

        result.put(role, new RepositoryTypeDescriptor(role, M1Repository.ID, "repositories",
            RepositoryType.UNLIMITED_INSTANCES));
        result.put(role, new RepositoryTypeDescriptor(role, M2Repository.ID, "repositories",
            RepositoryType.UNLIMITED_INSTANCES));

        role = ShadowRepository.class;

        result.put(role, new RepositoryTypeDescriptor(role, M1LayoutedM2ShadowRepository.ID, "shadows",
            RepositoryType.UNLIMITED_INSTANCES));
        result.put(role, new RepositoryTypeDescriptor(role, M2LayoutedM1ShadowRepository.ID, "shadows",
            RepositoryType.UNLIMITED_INSTANCES));

        role = GroupRepository.class;

        result.put(role, new RepositoryTypeDescriptor(role, M1GroupRepository.ID, "groups",
            RepositoryType.UNLIMITED_INSTANCES));
        result.put(role, new RepositoryTypeDescriptor(role, M2GroupRepository.ID, "groups",
            RepositoryType.UNLIMITED_INSTANCES));

        // No implementation exists in core!
        // role = WebSiteRepository.class.getName();

        // result.put( role, new RepositoryTypeDescriptor( role, XXX, "sites" ) );

        logger.info("Registered default repository types.");

        this.repositoryTypeDescriptorsMap = result;
      }

      return repositoryTypeDescriptorsMap;
    }
  }

  public Set<RepositoryTypeDescriptor> getRegisteredRepositoryTypeDescriptors() {
    return Collections.unmodifiableSet(new HashSet<RepositoryTypeDescriptor>(
        getRepositoryTypeDescriptors().values()));
  }

  public boolean registerRepositoryTypeDescriptors(RepositoryTypeDescriptor d) {
    boolean added = getRepositoryTypeDescriptors().put(d.getRole(), d);

    if (added) {
      if (d.getRepositoryMaxInstanceCount() == RepositoryType.UNLIMITED_INSTANCES) {
        logger.info("Registered Repository type " + d.toString() + ".");
      }
      else {
        logger.info(
            "Registered Repository type " + d.toString() + " with maximal instance limit set to "
                + d.getRepositoryMaxInstanceCount() + ".");
      }
    }

    return added;
  }

  public boolean unregisterRepositoryTypeDescriptors(RepositoryTypeDescriptor d) {
    boolean removed = getRepositoryTypeDescriptors().remove(d.getRole(), d);

    if (removed) {
      logger.info("Unregistered repository type " + d.toString());
    }

    return removed;
  }

  public Map<String, ContentClass> getContentClasses() {
    return Collections.unmodifiableMap(new HashMap<String, ContentClass>(contentClasses));
  }

  public Set<String> getCompatibleContentClasses(ContentClass contentClass) {
    Set<String> compatibles = new HashSet<String>();

    for (ContentClass cc : contentClasses.values()) {
      if (cc.isCompatible(contentClass) || contentClass.isCompatible(cc)) {
        compatibles.add(cc.getId());
      }
    }

    return compatibles;
  }

  public Set<Class<? extends Repository>> getRepositoryRoles() {
    Set<RepositoryTypeDescriptor> rtds = getRegisteredRepositoryTypeDescriptors();

    HashSet<Class<? extends Repository>> result = new HashSet<Class<? extends Repository>>(rtds.size());

    for (RepositoryTypeDescriptor rtd : rtds) {
      result.add(rtd.getRole());
    }

    return Collections.unmodifiableSet(result);
  }

  public Set<String> getExistingRepositoryHints(Class<? extends Repository> role) {
    if (!getRepositoryTypeDescriptors().containsKey(role)) {
      return Collections.emptySet();
    }

    HashSet<String> result = new HashSet<String>();

    for (RepositoryTypeDescriptor rtd : getRepositoryTypeDescriptors().get(role)) {
      result.add(rtd.getHint());
    }

    return result;
  }

  public RepositoryTypeDescriptor getRepositoryTypeDescriptor(Class<? extends Repository> role, String hint) {
    if (!getRepositoryTypeDescriptors().containsKey(role)) {
      return null;
    }

    for (RepositoryTypeDescriptor rtd : getRepositoryTypeDescriptors().get(role)) {
      if (rtd.getHint().equals(hint)) {
        return rtd;
      }
    }

    return null;
  }

  @Deprecated
  public RepositoryTypeDescriptor getRepositoryTypeDescriptor(String role, String hint) {
    Class<? extends Repository> roleClass = null;

    for (Class<? extends Repository> registeredClass : getRepositoryTypeDescriptors().keySet()) {
      if (registeredClass.getName().equals(role)) {
        roleClass = registeredClass;

        break;
      }
    }

    if (roleClass == null) {
      return null;
    }

    for (RepositoryTypeDescriptor rtd : getRepositoryTypeDescriptors().get(roleClass)) {
      if (rtd.getHint().equals(hint)) {
        return rtd;
      }
    }

    return null;
  }

  // TODO: solve this single place to cease the requirement for PlexusContainer!
  public ContentClass getRepositoryContentClass(Class<? extends Repository> role, String hint) {
    if (!getRepositoryRoles().contains(role)) {
      return null;
    }

    ContentClass result = null;

    String cacheKey = role + ":" + hint;

    if (repoCachedContentClasses.containsKey(cacheKey)) {
      result = repoCachedContentClasses.get(cacheKey);
    }
    else {
      if (container.hasComponent(role, hint)) {
        try {
          Repository repository = container.lookup(role, hint);

          result = repository.getRepositoryContentClass();

          container.release(repository);

          repoCachedContentClasses.put(cacheKey, result);
        }
        catch (ComponentLookupException e) {
          logger.warn("Container contains a component but lookup failed!", e);
        }
        catch (ComponentLifecycleException e) {
          logger.warn("Could not release the component! Possible leak here.", e);
        }
      }
      else {
        return null;
      }
    }

    return result;
  }
}
