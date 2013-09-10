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
package org.sonatype.nexus.plugin.internal;

import java.util.Collections;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.internal.VelocityTemplateEngine;
import org.sonatype.sisu.velocity.Velocity;
import org.sonatype.sisu.velocity.internal.VelocityConfigurator;
import org.sonatype.sisu.velocity.internal.VelocityImpl;

import org.apache.velocity.app.VelocityEngine;

/**
 * Creates shared {@link TemplateEngine}.
 *
 * @since 2.7
 */
@Named("shared")
@Singleton
public class SharedTemplateEngineProvider
    extends ComponentSupport
    implements Provider<TemplateEngine>
{
  private TemplateEngine engine;

  @Override
  public synchronized TemplateEngine get() {
    if (engine == null) {
      engine = create();
      log.debug("Created: {}", engine);
    }
    return engine;
  }

  private TemplateEngine create() {
    VelocityConfigurator configurator = new VelocityConfigurator()
    {
      @Override
      public void configure(final VelocityEngine engine) {
        // force templates to have inline local scope for VM definitions
        engine.setProperty("velocimacro.permissions.allow.inline.local.scope", "true");
      }
    };

    Velocity velocity = new VelocityImpl(Collections.singletonList(configurator));
    return new VelocityTemplateEngine(velocity);
  }

}
