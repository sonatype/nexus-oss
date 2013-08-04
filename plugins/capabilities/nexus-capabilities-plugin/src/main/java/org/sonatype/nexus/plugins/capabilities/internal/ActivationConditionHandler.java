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

import javax.inject.Inject;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.CapabilityContextAware;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.ConditionEvent;
import org.sonatype.nexus.plugins.capabilities.internal.condition.SatisfiedCondition;
import org.sonatype.nexus.plugins.capabilities.internal.condition.UnsatisfiedCondition;
import org.sonatype.nexus.plugins.capabilities.support.condition.Conditions;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles capability activation by reacting capability activation condition being satisfied/unsatisfied.
 *
 * @since 2.0
 */
public class ActivationConditionHandler
    extends AbstractLoggingComponent
{

  private final EventBus eventBus;

  private final DefaultCapabilityReference reference;

  private final Conditions conditions;

  private Condition activationCondition;

  private Condition nexusActiveCondition;

  @Inject
  ActivationConditionHandler(final EventBus eventBus,
                             final Conditions conditions,
                             final @Assisted DefaultCapabilityReference reference)
  {
    this.eventBus = checkNotNull(eventBus);
    this.conditions = checkNotNull(conditions);
    this.reference = checkNotNull(reference);
  }

  boolean isConditionSatisfied() {
    return activationCondition != null && activationCondition.isSatisfied();
  }

  @AllowConcurrentEvents
  @Subscribe
  public void handle(final ConditionEvent.Satisfied event) {
    if (event.getCondition() == activationCondition) {
      reference.activate();
    }
  }

  @AllowConcurrentEvents
  @Subscribe
  public void handle(final ConditionEvent.Unsatisfied event) {
    if (event.getCondition() == activationCondition || event.getCondition() == nexusActiveCondition) {
      reference.passivate();
    }
  }

  ActivationConditionHandler bind() {
    if (activationCondition == null) {
      nexusActiveCondition = conditions.nexus().active();
      try {
        activationCondition = reference.capability().activationCondition();
        if (activationCondition instanceof CapabilityContextAware) {
          ((CapabilityContextAware) activationCondition).setContext(reference.context());
        }
      }
      catch (Exception e) {
        activationCondition = new UnsatisfiedCondition("Failed to determine activation condition");
        getLogger().error(
            "Could not get activation condition from capability {} ({}). Considering it as non activatable",
            new Object[]{reference.capability(), reference.context().id(), e}
        );
      }
      if (activationCondition == null) {
        activationCondition = new SatisfiedCondition("Capability has no activation condition");
      }
      nexusActiveCondition.bind();
      activationCondition.bind();
      eventBus.register(this);
    }
    return this;
  }

  ActivationConditionHandler release() {
    if (activationCondition != null) {
      eventBus.unregister(this);
      nexusActiveCondition.release();
      activationCondition.release();
      activationCondition = null;
    }
    return this;
  }

  @Override
  public String toString() {
    return String.format(
        "Watching '%s' condition to activate/passivate capability '%s (id=%s)'",
        activationCondition, reference.capability(), reference.context().id()
    );
  }

  public String explainWhyNotSatisfied() {
    return isConditionSatisfied() ? null : activationCondition.explainUnsatisfied();
  }

}
