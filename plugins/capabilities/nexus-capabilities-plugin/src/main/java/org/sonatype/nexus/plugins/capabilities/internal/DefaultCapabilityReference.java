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

package org.sonatype.nexus.plugins.capabilities.internal;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableMap;

/**
 * Default {@link CapabilityReference} implementation.
 *
 * @since 2.0
 */
public class DefaultCapabilityReference
    extends AbstractLoggingComponent
    implements CapabilityReference, CapabilityContext
{

  private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

  private final CapabilityIdentity id;

  private final CapabilityType type;

  private final CapabilityDescriptor descriptor;

  private final Capability capability;

  private String notes;

  private final CapabilityRegistry capabilityRegistry;

  private final EventBus eventBus;

  private final ActivationConditionHandler activationHandler;

  private final ValidityConditionHandler validityHandler;

  private final ReentrantReadWriteLock stateLock;

  private Map<String, String> capabilityProperties;

  private State state;

  private Exception lastException;

  DefaultCapabilityReference(final CapabilityRegistry capabilityRegistry,
                             final EventBus eventBus,
                             final ActivationConditionHandlerFactory activationListenerFactory,
                             final ValidityConditionHandlerFactory validityConditionHandlerFactory,
                             final CapabilityIdentity id,
                             final CapabilityType type,
                             final CapabilityDescriptor descriptor,
                             final Capability capability)
  {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
    this.eventBus = checkNotNull(eventBus);

    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.descriptor = checkNotNull(descriptor);
    this.capability = checkNotNull(capability);
    capabilityProperties = EMPTY_MAP;

    state = new NewState();
    stateLock = new ReentrantReadWriteLock();
    activationHandler = checkNotNull(activationListenerFactory).create(this);
    validityHandler = checkNotNull(validityConditionHandlerFactory).create(this);

    capability.init(this);
  }

  public Capability capability() {
    return capability;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Capability> T capabilityAs(final Class<T> type) {
    return (T) capability;
  }

  @Override
  public CapabilityIdentity id() {
    return id;
  }

  @Override
  public CapabilityType type() {
    return type;
  }

  @Override
  public CapabilityDescriptor descriptor() {
    return descriptor;
  }

  @Override
  public String notes() {
    return notes;
  }

  public void setNotes(final String notes) {
    this.notes = notes;
  }

  @Override
  public boolean isEnabled() {
    try {
      stateLock.readLock().lock();
      return state.isEnabled();
    }
    finally {
      stateLock.readLock().unlock();
    }
  }

  /**
   * Enables the referenced capability.
   */
  public void enable() {
    try {
      stateLock.writeLock().lock();
      state.enable();
    }
    finally {
      stateLock.writeLock().unlock();
    }
  }

  /**
   * Disables the referenced capability.
   */
  public void disable() {
    try {
      stateLock.writeLock().lock();
      state.disable();
    }
    finally {
      stateLock.writeLock().unlock();
    }
  }

  @Override
  public boolean isActive() {
    try {
      stateLock.readLock().lock();
      return state.isActive();
    }
    finally {
      stateLock.readLock().unlock();
    }
  }

  @Override
  public boolean hasFailure() {
    return failure() != null;
  }

  @Override
  public Exception failure() {
    try {
      stateLock.readLock().lock();
      return lastException;
    }
    finally {
      stateLock.readLock().unlock();
    }
  }

  /**
   * Activates the referenced capability.
   */
  public void activate() {
    try {
      stateLock.writeLock().lock();
      state.activate();
    }
    finally {
      stateLock.writeLock().unlock();
    }
  }

  /**
   * Passivate the referenced capability.
   */
  public void passivate() {
    try {
      stateLock.writeLock().lock();
      state.passivate();
    }
    finally {
      stateLock.writeLock().unlock();
    }
  }

  @Override
  public CapabilityContext context() {
    return this;
  }

  /**
   * Callback when a new capability is created.
   *
   * @param properties capability configuration
   */
  public void create(final Map<String, String> properties) {
    try {
      stateLock.writeLock().lock();
      state.create(properties);
    }
    finally {
      stateLock.writeLock().unlock();
    }
  }

  /**
   * Callback when a capability configuration is loaded from persisted store (configuration file).
   *
   * @param properties capability configuration
   */
  public void load(final Map<String, String> properties) {
    try {
      stateLock.writeLock().lock();
      state.load(properties);
    }
    finally {
      stateLock.writeLock().unlock();
    }
  }

  /**
   * Callback when a capability configuration is updated.
   *
   * @param properties         capability configuration
   * @param previousProperties previous capability configuration
   */
  public void update(final Map<String, String> properties, final Map<String, String> previousProperties) {
    if (!sameProperties(previousProperties, properties)) {
      try {
        stateLock.writeLock().lock();
        state.update(properties, previousProperties);
      }
      finally {
        stateLock.writeLock().unlock();
      }
    }
  }

  /**
   * Callback when a capability configuration is removed.
   */
  public void remove() {
    try {
      stateLock.writeLock().lock();
      state.remove();
    }
    finally {
      stateLock.writeLock().unlock();
    }
  }

  @Override
  public Map<String, String> properties() {
    try {
      stateLock.readLock().lock();
      return capabilityProperties;
    }
    finally {
      stateLock.readLock().unlock();
    }
  }

  @Override
  public String stateDescription() {
    try {
      stateLock.readLock().lock();
      return state.stateDescription();
    }
    finally {
      stateLock.readLock().unlock();
    }
  }

  @Override
  public String toString() {
    return String.format("capability %s (enabled=%s, active=%s)", capability, isEnabled(), isActive());
  }

  // @TestAccessible //
  static boolean sameProperties(final Map<String, String> p1, final Map<String, String> p2) {
    if (p1 == null) {
      return p2 == null;
    }
    else if (p2 == null) {
      return false;
    }
    return p1.size() == p2.size() && p1.equals(p2);
  }

  private void resetLastException() {
    setLastException(null);
  }

  private void setLastException(final Exception e) {
    lastException = e;
  }

  private class State
  {

    State() {
      getLogger().debug(
          "Capability {} ({}) state changed to {}", new Object[]{capability, id, this}
      );
    }

    public boolean isEnabled() {
      return false;
    }

    public void enable() {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'enable' operation");
    }

    public void disable() {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'disable' operation");
    }

    public boolean isActive() {
      return false;
    }

    public void activate() {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'activate' operation");
    }

    public void passivate() {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'passivate' operation");
    }

    public void create(final Map<String, String> properties) {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'create' operation");
    }

    public void load(final Map<String, String> properties) {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'load' operation");
    }

    public void update(final Map<String, String> properties, final Map<String, String> previousProperties) {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'update' operation");
    }

    public void remove() {
      throw new IllegalStateException("State '" + toString() + "' does not permit 'remove' operation");
    }

    public String stateDescription() {
      return "Undefined";
    }

    @Override
    public String toString() {
      return getClass().getSimpleName();
    }

    void setDescription(final String description) {
      // do nothing
    }

  }

  private class NewState
      extends State
  {

    @Override
    public void create(final Map<String, String> properties) {
      try {
        capabilityProperties = properties == null ? EMPTY_MAP : unmodifiableMap(newHashMap(properties));
        capability.onCreate();
        resetLastException();
        validityHandler.bind();
        state = new ValidState();
      }
      catch (Exception e) {
        setLastException(e);
        state = new InvalidState("Failed to create: " + e.getMessage());
        getLogger().error(
            "Could not create capability {} ({})", new Object[]{capability, id, e}
        );
      }
    }

    @Override
    public void load(final Map<String, String> properties) {
      try {
        capabilityProperties = properties == null ? EMPTY_MAP : unmodifiableMap(newHashMap(properties));
        capability.onLoad();
        resetLastException();
        validityHandler.bind();
        state = new ValidState();
      }
      catch (Exception e) {
        setLastException(e);
        state = new InvalidState("Failed to load: " + e.getMessage());
        getLogger().error(
            "Could not load capability {} ({})", new Object[]{capability, id, e}
        );
      }
    }

    @Override
    public String stateDescription() {
      return "New";
    }

    @Override
    public String toString() {
      return "NEW";
    }

  }

  private class ValidState
      extends State
  {

    @Override
    public void enable() {
      getLogger().debug("Enabling capability {} ({})", capability, id);
      state = new EnabledState("Not yet activated");
      activationHandler.bind();
    }

    @Override
    public void disable() {
      // do nothing (not yet enabled)
    }

    @Override
    public void passivate() {
      // do nothing (not yet activated)
    }

    @Override
    public void update(final Map<String, String> properties, final Map<String, String> previousProperties) {
      try {
        eventBus.post(
            new CapabilityEvent.BeforeUpdate(
                capabilityRegistry, DefaultCapabilityReference.this, properties, previousProperties
            )
        );
        capabilityProperties = properties == null ? EMPTY_MAP : unmodifiableMap(newHashMap(properties));
        capability.onUpdate();
        resetLastException();
        eventBus.post(
            new CapabilityEvent.AfterUpdate(
                capabilityRegistry, DefaultCapabilityReference.this, properties, previousProperties
            )
        );
      }
      catch (Exception e) {
        setLastException(e);
        getLogger().error(
            "Could not update capability {} ({}).", new Object[]{capability, id, e}
        );
        DefaultCapabilityReference.this.passivate();
        state.setDescription("Update failed: " + e.getMessage());
      }
    }

    @Override
    public void remove() {
      try {
        DefaultCapabilityReference.this.disable();
        validityHandler.release();
        capability.onRemove();
        resetLastException();
        state = new RemovedState();
        eventBus.post(
            new CapabilityEvent.AfterRemove(capabilityRegistry, DefaultCapabilityReference.this)
        );
      }
      catch (Exception e) {
        setLastException(e);
        state = new InvalidState("Failed to remove: " + e.getMessage());
        getLogger().error(
            "Could not remove capability {} ({})", new Object[]{capability, id, e}
        );
      }
    }

    @Override
    public String stateDescription() {
      return "Disabled";
    }

    @Override
    public String toString() {
      return "VALID";
    }

  }

  private class EnabledState
      extends ValidState
  {

    private String description;

    EnabledState() {
      this("enabled");
    }

    EnabledState(final String description) {
      this.description = description;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public void enable() {
      // do nothing (already enabled)
    }

    @Override
    public void disable() {
      getLogger().debug("Disabling capability {} ({})", capability, id);
      activationHandler.release();
      DefaultCapabilityReference.this.passivate();
      state = new ValidState();
    }

    @Override
    public void activate() {
      if (activationHandler.isConditionSatisfied()) {
        getLogger().debug("Activating capability {} ({})", capability, id);
        try {
          capability.onActivate();
          resetLastException();
          getLogger().debug("Activated capability {} ({})", capability, id);
          state = new ActiveState();
          eventBus.post(
              new CapabilityEvent.AfterActivated(capabilityRegistry, DefaultCapabilityReference.this)
          );
        }
        catch (Exception e) {
          setLastException(e);
          getLogger().error(
              "Could not activate capability {} ({})", new Object[]{capability, id, e}
          );
          state.setDescription("Activation failed: " + e.getMessage());
        }
      }
      else {
        getLogger().debug("Capability {} ({}) is not yet activatable", capability, id);
      }
    }

    @Override
    public void passivate() {
      // do nothing (not yet activated)
    }

    @Override
    public String stateDescription() {
      return activationHandler.isConditionSatisfied() ? description : activationHandler.explainWhyNotSatisfied();
    }

    @Override
    public String toString() {
      return "ENABLED";
    }

    @Override
    void setDescription(final String description) {
      this.description = description;
    }
  }

  private class ActiveState
      extends EnabledState
  {

    @Override
    public boolean isActive() {
      return true;
    }

    @Override
    public void activate() {
      // do nothing (already active)
    }

    @Override
    public void passivate() {
      getLogger().debug("Passivating capability {} ({})", capability, id);
      try {
        state = new EnabledState("Passivated");
        eventBus.post(
            new CapabilityEvent.BeforePassivated(capabilityRegistry, DefaultCapabilityReference.this)
        );
        capability.onPassivate();
        resetLastException();
        getLogger().debug("Passivated capability {} ({})", capability, id);
      }
      catch (Exception e) {
        setLastException(e);
        getLogger().error(
            "Could not passivate capability {} ({})", new Object[]{capability, id, e}
        );
        state.setDescription("Passivation failed: " + e.getMessage());
      }
    }

    @Override
    public String stateDescription() {
      return "Active";
    }

    @Override
    public String toString() {
      return "ACTIVE";
    }

  }

  private class InvalidState
      extends State
  {

    private final String reason;

    InvalidState(final String reason) {
      this.reason = reason;
    }

    @Override
    public String stateDescription() {
      return reason;
    }

    @Override
    public String toString() {
      return "INVALID (" + reason + ")";
    }

  }

  public class RemovedState
      extends State
  {

    @Override
    public String stateDescription() {
      return "Removed";
    }

    @Override
    public String toString() {
      return "REMOVED";
    }

  }

}
