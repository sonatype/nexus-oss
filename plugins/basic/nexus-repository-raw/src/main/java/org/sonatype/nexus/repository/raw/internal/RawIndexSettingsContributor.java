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
package org.sonatype.nexus.repository.raw.internal;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.search.IndexSettingsContributor;

import com.google.common.io.Resources;

import static com.google.common.base.Charsets.UTF_8;

/**
 * Contributor to ES index settings.
 *
 * @since 3.0
 */
@Named
@Singleton
public class RawIndexSettingsContributor
    implements IndexSettingsContributor
{

  @Override
  public String getIndexSettings(final Repository repository) throws IOException {
    if (RawFormat.NAME.equals(repository.getFormat().getValue())) {
      return Resources.toString(Resources.getResource(RawIndexSettingsContributor.class, "es-mapping-raw.json"), UTF_8);
    }
    return null;
  }

}
