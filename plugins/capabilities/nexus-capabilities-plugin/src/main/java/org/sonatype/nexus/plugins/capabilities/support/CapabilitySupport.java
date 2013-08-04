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

package org.sonatype.nexus.plugins.capabilities.support;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.Capability;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.capabilities.Condition;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public abstract class CapabilitySupport
    extends AbstractLoggingComponent
    implements Capability
{

  private CapabilityContext context;

  /**
   * Returns capability context.
   *
   * @return capability context
   */
  protected CapabilityContext context() {
    checkState(context != null, "Capability was not yet initialized");
    return context;
  }

  @Override
  public void init(final CapabilityContext context) {
    this.context = checkNotNull(context);
  }

  @Override
  public String description() {
    return null;
  }

  @Override
  public String status() {
    return null;
  }

  @Override
  public void onCreate()
      throws Exception
  {
    // do nothing
  }

  @Override
  public void onLoad()
      throws Exception
  {
    // do nothing
  }

  @Override
  public void onUpdate()
      throws Exception
  {
    // do nothing
  }

  @Override
  public void onRemove()
      throws Exception
  {
    // do nothing
  }

  @Override
  public void onActivate()
      throws Exception
  {
    // do nothing
  }

  @Override
  public void onPassivate()
      throws Exception
  {
    // do nothing
  }

  /**
   * Returns null, meaning that this capability is always active.
   *
   * @return null
   */
  @Override
  public Condition activationCondition() {
    return null;
  }

  /**
   * Returns null, meaning that this capability is always valid.
   *
   * @return null
   */
  @Override
  public Condition validityCondition() {
    return null;
  }

}
