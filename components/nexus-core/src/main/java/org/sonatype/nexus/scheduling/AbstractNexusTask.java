/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.scheduling;

import java.util.Map;

import javax.inject.Inject;

import org.sonatype.nexus.scheduling.events.NexusTaskEventStarted;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Base class for all Nexus tasks.
 */
public abstract class AbstractNexusTask<T>
    extends ComponentSupport
    implements NexusTask<T>
{
  /**
   * Prefix for private properties keys.
   */
  static final String PRIVATE_PROP_PREFIX = ".";

  /**
   * Key of id property (private).
   */
  static final String ID_KEY = PRIVATE_PROP_PREFIX + "id";

  /**
   * Key of name property (private).
   */
  static final String NAME_KEY = PRIVATE_PROP_PREFIX + "name";

  /**
   * Key of enabled property (private).
   */
  static final String ENABLED_KEY = PRIVATE_PROP_PREFIX + "enabled";

  /**
   * Key of alert email property (private).
   */
  static final String ALERT_EMAIL_KEY = PRIVATE_PROP_PREFIX + "alertEmail";

  public static final long A_DAY = 24L * 60L * 60L * 1000L;

  private EventBus eventBus;

  private Map<String, String> parameters;

  protected AbstractNexusTask() {
    this(null);
  }

  protected AbstractNexusTask(final String name) {
    this.parameters = Maps.newHashMap();
    if (Strings.isNullOrEmpty(name)) {
      setName(getClass().getSimpleName());
    }
    else {
      setName(name);
    }
  }

  protected AbstractNexusTask(final EventBus eventBus, final String name) {
    this(name);
    this.eventBus = eventBus;
  }

  protected EventBus getEventBus() {
    checkState(eventBus != null);
    return eventBus;
  }

  @Inject
  public void setEventBus(final EventBus eventBus) {
    this.eventBus = eventBus;
  }

  protected final void notifyEventListeners(final Object event) {
    eventBus.post(event);
  }

  @Deprecated
  protected final Logger getLogger() {
    return log;
  }

  @Override
  public void addParameter(String key, String value) {
    checkNotNull(key);
    if (!Strings.isNullOrEmpty(value)) {
      parameters.put(key, value);
    }
    else {
      parameters.remove(key);
    }
  }

  @Override
  public String getParameter(String key) {
    checkNotNull(key);
    return parameters.get(key);
  }

  @Override
  public Map<String, String> getParameters() {
    return parameters;
  }

  @Override
  public Map<String, String> getPublicParameters() {
    final Map<String, String> result = Maps.newHashMap();
    for (Map.Entry<String, String> entry : getParameters().entrySet()) {
      if (!entry.getKey().startsWith(PRIVATE_PROP_PREFIX)) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }

  @Override
  public boolean isExposed() {
    // override to hide it
    return true;
  }

  @Override
  public String getId() {
    return getParameter(ID_KEY);
  }

  @Override
  public void setId(String id) {
    checkNotNull(id);
    addParameter(ID_KEY, id);
  }

  @Override
  public String getName() {
    return getParameter(NAME_KEY);
  }

  @Override
  public void setName(String name) {
    checkNotNull(name);
    addParameter(NAME_KEY, name);
  }

  @Override
  public boolean isEnabled() {
    final String val = getParameter(ENABLED_KEY);
    if (Strings.isNullOrEmpty(val)) {
      return true;
    }
    else {
      return Boolean.valueOf(val);
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!enabled) {
      addParameter(ENABLED_KEY, Boolean.FALSE.toString());
    }
    else {
      addParameter(ENABLED_KEY, null);
    }
  }

  @Override
  public boolean shouldSendAlertEmail() {
    final String alertEmail = getAlertEmail();
    return alertEmail != null && alertEmail.trim().length() > 0;
  }

  @Override
  public String getAlertEmail() {
    return getParameter(ALERT_EMAIL_KEY);
  }

  @Override
  public void setAlertEmail(String alertEmail) {
    addParameter(ALERT_EMAIL_KEY, alertEmail);
  }

  protected void checkInterruption()
      throws TaskInterruptedException
  {
    TaskUtil.checkInterruption();
  }
  
  @Override
  public final T call()
      throws Exception
  {
    getLogger().info(getLoggedMessage("started"));
    final long started = System.currentTimeMillis();

    // fire event
    final NexusTaskEventStarted<T> startedEvent = new NexusTaskEventStarted<T>(this);
    getEventBus().post(startedEvent);

    T result = null;

    try {
      beforeRun();

      result = doRun();

      if (TaskUtil.getCurrentProgressListener().isCanceled()) {
        getLogger().info(getLoggedMessage("canceled", started));

        getEventBus().post(new NexusTaskEventStoppedCanceled<T>(this, startedEvent));
      }
      else {
        getLogger().info(getLoggedMessage("finished", started));

        getEventBus().post(new NexusTaskEventStoppedDone<T>(this, startedEvent));
      }

      afterRun();

      return result;
    }
    catch (final Throwable e) {
      // this if below is to catch TaskInterruptedException in tasks that does not handle it
      // and let it propagate.
      if (e instanceof TaskInterruptedException) {
        getLogger().info(getLoggedMessage("canceled", started));

        // just return, nothing happened just task cancelled
        getEventBus().post(new NexusTaskEventStoppedCanceled<T>(this, startedEvent));

        return null;
      }
      else {
        getLogger().warn(getLoggedMessage("failed", started), e);

        // notify that there was a failure
        getEventBus().post(new NexusTaskEventStoppedFailed<T>(this, startedEvent, e));

        Throwables.propagateIfInstanceOf(e, Exception.class);
        throw Throwables.propagate(e);
      }
    }
  }

  protected String getLoggedMessage(final String action) {
    return String.format("Scheduled task (%s) %s :: %s", getName(), action, getMessage());
  }

  protected String getLoggedMessage(final String action, final long started) {
    final String startedStr = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(started);
    final String durationStr = DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - started);

    return String.format("%s (started %s, runtime %s)", getLoggedMessage(action), startedStr, durationStr);
  }

  protected void beforeRun()
      throws Exception
  {
    // override if needed
  }

  protected abstract T doRun()
      throws Exception;

  protected void afterRun()
      throws Exception
  {
    // override if needed
  }

  protected abstract String getAction();

  protected abstract String getMessage();

}
