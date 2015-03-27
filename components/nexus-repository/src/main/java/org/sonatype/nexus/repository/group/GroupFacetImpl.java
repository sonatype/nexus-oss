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
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;

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

  private final Set<String> memberNames = Sets.newLinkedHashSet();

  @Inject
  public GroupFacetImpl(final RepositoryManager repositoryManager) {
    this.repositoryManager = checkNotNull(repositoryManager);
  }

  // TODO: Check for compatibility and cyclic-references

  @Override
  protected void doConfigure() throws Exception {
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    memberNames.addAll(attributes.require("memberNames", COLLECTION_STRING));
    log.debug("Members names: {}", memberNames);
  }

  @Override
  protected void doDestroy() throws Exception {
    memberNames.clear();
  }

  @Override
  @Guarded(by = STARTED)
  public boolean member(final Repository repository) {
    checkNotNull(repository);
    return memberNames.contains(repository.getName());
  }

  @Override
  @Guarded(by = STARTED)
  public List<Repository> members() {
    List<Repository> members = new ArrayList<>(memberNames.size());
    for (String name : memberNames) {
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
