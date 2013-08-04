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

package org.sonatype.nexus.proxy.attributes;

import java.io.File;

import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * AttributeStorage implementation driven by XStream.
 *
 * @author cstamas
 */
public class AbstractAttributesHandlerTest
    extends AbstractNexusTestEnvironment
{

  protected DefaultAttributesHandler attributesHandler;

  protected Repository repository;

  public void setUp()
      throws Exception
  {
    super.setUp();

    FileUtils.copyDirectoryStructure(new File(getBasedir(), "target/test-classes/repo1"), new File(
        getBasedir(), "target/test-reposes/repo1"));

    attributesHandler = (DefaultAttributesHandler) lookup(AttributesHandler.class);

    repository = lookup(Repository.class, "maven2");

    CRepository repoConf = new DefaultCRepository();

    repoConf.setProviderRole(Repository.class.getName());
    repoConf.setProviderHint("maven2");
    repoConf.setId("dummy");

    repoConf.setLocalStorage(new CLocalStorage());
    repoConf.getLocalStorage().setProvider("file");
    File localStorageDirectory = new File(getBasedir(), "target/test-reposes/repo1");
    repoConf.getLocalStorage().setUrl(localStorageDirectory.toURI().toURL().toString());

    Xpp3Dom exRepo = new Xpp3Dom("externalConfiguration");
    repoConf.setExternalConfiguration(exRepo);
    M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration(exRepo);
    exRepoConf.setRepositoryPolicy(RepositoryPolicy.RELEASE);
    exRepoConf.setChecksumPolicy(ChecksumPolicy.STRICT_IF_EXISTS);

    if (attributesHandler.getAttributeStorage().getDelegate() instanceof DefaultFSAttributeStorage) {
      FileUtils.deleteDirectory(
          ((DefaultFSAttributeStorage) attributesHandler.getAttributeStorage().getDelegate()).getWorkingDirectory());
    }
    else if (attributesHandler.getAttributeStorage().getDelegate() instanceof DefaultLSAttributeStorage) {
      FileUtils.deleteDirectory(new File(localStorageDirectory, ".nexus/attributes"));
    }
    else if (attributesHandler.getAttributeStorage().getDelegate() instanceof TransitioningAttributeStorage) {
      FileUtils.deleteDirectory(new File(localStorageDirectory, ".nexus/attributes"));
    }

    repository.configure(repoConf);
  }
}
