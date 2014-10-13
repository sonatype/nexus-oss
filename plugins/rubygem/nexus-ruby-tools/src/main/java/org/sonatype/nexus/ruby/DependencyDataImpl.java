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
package org.sonatype.nexus.ruby;

import org.jruby.embed.ScriptingContainer;

/**
 * a wrapper around a JRuby object
 *
 * @author christian
 */
public class DependencyDataImpl
    extends ScriptWrapper
    implements DependencyData
{
  private final long modified;

  public DependencyDataImpl(ScriptingContainer scriptingContainer, Object dependencies, long modified) {
    super(scriptingContainer, dependencies);
    this.modified = modified;
  }

  @Override
  protected Object newScript() {
    throw new UnsupportedOperationException(); // TODO: wth?
  }

  @Override
  public String[] versions(boolean prereleased) {
    return callMethod("versions", prereleased, String[].class);
  }

  @Override
  public String platform(String version) {
    return callMethod("platform", version, String.class);
  }

  @Override
  public String name() {
    return callMethod("name", String.class);
  }

  @Override
  public long modified() {
    return modified;
  }

  @Override
  public String toString() {
    return callMethod("inspect", String.class);
  }
}