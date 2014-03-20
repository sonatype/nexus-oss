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

package org.sonatype.nexus.timeline.feeds;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.EntryListCallback;
import org.sonatype.nexus.timeline.NexusTimeline;
import org.sonatype.nexus.timeline.feeds.NexusArtifactEvent.NexusItemInfo;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class DefaultFeedRecorder
    extends ComponentSupport
    implements FeedRecorder
{

  public static final int DEFAULT_PAGE_SIZE = 40;

  public static final String REPOSITORY = "r";

  public static final String REPOSITORY_PATH = "path";

  public static final String REMOTE_URL = "rurl";

  public static final String CTX_PREFIX = "ctx.";

  public static final String ATR_PREFIX = "atr.";

  public static final String ACTION = "action";

  public static final String MESSAGE = "message";

  public static final String DATE = "date";

  /**
   * The time format used in events.
   */
  private static final String EVENT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

  /**
   * The timeline for persistent events and feeds.
   */
  private final NexusTimeline nexusTimeline;

  private final NexusItemAuthorizer nexusItemAuthorizer;

  private final RepositoryRegistry repositoryRegistry;

  @Inject
  public DefaultFeedRecorder(final NexusTimeline nexusTimeline,
                             final NexusItemAuthorizer nexusItemAuthorizer,
                             final RepositoryRegistry repositoryRegistry)
  {
    this.nexusTimeline = checkNotNull(nexusTimeline);
    this.nexusItemAuthorizer = checkNotNull(nexusItemAuthorizer);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
  }

  // filtering

  private boolean filterEvent(NexusArtifactEvent event) {
    try {
      final Repository repo = repositoryRegistry.getRepository(event.getNexusItemInfo().getRepositoryId());
      final ResourceStoreRequest req = new ResourceStoreRequest(event.getNexusItemInfo().getPath());
      if (!this.nexusItemAuthorizer.authorizePath(repo, req, Action.read)) {
        return false;
      }
    }
    catch (NoSuchRepositoryException e) {
      // Can't get repository for artifact, therefore we can't authorize access, therefore you don't see it
      log.debug(
          "Feed entry contained invalid repository id " + event.getNexusItemInfo().getRepositoryId(),
          e);
      return false;
    }
    return true;
  }

  // feed recorder

  protected DateFormat getDateFormat() {
    return new SimpleDateFormat(EVENT_DATE_FORMAT);
  }

  protected Date getEventDate(final Map<String, String> map) {
    Date eventDate;

    try {
      eventDate = getDateFormat().parse(map.get(DATE));
    }
    catch (ParseException e) {
      log.warn("Could not format event date!", e);

      eventDate = new Date();
    }

    return eventDate;
  }

  protected List<NexusArtifactEvent> getAisFromMaps(List<Entry> data) {
    List<NexusArtifactEvent> result = new ArrayList<NexusArtifactEvent>();

    for (Entry record : data) {
      Map<String, String> map = record.getData();

      final NexusItemInfo ai = new NexusItemInfo(map.get(REPOSITORY_PATH), map.get(REPOSITORY),map.get(REMOTE_URL));

      HashMap<String, String> ctx = new HashMap<String, String>();
      HashMap<String, String> atr = new HashMap<String, String>();

      for (String key : map.keySet()) {
        if (key.startsWith(CTX_PREFIX)) {
          ctx.put(key.substring(4), map.get(key));
        }
        else if (key.startsWith(ATR_PREFIX)) {
          atr.put(key.substring(4), map.get(key));
        }
      }

      NexusArtifactEvent nae =
          new NexusArtifactEvent(getEventDate(map), map.get(ACTION), map.get(MESSAGE), ai);

      // NEXUS-4038: backward compatibility
      // Before this fix, nae had NO attributes separately stored, but only ctx map existed with ctx + atr content
      // overlayed
      // After fix we have two separate maps. To handle well "old" timeline records, when we detect there is no
      // atr map (atr map is empty which will never be after fix), we "emulate" and lift all the ctx map into atr
      // map instead.

      if (atr.isEmpty()) {
        nae.addItemAttributes(ctx);
      }
      else {
        nae.addEventContext(ctx);
        nae.addItemAttributes(atr);
      }

      if (filterEvent(nae)) {
        result.add(nae);
      }
    }
    return result;
  }

  protected List<SystemEvent> getSesFromMaps(List<Entry> data) {
    List<SystemEvent> result = new ArrayList<SystemEvent>();

    for (Entry record : data) {
      Map<String, String> map = record.getData();

      HashMap<String, Object> ctx = new HashMap<String, Object>();

      for (String key : map.keySet()) {
        if (key.startsWith(CTX_PREFIX)) {
          ctx.put(key.substring(4), map.get(key));
        }
      }

      SystemEvent se = new SystemEvent(getEventDate(map), map.get(ACTION), map.get(MESSAGE));

      se.addEventContext(ctx);

      result.add(se);
    }

    return result;
  }

  protected List<AuthcAuthzEvent> getAaesFromMaps(List<Entry> data) {
    List<AuthcAuthzEvent> result = new ArrayList<AuthcAuthzEvent>();

    for (Entry record : data) {
      Map<String, String> map = record.getData();

      HashMap<String, Object> ctx = new HashMap<String, Object>();

      for (String key : map.keySet()) {
        if (key.startsWith(CTX_PREFIX)) {
          ctx.put(key.substring(4), map.get(key));
        }
      }

      AuthcAuthzEvent evt = new AuthcAuthzEvent(getEventDate(map), map.get(ACTION), map.get(MESSAGE));

      evt.addEventContext(ctx);

      result.add(evt);

    }

    return result;
  }

  // ==

  public List<Entry> getEvents(Set<String> types, Set<String> subtypes, Integer from, Integer count,
                               Predicate<Entry> filter)
  {
    int cnt = count != null ? count : DEFAULT_PAGE_SIZE;

    final EntryListCallback cb = new EntryListCallback();
    if (from != null) {
      nexusTimeline.retrieve(from, cnt, types, subtypes, filter, cb);
    }
    else {
      nexusTimeline.retrieve(0, cnt, types, subtypes, filter, cb);
    }
    return cb.getEntries();
  }

  public List<NexusArtifactEvent> getNexusArtifectEvents(Set<String> subtypes, Integer from, Integer count,
                                                         Predicate<Entry> filter)
  {
    List<Entry> result = getEvents(ImmutableSet.of(NexusArtifactEvent.TYPE), subtypes, from, count, filter);

    return getAisFromMaps(result);
  }

  public List<SystemEvent> getSystemEvents(Set<String> subtypes, Integer from, Integer count, Predicate<Entry> filter) {
    List<Entry> result = getEvents(ImmutableSet.of(SystemEvent.TYPE), subtypes, from, count, filter);

    return getSesFromMaps(result);
  }

  public List<AuthcAuthzEvent> getAuthcAuthzEvents(Set<String> subtypes, Integer from, Integer count,
                                                   Predicate<Entry> filter)
  {
    List<Entry> result = getEvents(ImmutableSet.of(AuthcAuthzEvent.TYPE), subtypes, from, count, filter);

    return getAaesFromMaps(result);
  }

  // ==

  public void addSystemEvent(SystemEvent event) {
    addToTimeline(event);
  }

  private void putContext(final Map<String, String> map, final String prefix, final Map<String, ?> context) {
    for (String key : context.keySet()) {
      Object value = context.get(key);

      if (value == null) {
        if (log.isDebugEnabled()) {
          log.debug("The attribute with key '" + key + "' in event context is NULL!");
        }

        value = "";
      }

      map.put(prefix + key, value.toString());
    }
  }

  public void addAuthcAuthzEvent(AuthcAuthzEvent evt) {
    Map<String, String> map = new HashMap<String, String>();

    putContext(map, CTX_PREFIX, evt.getEventContext());

    map.put(ACTION, evt.getAction());

    map.put(MESSAGE, evt.getMessage());

    map.put(DATE, getDateFormat().format(evt.getEventDate()));

    addToTimeline(map, AuthcAuthzEvent.TYPE, evt.getAction());
  }

  public void addNexusArtifactEvent(NexusArtifactEvent nae) {
    Map<String, String> map = new HashMap<String, String>();

    map.put(REPOSITORY, nae.getNexusItemInfo().getRepositoryId());

    map.put(REPOSITORY_PATH, nae.getNexusItemInfo().getPath());

    if (nae.getNexusItemInfo().getRemoteUrl() != null) {
      map.put(REMOTE_URL, nae.getNexusItemInfo().getRemoteUrl());
    }

    putContext(map, CTX_PREFIX, nae.getEventContext());
    putContext(map, ATR_PREFIX, nae.getItemAttributes());

    if (nae.getMessage() != null) {
      map.put(MESSAGE, nae.getMessage());
    }

    map.put(DATE, getDateFormat().format(nae.getEventDate()));

    map.put(ACTION, nae.getAction());

    addToTimeline(map, NexusArtifactEvent.TYPE, nae.getAction());
  }

  public TaskEvent taskStarted(String action, String message) {
    TaskEvent prc = new TaskEvent(new Date(), action, message, new Date());

    addToTimeline(prc);

    log.debug(prc.getMessage());

    return prc;
  }

  public void taskFinished(TaskEvent prc, String finishMessage) {
    prc.finished(finishMessage);

    addToTimeline(prc);

    log.debug(prc.getMessage());
  }

  public void taskCanceled(TaskEvent prc, String cancelMessage) {
    prc.canceled(cancelMessage);

    addToTimeline(prc);

    log.debug(prc.getMessage());
  }

  public void taskBroken(TaskEvent prc, Throwable e) {
    prc.broken(e);

    addToTimeline(prc);

    log.debug(prc.getMessage(), e);
  }

  protected void addToTimeline(SystemEvent se) {
    Map<String, String> map = new HashMap<String, String>();

    putContext(map, CTX_PREFIX, se.getEventContext());

    map.put(DATE, getDateFormat().format(se.getEventDate()));

    map.put(ACTION, se.getAction());

    map.put(MESSAGE, se.getMessage());

    addToTimeline(map, SystemEvent.TYPE, se.getAction());
  }

  protected void addToTimeline(Map<String, String> map, String t1, String t2) {
    nexusTimeline.add(System.currentTimeMillis(), t1, t2, map);
  }
}
