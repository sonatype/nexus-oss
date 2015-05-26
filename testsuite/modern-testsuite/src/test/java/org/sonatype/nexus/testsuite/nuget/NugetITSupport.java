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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.sonatype.nexus.repository.nuget.internal.NugetHostedRecipe;
import com.sonatype.nexus.repository.nuget.odata.FeedSplicer;
import com.sonatype.nexus.repository.nuget.odata.ODataConsumer;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;
import org.sonatype.nexus.testsuite.repository.RepositoryTestSupport;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption.OverwriteMode;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * Support for Nuget ITs
 */
public abstract class NugetITSupport
    extends RepositoryTestSupport
{
  public static final String VISUAL_STUDIO_INITIAL_COUNT_QUERY =
      "Search()/$count?$filter=IsLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=false";

  public static final String VISUAL_STUDIO_INITIAL_FEED_QUERY =
      "Search()?$filter=IsLatestVersion&$orderby=DownloadCount%20desc,Id&$skip=0&$top=30&searchTerm=''&targetFramework='net45'&includePrerelease=false";

  public static final int VS_DEFAULT_PAGE_REQUEST_SIZE = 30;

  @org.ops4j.pax.exam.Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        withHttps(),
        wrappedBundle(maven("org.apache.httpcomponents", "httpmime").versionAsInProject())
            .overwriteManifest(OverwriteMode.FULL).instructions("DynamicImport-Package=*"),
        // TODO: This should be replaced with:
        // mavenBundle("org.sonatype.http-testing-harness", "server-provider").versionAsInProject()
        // ..once the http-testing-harness duplicate import of org.sonatype.tests.http.server.api is corrected.
        wrappedBundle(maven("org.sonatype.http-testing-harness", "server-provider").versionAsInProject())
            .overwriteManifest(OverwriteMode.FULL).instructions("DynamicImport-Package=*")
    );
  }

  @NotNull
  protected Configuration hostedConfig(final String name) {
    final Configuration config = new Configuration();
    config.setRepositoryName(name);
    config.setRecipeName(NugetHostedRecipe.NAME);
    config.setOnline(true);
    config.attributes("storage").set("writePolicy", WritePolicy.ALLOW.toString());
    return config;
  }

  @NotNull
  protected NugetClient nugetClient(final Repository repository) throws Exception {
    final URL url = repositoryBaseUrl(repository);
    waitFor(responseFrom(url));
    return new NugetClient(clientBuilder().build(), clientContext(), url.toURI());
  }

  protected ParsedFeed parse(final String feedXml) throws Exception {
    return new ParsedFeed(feedXml);
  }

  protected List<Map<String, String>> parseFeedXml(final String entryXml) throws Exception {
    return parse(entryXml).getEntries();
  }

  protected Integer parseInlineCount(final String entryXml) throws Exception, XmlPullParserException {
    return parse(entryXml).getInlineCount();
  }

  protected String parseNextPageUrl(final String entryXml) throws Exception, XmlPullParserException {
    return parse(entryXml).getNextPageUrl();
  }

  /**
   * A utility to parse feeds
   */
  protected static class ParsedFeed
  {
    private final FeedSplicer splicer;

    private final EntryList consumer;

    private final String nextPageUrl;

    public ParsedFeed(final String feedXml) throws Exception {
      consumer = new EntryList();
      splicer = new FeedSplicer(consumer);
      try (InputStream is = IOUtils.toInputStream(feedXml, "UTF-8")) {
        nextPageUrl = splicer.consumePage(is);
      }
    }

    public List<Map<String, String>> getEntries() {
      return consumer.getEntries();
    }

    @Nullable
    public Integer getInlineCount() {
      return splicer.getCount();
    }

    @Nullable
    public String getNextPageUrl() {
      return nextPageUrl;
    }
  }

  private static class EntryList
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
