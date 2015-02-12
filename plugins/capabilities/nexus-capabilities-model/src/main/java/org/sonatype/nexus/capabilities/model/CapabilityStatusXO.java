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
@XmlType(name = "capabilityStatus", propOrder = {
    "capability",
    "description",
    "active",
    "error",
    "typeName",
    "stateDescription",
    "status",
    "tags"
})
@XmlRootElement(name = "capabilityStatus")
public class CapabilityStatusXO
{
  @XmlElement(required = true)
  @JsonProperty("capability")
  private CapabilityXO capability;

  @JsonProperty("description")
  private String description;

  @JsonProperty("active")
  private boolean active;

  @JsonProperty("error")
  private boolean error;

  @XmlElement(required = true)
  @JsonProperty("typeName")
  private String typeName;

  @JsonProperty("stateDescription")
  private String stateDescription;

  @JsonProperty("status")
  private String status;

  @JsonProperty("tags")
  private List<TagXO> tags;

  public CapabilityXO getCapability() {
    return capability;
  }

  public void setCapability(CapabilityXO value) {
    this.capability = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean value) {
    this.active = value;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean value) {
    this.error = value;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String value) {
    this.typeName = value;
  }

  public String getStateDescription() {
    return stateDescription;
  }

  public void setStateDescription(String value) {
    this.stateDescription = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public List<TagXO> getTags() {
    if (tags == null) {
      tags = new ArrayList<TagXO>();
    }
    return this.tags;
  }

  public void setTags(List<TagXO> value) {
    this.tags = null;
    List<TagXO> draftl = this.getTags();
    draftl.addAll(value);
  }

  public CapabilityStatusXO withCapability(CapabilityXO value) {
    setCapability(value);
    return this;
  }

  public CapabilityStatusXO withDescription(String value) {
    setDescription(value);
    return this;
  }

  public CapabilityStatusXO withActive(boolean value) {
    setActive(value);
    return this;
  }

  public CapabilityStatusXO withError(boolean value) {
    setError(value);
    return this;
  }

  public CapabilityStatusXO withTypeName(String value) {
    setTypeName(value);
    return this;
  }

  public CapabilityStatusXO withStateDescription(String value) {
    setStateDescription(value);
    return this;
  }

  public CapabilityStatusXO withStatus(String value) {
    setStatus(value);
    return this;
  }

  public CapabilityStatusXO withTags(TagXO... values) {
    if (values != null) {
      for (TagXO value : values) {
        getTags().add(value);
      }
    }
    return this;
  }

  public CapabilityStatusXO withTags(Collection<TagXO> values) {
    if (values != null) {
      getTags().addAll(values);
    }
    return this;
  }

  public CapabilityStatusXO withTags(List<TagXO> value) {
    setTags(value);
    return this;
  }
}
