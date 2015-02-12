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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "formField", propOrder = {
    "id",
    "type",
    "label",
    "helpText",
    "required",
    "regexValidation",
    "initialValue",
    "storePath",
    "storeRoot",
    "idMapping",
    "nameMapping"
})
public class FormFieldXO
{
  @XmlElement(required = true)
  @JsonProperty("id")
  private String id;

  @XmlElement(required = true)
  @JsonProperty("type")
  private String type;

  @XmlElement(required = true)
  @JsonProperty("label")
  private String label;

  @JsonProperty("helpText")
  private String helpText;

  @JsonProperty("required")
  private boolean required;

  @JsonProperty("regexValidation")
  private String regexValidation;

  @JsonProperty("initialValue")
  private String initialValue;

  @JsonProperty("storePath")
  private String storePath;

  @JsonProperty("storeRoot")
  private String storeRoot;

  @JsonProperty("idMapping")
  private String idMapping;

  @JsonProperty("nameMapping")
  private String nameMapping;

  public String getId() {
    return id;
  }

  public void setId(String value) {
    this.id = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String value) {
    this.type = value;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String value) {
    this.label = value;
  }

  public String getHelpText() {
    return helpText;
  }

  public void setHelpText(String value) {
    this.helpText = value;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean value) {
    this.required = value;
  }

  public String getRegexValidation() {
    return regexValidation;
  }

  public void setRegexValidation(String value) {
    this.regexValidation = value;
  }

  public String getInitialValue() {
    return initialValue;
  }

  public void setInitialValue(String value) {
    this.initialValue = value;
  }

  public String getStorePath() {
    return storePath;
  }

  public void setStorePath(String value) {
    this.storePath = value;
  }

  public String getStoreRoot() {
    return storeRoot;
  }

  public void setStoreRoot(String value) {
    this.storeRoot = value;
  }

  public String getIdMapping() {
    return idMapping;
  }

  public void setIdMapping(String value) {
    this.idMapping = value;
  }

  public String getNameMapping() {
    return nameMapping;
  }

  public void setNameMapping(String value) {
    this.nameMapping = value;
  }

  public FormFieldXO withId(String value) {
    setId(value);
    return this;
  }

  public FormFieldXO withType(String value) {
    setType(value);
    return this;
  }

  public FormFieldXO withLabel(String value) {
    setLabel(value);
    return this;
  }

  public FormFieldXO withHelpText(String value) {
    setHelpText(value);
    return this;
  }

  public FormFieldXO withRequired(boolean value) {
    setRequired(value);
    return this;
  }

  public FormFieldXO withRegexValidation(String value) {
    setRegexValidation(value);
    return this;
  }

  public FormFieldXO withInitialValue(String value) {
    setInitialValue(value);
    return this;
  }

  public FormFieldXO withStorePath(String value) {
    setStorePath(value);
    return this;
  }

  public FormFieldXO withStoreRoot(String value) {
    setStoreRoot(value);
    return this;
  }

  public FormFieldXO withIdMapping(String value) {
    setIdMapping(value);
    return this;
  }

  public FormFieldXO withNameMapping(String value) {
    setNameMapping(value);
    return this;
  }
}
