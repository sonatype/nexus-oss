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
package org.sonatype.nexus.configuration.source;

import java.io.IOException;

import org.sonatype.nexus.configuration.model.Configuration;

/**
 * The Interface ApplicationConfigurationSource, responsible to fetch Nexus user configuration by some means. It also
 * stores one instance of Configuration object maintained thru life of Nexus. This component is also able to persist
 * user config.
 *
 * @author cstamas
 */
public interface ApplicationConfigurationSource
{
  void storeConfiguration() throws IOException;

  Configuration getConfiguration();

  void setConfiguration(Configuration configuration);

  Configuration loadConfiguration() throws IOException;
}
