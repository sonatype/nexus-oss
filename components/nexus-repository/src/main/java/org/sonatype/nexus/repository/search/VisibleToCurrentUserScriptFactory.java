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
package org.sonatype.nexus.repository.search;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.security.BreadActions;
import org.sonatype.nexus.repository.security.RepositoryViewPermission;
import org.sonatype.nexus.security.SecurityHelper;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_GROUP;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

/**
 * @since 3.0
 */
@Named(VisibleToCurrentUserScriptFactory.NAME)
@Singleton
public class VisibleToCurrentUserScriptFactory
    extends ComponentSupport
    implements NativeScriptFactory
{

  public static final String NAME = "visibleToCurrentUser";

  private final SecurityHelper securityHelper;

  @Inject
  public VisibleToCurrentUserScriptFactory(final SecurityHelper securityHelper) {
    this.securityHelper = checkNotNull(securityHelper);
  }

  @Override
  public ExecutableScript newScript(@Nullable final Map<String, Object> params) {
    return new AbstractSearchScript()
    {
      @Override
      public Object run() {
        if (!SearchServiceImpl.TYPE.equals(docFieldStrings("_type").getValue())) {
          return true;
        }
        if (log.isDebugEnabled()) {
          log.trace("Verify permissions of {}:{}",
              docFieldStrings(P_GROUP).getValue(),
              docFieldStrings(P_NAME).getValue()
          );
        }
        String format = docFieldStrings(P_FORMAT).getValue();
        String repositoryName = docFieldStrings(P_REPOSITORY_NAME).getValue();
        return securityHelper.allPermitted(
            new RepositoryViewPermission(format, repositoryName, BreadActions.BROWSE)
            // TODO add repository component permission
        );
      }
    };
  }

}
