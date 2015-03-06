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
package org.sonatype.nexus.repository.search;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;

import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT;

/**
 * {@link ComponentMetadata} factory.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ComponentMetadataFactory
{

  private final Map<String, ComponentMetadataProducer> componentMetadataProducers;

  @Inject
  public ComponentMetadataFactory(final Map<String, ComponentMetadataProducer> componentMetadataProducers) {
    this.componentMetadataProducers = checkNotNull(componentMetadataProducers);
  }

  /**
   * Creates a component metadata out of a component using {@link ComponentMetadataProducer} specific to component
   * {@link Format}. If one is not available will use a default one ({@link DefaultComponentMetadataProducer}).
   */
  public ComponentMetadata from(final OrientVertex component) {
    checkNotNull(component);
    String format = component.getProperty(P_FORMAT);
    ComponentMetadataProducer producer = componentMetadataProducers.get(format);
    if (producer == null) {
      producer = componentMetadataProducers.get("default");
    }
    checkState(producer != null, "Could not find a component metadata producer for format: {}", format);
    return from(component.getId().toString(), producer.getMetadata(component));
  }

  /**
   * Creates component metadata.
   *
   * @param id   component id
   * @param json component metadata (json format)
   */
  public ComponentMetadata from(final String id, final String json) {
    checkNotNull(id);
    checkNotNull(json);
    return new ComponentMetadata()
    {
      @Override
      public String getId() {
        return id;
      }

      @Override
      public String toJson() {
        return json;
      }
    };
  }


}
