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
package org.sonatype.nexus.extjs;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * ExtJS class definition record.
 *
 * @since 3.0
 */
public class ClassDef
{
  private final String name;

  private final File source;

  private final Set<String> dependencies = Sets.newHashSet();

  private String extend;

  private String override;

  private List<String> requires;

  private List<String> mixins;

  private List<String> uses;

  public ClassDef(final String name, final File source) {
    this.name = name;
    this.source = source;
  }

  public String getName() {
    return name;
  }

  public File getSource() {
    return source;
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

  public String getExtend() {
    return extend;
  }

  public void setExtend(final String extend) {
    this.extend = extend;
    dependencies.add(extend);
  }

  public String getOverride() {
    return override;
  }

  public void setOverride(final String override) {
    this.override = override;
    dependencies.add(override);
  }

  public List<String> getRequires() {
    return requires;
  }

  public void setRequires(final List<String> requires) {
    this.requires = requires;
    dependencies.addAll(requires);
  }

  public List<String> getMixins() {
    return mixins;
  }

  public void setMixins(final List<String> mixins) {
    this.mixins = mixins;
    dependencies.addAll(mixins);
  }

  public List<String> getUses() {
    return uses;
  }

  public void setUses(final List<String> uses) {
    this.uses = uses;
    dependencies.addAll(uses);
  }
}
