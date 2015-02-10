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
@XmlType(name = "capabilityType", propOrder = {
    "id",
    "name",
    "about",
    "formFields"
})
@XmlRootElement(name = "capabilityType")
public class CapabilityTypeXO
{
  @XmlElement(required = true)
  @JsonProperty("id")
  private String id;

  @XmlElement(required = true)
  @JsonProperty("name")
  private String name;

  @JsonProperty("about")
  private String about;

  @JsonProperty("formFields")
  private List<FormFieldXO> formFields;

  public String getId() {
    return id;
  }

  public void setId(String value) {
    this.id = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getAbout() {
    return about;
  }

  public void setAbout(String value) {
    this.about = value;
  }

  public List<FormFieldXO> getFormFields() {
    if (formFields == null) {
      formFields = new ArrayList<FormFieldXO>();
    }
    return this.formFields;
  }

  public void setFormFields(List<FormFieldXO> value) {
    this.formFields = null;
    List<FormFieldXO> draftl = this.getFormFields();
    draftl.addAll(value);
  }

  public CapabilityTypeXO withId(String value) {
    setId(value);
    return this;
  }

  public CapabilityTypeXO withName(String value) {
    setName(value);
    return this;
  }

  public CapabilityTypeXO withAbout(String value) {
    setAbout(value);
    return this;
  }

  public CapabilityTypeXO withFormFields(FormFieldXO... values) {
    if (values != null) {
      for (FormFieldXO value : values) {
        getFormFields().add(value);
      }
    }
    return this;
  }

  public CapabilityTypeXO withFormFields(Collection<FormFieldXO> values) {
    if (values != null) {
      getFormFields().addAll(values);
    }
    return this;
  }

  public CapabilityTypeXO withFormFields(List<FormFieldXO> value) {
    setFormFields(value);
    return this;
  }
}
