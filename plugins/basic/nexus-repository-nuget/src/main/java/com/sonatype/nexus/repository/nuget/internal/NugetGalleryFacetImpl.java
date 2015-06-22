/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import com.sonatype.nexus.repository.nuget.internal.ComponentQuery.Builder;
import com.sonatype.nexus.repository.nuget.odata.NugetPackageUtils;
import com.sonatype.nexus.repository.nuget.odata.ODataFeedUtils;
import com.sonatype.nexus.repository.nuget.odata.ODataTemplates;
import com.sonatype.nexus.repository.nuget.odata.ODataUtils;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.TempStreamSupplier;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.time.Clock;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.group.GroupFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.xml.XmlEscapers;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.joda.time.DateTime;
import org.odata4j.producer.InlineCount;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Strings.nullToEmpty;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.*;
import static java.util.Collections.singletonList;
import static org.odata4j.producer.resources.OptionsQueryParser.parseInlineCount;
import static org.odata4j.producer.resources.OptionsQueryParser.parseSkip;
import static org.odata4j.producer.resources.OptionsQueryParser.parseTop;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME;

/**
 * A group-aware implementation of a hosted {@link} NugetGalleryFacet.
 *
 * @since 3.0
 */
@Named("local")
public class NugetGalleryFacetImpl
    extends FacetSupport
    implements NugetGalleryFacet
{
  public static final String WITH_NAMESPACES =
      " xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" " +
          "xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\"";

  public static final String NO_NAMESPACES = "";

  protected Clock clock = new Clock();

  private StorageFacet storage;

  private static final VersionScheme SCHEME = new GenericVersionScheme();

  private static final String P_LAST_VERIFIED_DATE = "last_verified";

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    super.doInit(configuration);
    storage = facet(StorageFacet.class);
  }

  @Override
  protected void doDestroy() throws Exception {
    storage = null;
  }

  @Override
  @Guarded(by = STARTED)
  public int count(final String operation, final Map<String, String> query) {
    log.debug("Count: {}", query);

    return count(operation, query, getRepositories());
  }

  protected int count(@SuppressWarnings("unused") final String operation,
                      final Map<String, String> query,
                      final Iterable<Repository> repositories)
  {
    final ComponentQuery componentQuery = ODataUtils.query(query, true);

    try (StorageTx storageTx = openStorageTx()) {
      int count = executeCount(componentQuery, storageTx, repositories);

      final Integer skip = parseSkip(query.get("$skip"));
      if (null != skip && skip >= 0) {
        // If we were asked to skip some values, deduct this from the total count reported by the query
        count = Math.max(0, count - skip);
      }
      final Integer top = parseTop(query.get("$top"));
      if (null != top && top >= 0) {
        // If we were asked for the top 'n' values, then cap the count at no more than this value
        count = Math.min(count, top.intValue());
      }

      storageTx.commit();
      return count;
    }
  }

  @Override
  @Guarded(by = STARTED)
  public String feed(final String base, final String operation, final Map<String, String> query) {
    log.debug("Select: {}", query);

    final List<Repository> repositories = getRepositories();

    FeedResult result = feed(base, operation, query, repositories);

    return renderFeedResults(result);
  }

  /**
   * Generate a {@link FeedResult} for a given feed query.
   */
  protected FeedResult feed(final String base, final String operation, final Map<String, String> query,
                            final Iterable<Repository> repositories)
  {
    FeedResult result = new FeedResult(base, operation, query);

    try (StorageTx storageTx = openStorageTx()) {

      // NXCM-4502 add inlinecount only if requested
      if (inlineCountRequested(query)) {
        int inlineCount = executeCount(ODataUtils.query(query, true), storageTx, repositories);

        result.setCount(inlineCount);
      }

      final ComponentQuery componentQuery = ODataUtils.query(query, false);
      final Iterable<Asset> assets = storageTx.findAssets(componentQuery.getWhere(),
          componentQuery.getParameters(), getRepositories(), componentQuery.getQuerySuffix());

      int n = 0;
      for (Asset asset : assets) {
        n++;
        final NestedAttributesMap nugetAttributes = asset.formatAttributes();
        final Map<String, ?> data = toData(nugetAttributes, extraTemplateVars(base, operation));

        result.addEntry(data);
        if (n == ODataUtils.PAGE_SIZE) {
          result.setSkipLinkEntry(data);
        }
      }

      storageTx.commit();
    }
    return result;
  }

  protected String renderFeedResults(final FeedResult result)
  {
    final StringBuilder xml = new StringBuilder();
    xml.append(interpolateTemplate(ODataTemplates.NUGET_FEED,
        extraTemplateVars(result.getBase(), result.getOperation())));

    if (result.getCount() != null) {
      xml.append(interpolateTemplate(ODataTemplates.NUGET_INLINECOUNT,
          ImmutableMap.of("COUNT", String.valueOf(result.getCount()))
      ));
    }
    for (Map<String, ?> data : result.getEntries()) {
      xml.append(interpolateTemplate(ODataTemplates.NUGET_ENTRY, data));
    }

    if (result.getSkipLinkEntry() != null) {
      final String skipLink = odataLink(result.getBase(),
          result.getOperation(),
          ODataFeedUtils.skipLinkQueryString(result.getQuery()));

      xml.append("  <link rel=\"next\" href=\"")
          .append(XmlEscapers.xmlAttributeEscaper().escape(skipLink))
          .append("\"/>\n");
    }
    return xml.append("</feed>").toString();
  }

  public String odataLink(final String base, final String operation, final String queryString) {
    return String.format("%s/%s()?%s", base, operation, queryString);
  }

  @Override
  public void putMetadata(final Map<String, String> metadata) {
    try (StorageTx tx = openStorageTx()) {
      createOrUpdatePackage(tx, metadata);
      tx.commit();
    }

    // Separate tx is necessary for the meantime, since the re-querying orient doesn't pick up uncommitted state
    try (StorageTx tx = openStorageTx()) {
      maintainAggregateInfo(tx, metadata.get(ID));
      tx.commit();
    }
  }

  @Override
  public void putContent(final String id, final String version, final InputStream content) throws IOException {
    try (StorageTx tx = openStorageTx()) {
      Component component = findComponent(tx, id, version);
      checkState(
          component != null && tx.browseAssets(component).iterator().hasNext(),
          "Component metadata does not exist yet"
      );
      createOrUpdateAssetAndContents(tx, component, content, null);
      tx.commit();
    }
  }

  @VisibleForTesting
  Map<String, ?> toData(final NestedAttributesMap nugetAttributes, Map<String, String> extras) {
    Map<String, Object> data = Maps.newHashMap();

    for (Entry<String, Object> attrib : nugetAttributes.entries()) {
      data.put(attrib.getKey(), attrib.getValue());
    }

    for (Entry<String, String> extra : extras.entrySet()) {
      data.put(extra.getKey(), extra.getValue());
    }

    return data;
  }

  @Override
  public String entry(final String base, final String id, final String version) {
    final Map<String, String> extra = ImmutableMap.of("BASEURI", base, "NAMESPACES", WITH_NAMESPACES);

    final StringBuilder xml = new StringBuilder();
    try (StorageTx tx = openStorageTx()) {
      final Component component = findComponent(tx, id, version);
      if (component == null) {
        return null;
      }

      final Asset asset = findAsset(tx, component);
      checkState(asset != null);
      final Map<String, ?> entryData = toData(asset.formatAttributes(), extra);

      final String nugetEntry = ODataTemplates.NUGET_ENTRY;
      xml.append(interpolateTemplate(nugetEntry, entryData));
    }
    return xml.toString();
  }

  @VisibleForTesting
  String interpolateTemplate(final String template, final Map<String, ?> entryData) {
    return ODataTemplates
        .interpolate(template, entryData);
  }

  private Map<String, String> extraTemplateVars(final String base, final String operation) {
    return ImmutableMap.of("BASEURI", base, "ENDPOINT", operation, LAST_UPDATED,
        ODataFeedUtils.datetime(System.currentTimeMillis()), "NAMESPACES", NO_NAMESPACES);
  }

  @Override
  @Guarded(by = STARTED)
  public void put(final InputStream inputStream) throws IOException, NugetPackageException {
    String componentId;

    try (StorageTx storageTx = openStorageTx();
         TempStreamSupplier tempStream = new TempStreamSupplier(inputStream)) {

      final Map<String, String> recordMetadata = Maps.newHashMap();
      Map<String, String> packageMetadata;
      try (InputStream in = tempStream.get()) {
        packageMetadata = NugetPackageUtils.packageMetadata(in);
      }

      recordMetadata.putAll(packageMetadata);

      // Add time-related metadata, not present in the .nuspec
      final String creationTime = ODataFeedUtils.datetime(clock.millis());
      recordMetadata.put(CREATED, creationTime);
      recordMetadata.put(LAST_UPDATED, creationTime);
      recordMetadata.put(PUBLISHED, creationTime);

      try (InputStream in = tempStream.get()) {
        createOrUpdatePackageAndContents(storageTx, recordMetadata, in);
      }

      componentId = recordMetadata.get(ID);
      storageTx.commit();
    }

    if (componentId != null) {
      try (StorageTx storageTx = openStorageTx()) {
        maintainAggregateInfo(storageTx, componentId);
        storageTx.commit();
      }
    }
  }

  @Override
  @Guarded(by = STARTED)
  public Payload get(final String id, final String version) throws IOException {
    checkNotNull(id);
    checkNotNull(version);

    try (StorageTx tx = openStorageTx()) {
      Asset asset = findAsset(tx, id, version);
      if (asset == null) {
        return null;
      }

      final BlobRef blobRef = asset.blobRef();
      if (blobRef == null) {
        return null;
      }

      final Blob blob = tx.requireBlob(blobRef);
      String contentType = asset.contentType();

      return new StreamPayload(
          new InputStreamSupplier()
          {
            @Nonnull
            @Override
            public InputStream get() throws IOException {
              return blob.getInputStream();
            }
          },
          blob.getMetrics().getContentSize(),
          contentType
      );
    }
  }

  @Override
  public DateTime getLastVerified(final String id, final String version) {
    checkNotNull(id);
    checkNotNull(version);

    try (StorageTx tx = openStorageTx()) {
      final Asset asset = findAsset(tx, id, version);

      checkState(asset != null);

      Date date = asset.formatAttributes().get(P_LAST_VERIFIED_DATE, Date.class);

      log.debug("Content for {} {} last verified as of {}", id, version, date);
      return new DateTime(checkNotNull(date));
    }
  }

  @Override
  public void setLastVerified(final String id, final String version, final DateTime date) {
    checkNotNull(date);
    try (StorageTx tx = openStorageTx()) {
      Asset asset = findAsset(tx, id, version);
      checkState(asset != null);

      final Date priorDate = asset.formatAttributes().get(P_LAST_VERIFIED_DATE, Date.class);
      log.debug("Updating last verified date of {} {} from {} to {}", id, version, priorDate, date);
      asset.formatAttributes().set(P_LAST_VERIFIED_DATE, date.toDate());
      tx.commit();
    }
  }

  @Override
  public boolean delete(final String id, final String version) throws IOException {
    checkNotNull(id);
    checkNotNull(version);

    try (StorageTx tx = openStorageTx()) {
      Component component = findComponent(tx, id, version);
      if (component == null) {
        return false;
      }
      tx.deleteComponent(component);
      tx.commit();
      return true;
    }
  }

  @VisibleForTesting
  StorageTx openStorageTx() {
    return storage.openTx();
  }

  @VisibleForTesting
  Component createOrUpdatePackageAndContents(final StorageTx storageTx, final Map<String, String> recordMetadata,
                                             final InputStream packageStream)
  {
    final Component component = createOrUpdateComponent(storageTx, recordMetadata);
    createOrUpdateAssetAndContents(storageTx, component, packageStream, recordMetadata);
    return component;
  }

  @VisibleForTesting
  Component createOrUpdatePackage(final StorageTx storageTx, final Map<String, String> recordMetadata)
  {
    final Component component = createOrUpdateComponent(storageTx, recordMetadata);
    Asset asset = findOrCreateAsset(storageTx, component);
    updateAssetMetadata(asset, recordMetadata, component.isNew());
    storageTx.saveAsset(asset);
    return component;
  }

  private void updateAssetMetadata(final Asset asset, final Map<String, String> data, final boolean componentIsNew) {
    if (data != null) {
      final NestedAttributesMap nugetAttr = asset.formatAttributes();

      setDerivedAttributes(data, nugetAttr, !componentIsNew);

      nugetAttr.set(P_AUTHORS, data.get(AUTHORS));
      nugetAttr.set(P_COPYRIGHT, data.get(COPYRIGHT));
      nugetAttr.set(P_DEPENDENCIES, data.get(DEPENDENCIES));
      nugetAttr.set(P_DESCRIPTION, data.get(DESCRIPTION));
      nugetAttr.set(P_GALLERY_DETAILS_URL, data.get(GALLERY_DETAILS_URL));
      nugetAttr.set(P_ICON_URL, data.get(ICON_URL));
      nugetAttr.set(P_ID, data.get(ID));
      nugetAttr.set(P_IS_PRERELEASE, Boolean.parseBoolean(data.get(IS_PRERELEASE)));
      nugetAttr.set(P_LANGUAGE, data.get(LANGUAGE));
      nugetAttr.set(P_LICENSE_URL, data.get(LICENSE_URL));
      nugetAttr.set(P_LOCATION, data.get(LOCATION));
      nugetAttr.set(P_PACKAGE_HASH, data.get(PACKAGE_HASH));
      nugetAttr.set(P_PACKAGE_HASH_ALGORITHM, data.get(PACKAGE_HASH_ALGORITHM));
      nugetAttr.set(P_PACKAGE_SIZE, Integer.parseInt(data.get(PACKAGE_SIZE)));
      nugetAttr.set(P_PROJECT_URL, data.get(PROJECT_URL));
      nugetAttr.set(P_RELEASE_NOTES, data.get(RELEASE_NOTES));
      nugetAttr.set(P_REPORT_ABUSE_URL, data.get(REPORT_ABUSE_URL));
      nugetAttr.set(P_REQUIRE_LICENSE_ACCEPTANCE, Boolean.parseBoolean(data.get(REQUIRE_LICENSE_ACCEPTANCE)));
      nugetAttr.set(P_SUMMARY, data.get(SUMMARY));
      nugetAttr.set(P_TAGS, data.get(TAGS));
      nugetAttr.set(P_TITLE, data.get(TITLE));
      nugetAttr.set(NugetProperties.P_VERSION, data.get(VERSION));
    }
  }

  private Asset findOrCreateAsset(final StorageTx storageTx, final Component component) {
    Asset asset = findAsset(storageTx, component);
    if (asset == null) {
      asset = storageTx.createAsset(storageTx.getBucket(), component);
      asset.name(component.name());
    }
    return asset;
  }

  @Nullable
  @VisibleForTesting
  protected Asset findAsset(final StorageTx storageTx, final Component component) {
    final Iterable<Asset> assets = storageTx.browseAssets(component);
    if (assets.iterator().hasNext()) {
      return assets.iterator().next();
    }
    return null;
  }

  @Nullable
  private Asset findAsset(final StorageTx tx, final String id, final String version) {
    Component component = findComponent(tx, id, version);
    if (component == null) {
      return null;
    }
    return tx.firstAsset(component);
  }

  private String blobName(Component component) {
    return component.name() + "-" + component.requireVersion() + ".nupkg";
  }

  /**
   * Ensure all the components for a given 'id':
   * - have up to date latest version/absolute latest version fields.
   * - have up to date aggregate download count info
   * (updating download counts is different for hosted and proxies; proxies possibly don't need to..)
   */
  protected void maintainAggregateInfo(final StorageTx storageTx, final String id) {
    maintainAggregateInfo(storageTx, findComponentsById(storageTx, id));
  }

  @VisibleForTesting
  void maintainAggregateInfo(final StorageTx storageTx, final Iterable<Component> versions) {
    long totalDownloadCount = 0;

    SortedSet<Component> releases = Sets.newTreeSet(new ComponentVersionComparator());
    SortedSet<Component> allReleases = Sets.newTreeSet(new ComponentVersionComparator());

    for (Component version : versions) {
      final NestedAttributesMap nugetAttributes = storageTx.firstAsset(version).formatAttributes();

      final boolean isPrerelease = nugetAttributes.require(P_IS_PRERELEASE, Boolean.class);
      if (!isPrerelease) {
        releases.add(version);
      }
      allReleases.add(version);

      final int versionDownloadCount = nugetAttributes.require(P_VERSION_DOWNLOAD_COUNT, Integer.class);
      totalDownloadCount += versionDownloadCount;
    }

    Component latestVersion = releases.isEmpty() ? null : releases.last();
    Component absoluteLatestVersion = allReleases.isEmpty() ? null : allReleases.last();

    for (Component component : allReleases) {
      final Asset asset = storageTx.firstAsset(component);
      final NestedAttributesMap nugetAttributes = asset.formatAttributes();

      nugetAttributes.set(P_IS_LATEST_VERSION, component.equals(latestVersion));
      nugetAttributes.set(P_IS_ABSOLUTE_LATEST_VERSION, component.equals(absoluteLatestVersion));

      if (isRepoAuthoritative()) {
        nugetAttributes.set(P_DOWNLOAD_COUNT, totalDownloadCount);
      }
      storageTx.saveAsset(asset);
    }
  }

  private Iterable<Component> findComponentsById(final StorageTx storageTx, final Object id) {
    final String whereClause = "name = :name";
    Map<String, Object> parameters = ImmutableMap.of(P_NAME, id);
    return storageTx.findComponents(whereClause, parameters, getRepositories(), null);
  }

  private void createOrUpdateAssetAndContents(final StorageTx storageTx,
                                              final Component component,
                                              final InputStream in,
                                              final Map<String, String> data)
  {
    try {
      Asset asset = findOrCreateAsset(storageTx, component);
      updateAssetMetadata(asset, data, component.isNew());

      asset.formatAttributes().set(P_LAST_VERIFIED_DATE, new Date());
      storageTx.setBlob(asset, blobName(component), in, singletonList(HashAlgorithm.SHA512), null, "application/zip");

      storageTx.saveAsset(asset);
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private String checkVersion(String stringValue) {
    try {
      SCHEME.parseVersion(checkNotNull(stringValue));
      return stringValue;
    }
    catch (InvalidVersionSpecificationException e) {
      throw new IllegalArgumentException("Bad version syntax: " + stringValue, e);
    }
  }

  private Component createOrUpdateComponent(final StorageTx storageTx,
                                            final Map<String, String> data)
  {
    final String id = checkNotNull(data.get(ID));
    final String version = checkVersion(data.get(VERSION));

    final Component component = findOrCreateComponent(storageTx, id, version);

    NestedAttributesMap nugetAttr = component.formatAttributes();
    nugetAttr.set(P_ID, data.get(ID));
    nugetAttr.set(P_PACKAGE_HASH, data.get(PACKAGE_HASH));
    nugetAttr.set(P_PACKAGE_HASH_ALGORITHM, data.get(PACKAGE_HASH_ALGORITHM));
    nugetAttr.set(P_TITLE, data.get(TITLE));
    nugetAttr.set(NugetProperties.P_VERSION, data.get(VERSION));

    storageTx.saveComponent(component);
    return component;
  }

  /**
   * Is this repository an authoritative source for the packages and metadata it contains?
   */
  @VisibleForTesting
  boolean isRepoAuthoritative() {
    return HostedType.NAME.equals(getRepository().getType().getValue());
  }

  @VisibleForTesting
  void setDerivedAttributes(final Map<String, String> incomingMetadata,
                            final NestedAttributesMap storedMetadata, final boolean republishing)
  {
    if (!republishing) {
      if (isRepoAuthoritative()) {
        // If we're the authoritative repo for this content, set the download counts to zero
        storedMetadata.set(P_DOWNLOAD_COUNT, 0);
        storedMetadata.set(P_VERSION_DOWNLOAD_COUNT, 0);
      }
      else {
        // Otherwise, use what's in the feed (if anything)
        storedMetadata.set(P_DOWNLOAD_COUNT, parseIntOrZero(incomingMetadata.get(DOWNLOAD_COUNT)));
        storedMetadata.set(P_VERSION_DOWNLOAD_COUNT, parseIntOrZero(incomingMetadata.get(VERSION_DOWNLOAD_COUNT)));
      }
    }

    final Date now = new Date(clock.millis());
    if (!republishing && isRepoAuthoritative()) {
      // This is the first insertion into a hosted repository
      storedMetadata.set(P_CREATED, now);
      storedMetadata.set(P_PUBLISHED, now);
    }
    else {
      storedMetadata.set(P_CREATED, ODataUtils.toDate(incomingMetadata.get(CREATED)));
      storedMetadata.set(P_PUBLISHED, ODataUtils.toDate(incomingMetadata.get(PUBLISHED)));
    }

    final String lastUpdated = incomingMetadata.get(LAST_UPDATED);
    storedMetadata.set(P_LAST_UPDATED, lastUpdated == null ? new Date() : ODataUtils.toDate(lastUpdated));

    // Populate keywords for case-insensitive search
    Joiner joiner = Joiner.on(" ").skipNulls();
    String keywords = joiner.join(incomingMetadata.get(ID),
        incomingMetadata.get(TITLE),
        incomingMetadata.get(DESCRIPTION),
        incomingMetadata.get(TAGS),
        incomingMetadata.get(AUTHORS));
    storedMetadata.set(P_KEYWORDS, keywords.toLowerCase());

    // Populate order-by field to support Visual Studio's ordering by name, which is based on CONCAT(title,id)
    // Orient doesn't support anything other than identifiers in ORDER BY
    storedMetadata.set(P_NAME_ORDER,
        (nullToEmpty(incomingMetadata.get(TITLE)) + nullToEmpty(incomingMetadata.get(ID))).toLowerCase());
  }

  private int parseIntOrZero(final String s) {
    try {
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  private Component findOrCreateComponent(final StorageTx storageTx, final String name,
                                          final String version)
  {
    final Component found = findComponent(storageTx, name, version);
    if (found != null) {
      return found;
    }

    return createComponent(storageTx, name, version);
  }

  @VisibleForTesting
  Component findComponent(final StorageTx storageTx, final String name, final Object version) {
    Builder builder = new Builder().where("name = ").param(name).where(" and version = ").param(version);

    return Iterables.getFirst(findComponents(storageTx, builder.build()), null);
  }

  private Iterable<Component> findComponents(final StorageTx storageTx, final ComponentQuery query) {
    return storageTx.findComponents(query.getWhere(), query.getParameters(),
        getRepositories(), query.getQuerySuffix());
  }

  private Component createComponent(final StorageTx storageTx,
                                    final String name,
                                    final String version)
  {
    log.debug("Creating NuGet component {} v. {}", name, version);
    return storageTx.createComponent(storageTx.getBucket(), getRepository().getFormat())
        .name(name)
        .version(version); // Nuget components don't have a group
  }

  private boolean inlineCountRequested(Map<String, String> query) {
    return InlineCount.ALLPAGES.equals(parseInlineCount(query.get("$inlinecount")));
  }

  private int executeCount(final ComponentQuery query, final StorageTx storageTx,
                           final Iterable<Repository> repositories)
  {
    return (int) storageTx.countAssets(query.getWhere(), query.getParameters(), repositories,
        query.getQuerySuffix());
  }

  /**
   * Returns the constituent repositories - just this one, or the leaf member repositories if this is a group.
   */
  protected List<Repository> getRepositories() {
    try {
      final GroupFacet facet = getRepository().facet(GroupFacet.class);
      return facet.leafMembers();
    }
    catch (MissingFacetException e) {
      return singletonList(getRepository());
    }
  }

  protected Iterable<Repository> getHostedRepositories() {
    return Iterables.filter(getRepositories(), not(new HasFacet(ProxyFacet.class)));
  }

  protected Iterable<Repository> getProxyRepositories() {
    return Iterables.filter(getRepositories(), new HasFacet(ProxyFacet.class));
  }

  @VisibleForTesting
  static class ComponentVersionComparator
      implements Comparator<Component>
  {
    @Override
    public int compare(final Component o1, final Component o2) {
      try {
        Version v1 = SCHEME.parseVersion(o1.requireVersion());
        Version v2 = SCHEME.parseVersion(o2.requireVersion());
        return v1.compareTo(v2);
      }
      catch (InvalidVersionSpecificationException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  private static class HasFacet
      implements Predicate<Repository>
  {
    private final Class<ProxyFacet> facetClass;

    public HasFacet(final Class<ProxyFacet> facetClass) {
      this.facetClass = facetClass;
    }

    @Override
    public boolean apply(final Repository input) {
      try {

        input.facet(facetClass);
        return true;
      }
      catch (MissingFacetException e) {
        return false;
      }
    }
  }
}
