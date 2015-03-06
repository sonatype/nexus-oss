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
package org.sonatype.nexus.elasticsearch.internal;

import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.ScriptModule;

/**
 * @since 3.0
 */
public class ScriptBridgePlugin
    extends AbstractPlugin
{
  private static Map<String, NativeScriptFactory> scripts;

  public static void setScripts(final Map<String, NativeScriptFactory> scripts) {
    ScriptBridgePlugin.scripts = scripts;
  }

  @Override
  public String name() {
    return "scriptBridge";
  }

  @Override
  public String description() {
    return "Bridges scripts Nexus ES scripts";
  }

  public void onModule(final ScriptModule scriptModule) {
    scriptModule.registerScript("bridge", BridgedNativeScriptFactory.class);
  }

  public static class BridgedNativeScriptFactory
      implements NativeScriptFactory
  {

    @Override
    public ExecutableScript newScript(@Nullable final Map<String, Object> params) {
      if (params == null) {
        throw new IllegalArgumentException("Missing bridged script name");
      }
      Object name = params.get("name");
      if (name == null) {
        throw new IllegalArgumentException("Missing bridged script name");
      }
      NativeScriptFactory scriptFactory = scripts.get(name.toString());
      if (scriptFactory == null) {
        throw new IllegalArgumentException("Bridged script '" + name + "' not found");
      }
      return scriptFactory.newScript(params);
    }
  }

}
