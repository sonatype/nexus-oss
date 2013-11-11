/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.sonatype.nexus.velocity;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * Nexus preconfigured ans shared Velocity provider.
 *
 * @since 2.8.0
 */
@Named
@Singleton
public class VelocityEngineProvider
    extends ComponentSupport
    implements Provider<VelocityEngine>
{
  private final VelocityEngine sharedVelocityEngine;

  @Inject
  public VelocityEngineProvider() {
    this.sharedVelocityEngine = createEngine();
  }

  @Override
  public VelocityEngine get() {
    return sharedVelocityEngine;
  }

  private VelocityEngine createEngine() {
    log.info("Creating Nexus VelocityEngine");

    VelocityEngine velocityEngine = new VelocityEngine();
    // log using our chute (slf4j with level fix)
    velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new Slf4jLogChute());

    // setting various defaults
    // ========================
    // to avoid "unable to find resource 'VM_global_library.vm' in any resource loader."
    velocityEngine.setProperty("velocimacro.library", "");
    // to use classpath loader
    velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
    velocityEngine.setProperty("class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    // to make us strict with template references (early problem detection)
    velocityEngine.setProperty("runtime.references.strict", "true");

    velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
    velocityEngine.setProperty("class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    // to set caching ON
    velocityEngine.setProperty("class.resource.loader.cache", "true");
    // to never check for template modification (they are JARred)
    velocityEngine.setProperty("class.resource.loader.modificationCheckInterval", "0");
    // to set strict mode OFF
    velocityEngine.setProperty("runtime.references.strict", "false");
    // to force templates having inline local scope for VM definitions
    velocityEngine.setProperty("velocimacro.permissions.allow.inline.local.scope", "true");

    // fire up the engine
    // ==================
    try {
      velocityEngine.init();
    }
    catch (Exception e) {
      throw new IllegalStateException("Cannot initialize VelocityEngine", e);
    }
    return velocityEngine;
  }
}
