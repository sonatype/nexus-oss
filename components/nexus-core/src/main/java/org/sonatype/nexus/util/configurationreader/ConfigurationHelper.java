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

package org.sonatype.nexus.util.configurationreader;

import java.io.File;
import java.util.concurrent.locks.Lock;

import org.sonatype.configuration.upgrade.ConfigurationUpgrader;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;

@SuppressWarnings("deprecation")
public interface ConfigurationHelper
{
  public <E extends org.sonatype.configuration.Configuration> E load(E emptyInstance, String modelVersion,
                                                                     File configurationFile, Lock lock,
                                                                     ConfigurationReader<E> reader,
                                                                     ConfigurationValidator<E> validator,
                                                                     ConfigurationUpgrader<E> upgrader);

  public <E> void save(E configuration, File configurationFile,
                       ConfigurationWritter<E> configurationXpp3Writter, Lock lock);

}
