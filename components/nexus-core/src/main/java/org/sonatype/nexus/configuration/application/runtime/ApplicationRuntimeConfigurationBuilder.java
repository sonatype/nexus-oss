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

package org.sonatype.nexus.configuration.application.runtime;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A component to be slimmed! Actually, it is a "factory" (backed by Plexus) that creates repo and other instances. It
 * should realy onto plexus as much can.
 *
 * @author cstamas
 */
public interface ApplicationRuntimeConfigurationBuilder
{
  Repository createRepositoryFromModel(Configuration configuration, CRepository repoConf)
      throws ConfigurationException;

  void releaseRepository(Repository repository, Configuration configuration, CRepository repoConf)
      throws ConfigurationException;
}
