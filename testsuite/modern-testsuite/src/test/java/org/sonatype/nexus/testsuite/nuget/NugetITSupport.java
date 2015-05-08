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
package org.sonatype.nexus.testsuite.nuget;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.sonatype.nexus.repository.nuget.internal.NugetHostedRecipe;
import com.sonatype.nexus.repository.nuget.odata.FeedSplicer;
import com.sonatype.nexus.repository.nuget.odata.ODataConsumer;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption.OverwriteMode;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * @since 3.0
 */
public class NugetITSupport
    extends NexusHttpsITSupport
{
  @org.ops4j.pax.exam.Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        withHttps(),
        wrappedBundle(maven("org.apache.httpcomponents", "httpmime").versionAsInProject())
            .overwriteManifest(OverwriteMode.FULL).instructions("DynamicImport-Package=*")
    );
  }

  @Inject
  private RepositoryManager repositoryManager;

  @NotNull
  protected Configuration hostedConfig(final String name) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(NugetHostedRecipe.NAME);
    config.setOnline(true);
    config.attributes("storage").set("writePolicy", WritePolicy.ALLOW.toString());
    return config;
  }

  /**
   * Creates a repository, first removing an existing one if necessary.
   */
  protected Repository createRepository(final Configuration config) throws Exception {
    waitFor(responseFrom(nexusUrl));
    return repositoryManager.create(config);
  }

  protected void deleteRepository(String name) throws Exception {
    repositoryManager.delete(name);
  }

  @NotNull
  protected NugetClient nugetClient(final Repository repository) throws Exception {
    final URL url = resolveUrl(nexusUrl, "/repository/" + repository.getName() + "/");
    waitFor(responseFrom(url));
    return new NugetClient(clientBuilder().build(), clientContext(), url.toURI());
  }

  protected List<Map<String, String>> parseFeedXml(final String entryXml) throws IOException, XmlPullParserException {
    final EntryList consumer = new EntryList();
    FeedSplicer splicer = new FeedSplicer(consumer);
    try (InputStream is = IOUtils.toInputStream(entryXml, "UTF-8")) {
      splicer.consumePage(is);
    }
    return consumer.getEntries();
  }

  public static class EntryList
      implements ODataConsumer
  {
    private final List<Map<String, String>> entries = new ArrayList<>();

    @Override
    public void consume(final Map<String, String> data) {
      entries.add(data);
    }

    public List<Map<String, String>> getEntries() {
      return entries;
    }
  }
}
