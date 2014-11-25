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

import java.util.Date;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The task configuration backed by plain map. The configuration is persisted by actual underlying scheduler, so it
 * MUST contain strings only (and string encoded primitives). Still, you can circumvent this primitive configuration by
 * storing some custom string as key here, and using that key fetch some custom configuration for your task via some
 * injected component.
 *
 * @since 3.0
 */
public final class TaskConfiguration
{
  /**
   * Checks if a property is a private property. Private properties are those properties that start with
   * {@link #PRIVATE_PROP_PREFIX}.
   *
   * @param key property key
   * @return true if the key defines a private property
   */
  public static boolean isPrivateProperty(final String key) {
    return key != null && key.startsWith(PRIVATE_PROP_PREFIX);
  }

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
   * Key of type property (private).
   */
  static final String TYPE_KEY = PRIVATE_PROP_PREFIX + "type";

  /**
   * Key of enabled property (private).
   */
  static final String ENABLED_KEY = PRIVATE_PROP_PREFIX + "enabled";

  /**
   * Key of visible property (private).
   */
  static final String VISIBLE_KEY = PRIVATE_PROP_PREFIX + "visible";

  /**
   * Key of alert email property (private).
   */
  static final String ALERT_EMAIL_KEY = PRIVATE_PROP_PREFIX + "alertEmail";

  /**
   * Key of created property (private).
   */
  static final String CREATED_KEY = PRIVATE_PROP_PREFIX + "created";

  /**
   * Key of updated property (private).
   */
  static final String UPDATED_KEY = PRIVATE_PROP_PREFIX + "updated";

  /**
   * Key of message property (private).
   */
  static final String MESSAGE_KEY = PRIVATE_PROP_PREFIX + "message";

  /**
   * Key of description.
   */
  public static final String DESCRIPTION_KEY = "description";

  /**
   * Key of repository.
   */
  public static final String REPOSITORY_ID_KEY = "repositoryId";

  /**
   * Key of path.
   */
  public static final String PATH_KEY = "path";

  private final Map<String, String> configuration = Maps.newHashMap();

  /**
   * Performs a "self" validation of the configuration for completeness and correctness.
   * TODO: hook this into validator somehow? Currently, this method is only a "shortcut" to not have it
   * spread across task factory and scheduler.
   */
  public final void validate() throws IllegalStateException {
    // Minimum requirements
    checkState(!Strings.isNullOrEmpty(getId()), "Incomplete task configuration: id");
    checkState(!Strings.isNullOrEmpty(getType()), "Incomplete task configuration: type");
  }

  /**
   * Exposes the "live" backing map.
   */
  public Map<String, String> getMap() {
    return configuration;
  }

  /**
   * Returns a unique ID of the task instance.
   */
  public String getId() {
    return getString(ID_KEY);
  }

  /**
   * Sets the ID.
   */
  public void setId(final String id) {
    checkNotNull(id);
    getMap().put(ID_KEY, id);
  }

  /**
   * Returns a name of the task instance.
   */
  public String getName() {
    return getString(NAME_KEY);
  }

  /**
   * Sets the task name.
   */
  public void setName(final String name) {
    checkNotNull(name);
    getMap().put(NAME_KEY, name);
  }

  /**
   * Returns a type of the task instance.
   */
  public String getType() {
    return getString(TYPE_KEY);
  }

  /**
   * Sets the task type.
   */
  public void setType(final String type) {
    checkNotNull(type);
    getMap().put(TYPE_KEY, type);
  }

  /**
   * Is task enabled?
   */
  public boolean isEnabled() {
    return getBoolean(ENABLED_KEY, true);
  }

  /**
   * Sets is task enabled.
   */
  public void setEnabled(final boolean enabled) {
    getMap().put(ENABLED_KEY, Boolean.toString(enabled));
  }

  /**
   * Is task while running visible?
   */
  public boolean isVisible() {
    return getBoolean(VISIBLE_KEY, true);
  }

  /**
   * Sets is running task visible.
   */
  public void setVisible(final boolean visible) {
    getMap().put(VISIBLE_KEY, Boolean.toString(visible));
  }

  /**
   * Returns the email where alert should be sent in case of failure.
   */
  public String getAlertEmail() {
    return getString(ALERT_EMAIL_KEY);
  }

  /**
   * Sets or clears the alert email.
   */
  public void setAlertEmail(final String email) {
    if (Strings.isNullOrEmpty(email)) {
      getMap().remove(ALERT_EMAIL_KEY);
    }
    else {
      getMap().put(ALERT_EMAIL_KEY, email);
    }
  }

  /**
   * Gets created.
   */
  public Date getCreated() {
    return getDate(CREATED_KEY, null);
  }

  /**
   * Sets created, {@code date} cannot be {@code null}.
   */
  public void setCreated(final Date date) {
    checkNotNull(date);
    setDate(CREATED_KEY, date);
  }

  /**
   * Gets updated.
   */
  public Date getUpdated() {
    return getDate(UPDATED_KEY, null);
  }

  /**
   * Sets updated, {@code date} cannot be {@code null}.
   */
  public void setUpdated(final Date date) {
    checkNotNull(date);
    setDate(UPDATED_KEY, date);
  }

  /**
   * Returns the message of current or last run of task.
   */
  public String getMessage() {
    return getString(MESSAGE_KEY);
  }

  /**
   * Sets or clears task message of current or last run.
   */
  public void setMessage(final String message) {
    if (Strings.isNullOrEmpty(message)) {
      getMap().remove(MESSAGE_KEY);
    }
    else {
      getMap().put(MESSAGE_KEY, message);
    }
  }

  /**
   * Returns the description of the task.
   */
  public String getDescription() {
    final String description = getString(DESCRIPTION_KEY);
    if (Strings.isNullOrEmpty(description)) {
      return getName();
    }
    return description;
  }

  /**
   * Sets or resets the description of the task.
   */
  public void setDescription(final String description) {
    if (Strings.isNullOrEmpty(description)) {
      getMap().remove(DESCRIPTION_KEY);
    }
    else {
      getMap().put(DESCRIPTION_KEY, description);
    }
  }

  /**
   * Returns the repository ID that task should target or {@code null} if not set. The latter usually means
   * "all repositories" but the meaning might be different per task.
   */
  public String getRepositoryId() {
    // TODO: this might change?
    final String repoId = getString(REPOSITORY_ID_KEY);
    if (repoId == null || "*".equals(repoId) || "all_repo".equals(repoId)) {
      return null;
    }
    return repoId;
  }

  /**
   * Sets or clears the repository ID.
   */
  public void setRepositoryId(final String repoId) {
    // TODO: this might change?
    if (Strings.isNullOrEmpty(repoId) || "*".equals(repoId) || "all_repo".equals(repoId)) {
      getMap().remove(REPOSITORY_ID_KEY);
    }
    else {
      getMap().put(REPOSITORY_ID_KEY, repoId);
    }
  }

  /**
   * Returns the path under which task should operate, if applicable. Never returns {@code null}.
   */
  public String getPath() {
    return getString(PATH_KEY, "/");
  }

  /**
   * Sets or clears the path.
   */
  public void setPath(final String path) {
    if (Strings.isNullOrEmpty(path)) {
      getMap().remove(PATH_KEY);
    }
    else {
      getMap().put(PATH_KEY, path);
    }
  }

  // ==

  /**
   * Returns date parameter by key.
   */
  public Date getDate(final String key, final Date defaultValue) {
    if (getMap().containsKey(key)) {
      // TODO: will NPE if value is null
      return new DateTime(getString(key)).toDate();
    }
    else {
      return defaultValue;
    }
  }

  /**
   * Sets or clears a date parameter.
   */
  public void setDate(final String key, final Date date) {
    checkNotNull(key);
    if (date == null) {
      getMap().remove(key);
    }
    else {
      getMap().put(key, new DateTime(date).toString());
    }
  }

  /**
   * Returns boolean parameter by key.
   */
  public boolean getBoolean(final String key, final boolean defaultValue) {
    return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
  }

  /**
   * Sets a boolean value.
   */
  public void setBoolean(final String key, final boolean value) {
    checkNotNull(key);
    getMap().put(key, String.valueOf(value));
  }

  /**
   * Returns int parameter by key.
   */
  public int getInteger(final String key, final int defaultValue) {
    return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
  }

  /**
   * Sets' a integer value.
   */
  public void setInteger(final String key, final int value) {
    checkNotNull(key);
    getMap().put(key, String.valueOf(value));
  }

  /**
   * Returns long parameter by key.
   */
  public long getLong(final String key, final long defaultValue) {
    return Long.parseLong(getString(key, String.valueOf(defaultValue)));
  }

  /**
   * Sets' a long value.
   */
  public void setLong(final String key, final long value) {
    checkNotNull(key);
    getMap().put(key, String.valueOf(value));
  }

  /**
   * Returns string parameter by key or {@code null} if no such key mapped.
   */
  public String getString(final String key) {
    return getString(key, null);
  }

  /**
   * Returns string parameter by key or {@code defaultValue} if no such key mapped..
   */
  public String getString(final String key, final String defaultValue) {
    checkNotNull(key);
    if (getMap().containsKey(key)) {
      return getMap().get(key);
    }
    else {
      return defaultValue;
    }
  }

  /**
   * Sets or clears a string value.
   */
  public void setString(final String key, final String value) {
    checkNotNull(key);
    if (value == null) {
      getMap().remove(key);
    }
    else {
      getMap().put(key, value);
    }
  }

  // ==

  public String toString() {
    return getMap().toString();
  }
}
