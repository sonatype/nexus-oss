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

package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;

/**
 * Describes a Nexus plugin: its metadata, exports/imports, and what resources it contains.
 */
@Deprecated
public final class PluginDescriptor
{
  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  private static final String LS = System.getProperty("line.separator");

  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final GAVCoordinate gav;

  private PluginMetadata metadata;

  private List<String> exportedClassnames = Collections.emptyList();

  private List<GAVCoordinate> importedPlugins = Collections.emptyList();

  private List<GAVCoordinate> resolvedPlugins = Collections.emptyList();

  private List<RepositoryTypeDescriptor> repositoryTypes = Collections.emptyList();

  private List<WebResource> staticResources = Collections.emptyList();

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  PluginDescriptor(final GAVCoordinate gav) {
    this.gav = gav;
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public GAVCoordinate getPluginCoordinates() {
    return gav;
  }

  public PluginMetadata getPluginMetadata() {
    return metadata;
  }

  public List<String> getExportedClassnames() {
    return exportedClassnames;
  }

  public List<GAVCoordinate> getImportedPlugins() {
    return importedPlugins;
  }

  public List<GAVCoordinate> getResolvedPlugins() {
    return resolvedPlugins;
  }

  public String formatAsString() {
    final StringBuilder buf = new StringBuilder();

    buf.append("       Detailed report about plugin \"").append(gav).append("\":").append(LS);

    if (metadata != null) {
      buf.append(LS);
      buf.append("         Source: \"").append(metadata.sourceUrl).append("\"").append(LS);
    }

    // TODO: list components? list exports/imports?

    if (repositoryTypes != null) {
      buf.append(LS);
      buf.append("         Custom repository types:").append(LS);

      for (final RepositoryTypeDescriptor type : repositoryTypes) {
        buf.append("         * Repository type \"").append(type.getRole().getName());
        buf.append("\", to be published at path \"").append(type.getPrefix()).append("\"").append(LS);
      }
    }

    if (staticResources != null) {
      buf.append(LS);
      buf.append("         Static resources:").append(LS);

      for (final WebResource resource : staticResources) {
        buf.append("         * Content type \"").append(resource.getContentType());
        buf.append("\", to be published at path \"").append(resource.getPath()).append("\"").append(LS);
      }
    }

    if (importedPlugins != null) {
      buf.append(LS);
      buf.append("         Imported plugins:").append(LS);

      for (final GAVCoordinate gav : importedPlugins) {
        buf.append("         * GAV \"").append(gav.toString()).append(LS);
      }
    }

    if (resolvedPlugins != null) {
      buf.append(LS);
      buf.append("        Resolved plugins:").append(LS);

      for (final GAVCoordinate gav : resolvedPlugins) {
        buf.append("         * GAV \"").append(gav.toString()).append(LS);
      }
    }

    return buf.toString();
  }

  // ----------------------------------------------------------------------
  // Locally-shared methods
  // ----------------------------------------------------------------------

  void setPluginMetadata(final PluginMetadata metadata) {
    this.metadata = metadata;
  }

  void setExportedClassnames(final List<String> classNames) {
    exportedClassnames = Collections.unmodifiableList(new ArrayList<String>(classNames));
  }

  void setImportedPlugins(final List<GAVCoordinate> plugins) {
    importedPlugins = Collections.unmodifiableList(new ArrayList<GAVCoordinate>(plugins));
  }

  void setResolvedPlugins(final List<GAVCoordinate> plugins) {
    resolvedPlugins = Collections.unmodifiableList(new ArrayList<GAVCoordinate>(plugins));
  }

  void setRepositoryTypes(final List<RepositoryTypeDescriptor> types) {
    repositoryTypes = Collections.unmodifiableList(new ArrayList<RepositoryTypeDescriptor>(types));
  }

  void setStaticResources(final List<WebResource> resources) {
    staticResources = Collections.unmodifiableList(new ArrayList<WebResource>(resources));
  }
}
