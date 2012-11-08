/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal.config;

import java.io.File;

import org.sonatype.nexus.repository.yum.internal.config.YumConfiguration;
import org.sonatype.nexus.repository.yum.internal.rest.AliasNotFoundException;

public interface YumPluginConfiguration
{
    public String getVersion( String repositoryId, String alias )
        throws AliasNotFoundException;

    public void setAlias( String repositoryId, String alias, String version );

    public YumConfiguration getXmlYumConfiguration();

    public void setFilename( String testConfFilename );

    public void saveConfig( YumConfiguration confToWrite );

    public void load();

    public File getConfigFile();

    public void setRepositoryOfRepositoryVersionsActive( boolean active );

    public boolean isRepositoryOfRepositoryVersionsActive();

    public boolean isDeleteProcessing();

    public void setDeleteProcessing( boolean active );

    public long getDelayAfterDeletion();

    public void setDelayAfterDeletion( long timeout );

    public File getBaseTempDir();

    public int getMaxParallelThreadCount();

    public boolean isActive();

    public void setActive( boolean active );
}
