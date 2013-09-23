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
import java.io.IOException;
import java.util.Date;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * AttributeStorage implementation driven by XStream.
 *
 * @author cstamas
 */
public class DefaultAttributeStorageIT
    extends AbstractNexusTestEnvironment
{

  protected AttributeStorage attributeStorage;

  protected RepositoryItemUidFactory repositoryItemUidFactory;

  protected Repository repository;

  protected File localStorageDirectory;

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    attributeStorage = lookup(AttributeStorage.class, "fs");

    repositoryItemUidFactory = lookup(RepositoryItemUidFactory.class);

    repository = lookup(Repository.class, "maven2");

    CRepository repoConf = new DefaultCRepository();

    repoConf.setProviderRole(Repository.class.getName());
    repoConf.setProviderHint("maven2");
    repoConf.setId("dummy");

    repoConf.setLocalStorage(new CLocalStorage());
    repoConf.getLocalStorage().setProvider("file");
    localStorageDirectory = new File(getBasedir(), "target/test-reposes/repo1");
    repoConf.getLocalStorage().setUrl(localStorageDirectory.toURI().toURL().toString());

    Xpp3Dom exRepo = new Xpp3Dom("externalConfiguration");
    repoConf.setExternalConfiguration(exRepo);
    M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration(exRepo);
    exRepoConf.setRepositoryPolicy(RepositoryPolicy.RELEASE);
    exRepoConf.setChecksumPolicy(ChecksumPolicy.STRICT_IF_EXISTS);

    if (attributeStorage instanceof DefaultFSAttributeStorage) {
      FileUtils.deleteDirectory(((DefaultFSAttributeStorage) attributeStorage).getWorkingDirectory());
    }
    else if (attributeStorage instanceof DefaultLSAttributeStorage) {
      FileUtils.deleteDirectory(new File(localStorageDirectory, ".nexus/attributes"));
    }

    repository.configure(repoConf);
  }

  @Test
  public void testSimplePutGet()
      throws Exception
  {
    DefaultStorageFileItem file =
        new DefaultStorageFileItem(repository, new ResourceStoreRequest("/a.txt"), true, true,
            new StringContentLocator("CONTENT"));

    file.getRepositoryItemAttributes().put("kuku", "kuku");

    attributeStorage.putAttributes(file.getRepositoryItemUid(), file.getRepositoryItemAttributes());

    RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(repository, "/a.txt");
    Attributes file1 = attributeStorage.getAttributes(uid);

    assertTrue(file1.containsKey("kuku"));
    assertTrue("kuku".equals(file1.get("kuku")));
  }

  @Test
  public void testSimplePutGetNEXUS3911()
      throws Exception
  {
    DefaultStorageFileItem file =
        new DefaultStorageFileItem(repository, new ResourceStoreRequest("/a.txt"), true, true,
            new StringContentLocator("CONTENT"));

    file.getRepositoryItemAttributes().put("kuku", "kuku");

    attributeStorage.putAttributes(file.getRepositoryItemUid(), file.getRepositoryItemAttributes());

    RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(repository, "/a.txt");
    Attributes file1 = attributeStorage.getAttributes(uid);

    assertTrue(file1.containsKey("kuku"));
    assertTrue("kuku".equals(file1.get("kuku")));

    // this above is same as in testSimplePutGet(), but now we will replace the attribute file

    // reverted back to "old" attributes
    File attributeFile =
        new File(((DefaultFSAttributeStorage) attributeStorage).getWorkingDirectory(), repository.getId()
            + "/a.txt");
    // File attributeFile = new File( localStorageDirectory, ".nexus/attributes/a.txt" );

    FileUtils.fileWrite(attributeFile.getAbsolutePath(), "<file");

    // try to read it, we should not get NPE
    try {
      file1 = attributeStorage.getAttributes(uid);
    }
    catch (NullPointerException e) {
      fail("We should not get NPE!");
    }

    assertNull("file1 is corrupt, hence it should be null!", file1);
  }

  @Test
  public void testSimplePutDelete()
      throws Exception
  {
    DefaultStorageFileItem file =
        new DefaultStorageFileItem(repository, new ResourceStoreRequest("/b.txt"), true, true,
            new StringContentLocator("CONTENT"));

    file.getRepositoryItemAttributes().put("kuku", "kuku");

    attributeStorage.putAttributes(file.getRepositoryItemUid(), file.getRepositoryItemAttributes());

    RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(repository, "/b.txt");

    assertNotNull(attributeStorage.getAttributes(uid));

    assertTrue(attributeStorage.deleteAttributes(uid));

    assertNull(attributeStorage.getAttributes(uid));
  }

  @Test
  public void testZeroLenghtFSAttributeStorage()
      throws Exception
  {
    // NEXUS-4871
    ApplicationConfiguration appCfg = lookup(ApplicationConfiguration.class);
    File workDir = appCfg.getWorkingDirectory("proxy/attributes-ng");
    workDir.mkdirs();

    RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(repository, "a/b/c.txt");

    File attFile = new File(workDir, repository.getId() + "/" + uid.getPath());
    attFile.getParentFile().mkdirs();

    // make a zero length file
    attFile.createNewFile();
    assertThat(attFile, FileMatchers.sized(0L));

    final DefaultFSAttributeStorage fsStorage = new DefaultFSAttributeStorage(appCfg);

    // check getAttributes is gonna delete it
    Attributes att = fsStorage.getAttributes(uid);
    assertThat(att, nullValue());
    assertThat(attFile, not(FileMatchers.exists()));

    DefaultStorageFileItem file =
        new DefaultStorageFileItem(repository, new ResourceStoreRequest(uid.getPath()), true, true,
            new StringContentLocator("CONTENT"));

    long creationTime = createFile(attFile);
    assertThat(attFile, FileMatchers.sized(0L));

    att = file.getRepositoryItemAttributes();
    fsStorage.putAttributes(uid, att);

    // put shall create the file
    assertThat(attFile, FileMatchers.exists());
    // must be newer then the zero length file
    assertThat(attFile.lastModified(), greaterThan(creationTime));

    Attributes retAtt = fsStorage.getAttributes(uid);
    assertThat(retAtt, notNullValue());
  }

  @Test
  public void testZeroLenghtLSAttributeStorage()
      throws Exception
  {
    // NEXUS-4871
    RepositoryItemUid uid = getRepositoryItemUidFactory().createUid(repository, "a/b/c.txt");

    File attFile = new File(localStorageDirectory, ".nexus/attributes/" + uid.getPath());
    attFile.getParentFile().mkdirs();

    // cleanup
    attFile.delete();
    new File(localStorageDirectory, ".nexus/trash/.nexus/attributes/" + uid.getPath()).delete();

    // make a zero length file
    attFile.createNewFile();
    assertThat(attFile, FileMatchers.sized(0L));

    final DefaultLSAttributeStorage lsStorage = new DefaultLSAttributeStorage();

    // check getAttributes is gonna delete it
    Attributes att = lsStorage.getAttributes(uid);
    assertThat(att, nullValue());
    assertThat(attFile, not(FileMatchers.exists()));

    DefaultStorageFileItem file =
        new DefaultStorageFileItem(repository, new ResourceStoreRequest(uid.getPath()), true, true,
            new StringContentLocator("CONTENT"));

    // make a zero length file
    long creationTime = createFile(attFile);
    assertThat(attFile, FileMatchers.sized(0L));

    att = file.getRepositoryItemAttributes();
    lsStorage.putAttributes(uid, att);

    // put shall create the file
    assertThat(attFile, FileMatchers.exists());
    // must be newer then the zero length file
    assertThat(attFile.lastModified(), greaterThan(creationTime));

    Attributes retAtt = lsStorage.getAttributes(uid);
    assertThat(retAtt, notNullValue());
  }

  protected long createFile(File attFile)
      throws IOException, InterruptedException
  {
    attFile.createNewFile();
    long creationTime = new Date().getTime();

    // https://github.com/sonatype/nexus/pull/308#r460989
    // This may fail depending on the filesystem timestamp resolution (ext3 uses 1s IIRC)
    Thread.sleep(1500);

    return creationTime;
  }

}
