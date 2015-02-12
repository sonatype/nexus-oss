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

package org.sonatype.nexus.capabilities.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "capability", propOrder = {
    "id",
    "notes",
    "enabled",
    "typeId",
    "properties"
})
@XmlRootElement(name = "capability")
public class CapabilityXO
{
  @JsonProperty("id")
  private String id;

  @JsonProperty("notes")
  private String notes;

  @JsonProperty("enabled")
  private boolean enabled;

  @XmlElement(required = true)
  @JsonProperty("typeId")
  private String typeId;

  @JsonProperty("properties")
  private List<PropertyXO> properties;

  public String getId() {
    return id;
  }

  public void setId(String value) {
    this.id = value;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    this.notes = value;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean value) {
    this.enabled = value;
  }

  public String getTypeId() {
    return typeId;
  }

  public void setTypeId(String value) {
    this.typeId = value;
  }

  public List<PropertyXO> getProperties() {
    if (properties == null) {
      properties = new ArrayList<PropertyXO>();
    }
    return this.properties;
  }

  public void setProperties(List<PropertyXO> value) {
    this.properties = null;
    List<PropertyXO> draftl = this.getProperties();
    draftl.addAll(value);
  }

  public CapabilityXO withId(String value) {
    setId(value);
    return this;
  }

  public CapabilityXO withNotes(String value) {
    setNotes(value);
    return this;
  }

  public CapabilityXO withEnabled(boolean value) {
    setEnabled(value);
    return this;
  }

  public CapabilityXO withTypeId(String value) {
    setTypeId(value);
    return this;
  }

  public CapabilityXO withProperties(PropertyXO... values) {
    if (values != null) {
      for (PropertyXO value : values) {
        getProperties().add(value);
      }
    }
    return this;
  }

  public CapabilityXO withProperties(Collection<PropertyXO> values) {
    if (values != null) {
      getProperties().addAll(values);
    }
    return this;
  }

  public CapabilityXO withProperties(List<PropertyXO> value) {
    setProperties(value);
    return this;
  }
}
