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
package org.sonatype.nexus.repository.proxy;

import java.io.IOException;
import java.net.URI;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.MissingFacetException;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.negativecache.NegativeCacheFacet;
import org.sonatype.nexus.repository.proxy.remote.AutoBlockedUnavailableConnection;
import org.sonatype.nexus.repository.proxy.remote.AutoBlockingAvailableConnection;
import org.sonatype.nexus.repository.proxy.remote.AvailableConnection;
import org.sonatype.nexus.repository.proxy.remote.GetResult;
import org.sonatype.nexus.repository.proxy.remote.ManuallyBlockedConnection;
import org.sonatype.nexus.repository.proxy.remote.RemoteConnection;
import org.sonatype.nexus.repository.proxy.remote.RemoteConnectionStatus;
import org.sonatype.nexus.repository.proxy.remote.UnavailableConnection;
import org.sonatype.nexus.repository.proxy.remote.UnknownConnection;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public abstract class ProxyFacetSupport
    extends FacetSupport
    implements ProxyFacet
{
  public static final String CONFIG_KEY = "proxy";

  private URI remoteUrl;

  private Boolean enabled;

  private Boolean autoBlock;

  private int artifactMaxAgeMinutes;

  private RemoteConnection remoteConnection;

  private boolean remoteUrlChanged;

  @Override
  protected void doConfigure() throws Exception {
    NestedAttributesMap attributes = getRepository().getConfiguration().attributes(CONFIG_KEY);
    String url = attributes.require("remoteUrl", String.class);
    if (!url.endsWith("/")) {
      url = url + "/";
    }

    final URI newRemoteURI = new URI(url);
    if (remoteUrl != null && !remoteUrl.equals(newRemoteURI)) {
      remoteUrlChanged = true;
    }

    this.remoteUrl = newRemoteURI;
    log.debug("Remote URL: {}", remoteUrl);

    this.enabled = attributes.get("enabled", Boolean.class, true);
    this.autoBlock = attributes.get("autoBlock", Boolean.class, true);

    artifactMaxAgeMinutes = attributes.require("artifactMaxAge", Integer.class);
    log.debug("Artifact max age: {}", artifactMaxAgeMinutes);
  }


  @Override
  protected void doStart() throws Exception {
    remoteConnection = createRemoteState();
    log.debug("Remote connection status set to '{}'", remoteConnection.getStatus().getDescription());

    if (remoteUrlChanged) {
      remoteUrlChanged = false;
      try {
        getRepository().facet(NegativeCacheFacet.class).invalidate();
      }
      catch (MissingFacetException e) {
        // NCF is optional
      }
    }
  }

  @Override
  protected void doDestroy() throws Exception {
    remoteUrl = null;
  }

  @Override
  public Payload get(final Context context) throws IOException {
    checkNotNull(context);

    Payload content = getCachedPayload(context);

    if (content == null || isStale(context)) {
      try {
        final GetResult result = remoteConnection.get(remoteUrl.resolve(getUrl(context)));
        if (result.getPayload() != null) {

          // TODO: Introduce content validation.. perhaps content's type not matching path's implied type.

          store(context, result.getPayload());

          content = result.getPayload();
        }
        else if (result.getStatus() == HttpStatus.NOT_MODIFIED) {
          indicateUpToDate(context);
        }
      }
      catch (IOException e) {
        log.warn("Failed to get: {}", getUrl(context), e);
        throw e;
      }
    }
    return content;
  }

  @Override
  public RemoteConnectionStatus getRemoteConnectionStatus() {
    return remoteConnection.getStatus();
  }

  public void setRemoteConnection(final RemoteConnection remoteConnection) {
    checkNotNull(remoteConnection);
    log.debug(
        "Remote connection status changed from '{}' to '{}'",
        this.remoteConnection.getStatus().getDescription(), remoteConnection.getStatus().getDescription()
    );
    this.remoteConnection = remoteConnection;
  }

  public Repository getRepository() {
    return super.getRepository();
  }

  /**
   * If we have the content cached locally already, return that - otherwise {@code null}.
   */
  protected abstract Payload getCachedPayload(final Context context) throws IOException;

  /**
   * Store a new Payload, freshly fetched from the remote URL. The Context indicates which component
   * was being requested.
   */
  protected abstract void store(final Context context, final Payload payload)
      throws IOException, InvalidContentException;

  /**
   * For whatever component/asset is implied by the Context, return the date it was last deemed up to date, or {@code
   * null} if it isn't present.
   */
  protected abstract DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException;

  /**
   * For whatever component/asset
   */
  protected abstract void indicateUpToDate(final Context context) throws IOException;

  /**
   * Provide the relative URL to the
   */
  protected abstract String getUrl(final @Nonnull Context context);

  private RemoteConnection createRemoteState() {
    if (!enabled) {
      return new ManuallyBlockedConnection();
    }
    return new UnknownConnection(this)
    {
      @Override
      public GetResult get(final URI uri) throws IOException {
        try {
          GetResult result = super.get(uri);
          setRemoteConnection(
              autoBlock
                  ? new AutoBlockingAvailableConnection(ProxyFacetSupport.this)
                  : new AvailableConnection(ProxyFacetSupport.this)
          );
          return result;
        }
        catch (IOException e) {
          if (isRemoteUnavailable(e)) {
            setRemoteConnection(
                autoBlock
                    ? new AutoBlockedUnavailableConnection(ProxyFacetSupport.this, e)
                    : new UnavailableConnection(ProxyFacetSupport.this, e)
            );
          }
          else {
            setRemoteConnection(
                autoBlock
                    ? new AutoBlockingAvailableConnection(ProxyFacetSupport.this)
                    : new AvailableConnection(ProxyFacetSupport.this)
            );
          }
          throw e;
        }
      }
    };
  }

  private boolean isStale(final Context context) throws IOException {
    if (artifactMaxAgeMinutes < 0) {
      log.trace("Artifact max age checking disabled.");
      return false;
    }

    final DateTime lastUpdated = getCachedPayloadLastUpdatedDate(context);

    if (lastUpdated == null) {
      log.debug("Artifact last modified date unknown.");
      return true;
    }

    final DateTime earliestFreshDate = new DateTime().minusMinutes(artifactMaxAgeMinutes);
    return lastUpdated.isBefore(earliestFreshDate);
  }

}
