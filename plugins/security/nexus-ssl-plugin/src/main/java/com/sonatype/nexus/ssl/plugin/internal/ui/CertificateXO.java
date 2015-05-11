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

package com.sonatype.nexus.ssl.plugin.internal.ui;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "certificate", propOrder = {
    "id",
    "fingerprint",
    "pem",
    "serialNumber",
    "subjectCommonName",
    "subjectOrganization",
    "subjectOrganizationalUnit",
    "issuerCommonName",
    "issuerOrganization",
    "issuerOrganizationalUnit",
    "issuedOn",
    "expiresOn",
    "inNexusSSLTrustStore"
})
@XmlRootElement(name = "certificate")
public class CertificateXO
{
  @XmlElement(required = true)
  @JsonProperty("id")
  private String id;

  @XmlElement(required = true)
  @JsonProperty("fingerprint")
  private String fingerprint;

  @XmlElement(required = true)
  @JsonProperty("pem")
  private String pem;

  @XmlElement(required = true)
  @JsonProperty("serialNumber")
  private String serialNumber;

  @XmlElement(required = true)
  @JsonProperty("subjectCommonName")
  private String subjectCommonName;

  @XmlElement(required = true)
  @JsonProperty("subjectOrganization")
  private String subjectOrganization;

  @XmlElement(required = true)
  @JsonProperty("subjectOrganizationalUnit")
  private String subjectOrganizationalUnit;

  @XmlElement(required = true)
  @JsonProperty("issuerCommonName")
  private String issuerCommonName;

  @XmlElement(required = true)
  @JsonProperty("issuerOrganization")
  private String issuerOrganization;

  @XmlElement(required = true)
  @JsonProperty("issuerOrganizationalUnit")
  private String issuerOrganizationalUnit;

  @JsonProperty("issuedOn")
  private long issuedOn;

  @JsonProperty("expiresOn")
  private long expiresOn;

  @JsonProperty("inNexusSSLTrustStore")
  private boolean inNexusSSLTrustStore;

  public String getId() {
    return id;
  }

  public void setId(String value) {
    this.id = value;
  }

  public String getFingerprint() {
    return fingerprint;
  }

  public void setFingerprint(String value) {
    this.fingerprint = value;
  }

  public String getPem() {
    return pem;
  }

  public void setPem(String value) {
    this.pem = value;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String value) {
    this.serialNumber = value;
  }

  public String getSubjectCommonName() {
    return subjectCommonName;
  }

  public void setSubjectCommonName(String value) {
    this.subjectCommonName = value;
  }

  public String getSubjectOrganization() {
    return subjectOrganization;
  }

  public void setSubjectOrganization(String value) {
    this.subjectOrganization = value;
  }

  public String getSubjectOrganizationalUnit() {
    return subjectOrganizationalUnit;
  }

  public void setSubjectOrganizationalUnit(String value) {
    this.subjectOrganizationalUnit = value;
  }

  public String getIssuerCommonName() {
    return issuerCommonName;
  }

  public void setIssuerCommonName(String value) {
    this.issuerCommonName = value;
  }

  public String getIssuerOrganization() {
    return issuerOrganization;
  }

  public void setIssuerOrganization(String value) {
    this.issuerOrganization = value;
  }

  public String getIssuerOrganizationalUnit() {
    return issuerOrganizationalUnit;
  }

  public void setIssuerOrganizationalUnit(String value) {
    this.issuerOrganizationalUnit = value;
  }

  public long getIssuedOn() {
    return issuedOn;
  }

  public void setIssuedOn(long value) {
    this.issuedOn = value;
  }

  public long getExpiresOn() {
    return expiresOn;
  }

  public void setExpiresOn(long value) {
    this.expiresOn = value;
  }

  public boolean isInNexusSSLTrustStore() {
    return inNexusSSLTrustStore;
  }

  public void setInNexusSSLTrustStore(boolean value) {
    this.inNexusSSLTrustStore = value;
  }

  public CertificateXO withId(String value) {
    setId(value);
    return this;
  }

  public CertificateXO withFingerprint(String value) {
    setFingerprint(value);
    return this;
  }

  public CertificateXO withPem(String value) {
    setPem(value);
    return this;
  }

  public CertificateXO withSerialNumber(String value) {
    setSerialNumber(value);
    return this;
  }

  public CertificateXO withSubjectCommonName(String value) {
    setSubjectCommonName(value);
    return this;
  }

  public CertificateXO withSubjectOrganization(String value) {
    setSubjectOrganization(value);
    return this;
  }

  public CertificateXO withSubjectOrganizationalUnit(String value) {
    setSubjectOrganizationalUnit(value);
    return this;
  }

  public CertificateXO withIssuerCommonName(String value) {
    setIssuerCommonName(value);
    return this;
  }

  public CertificateXO withIssuerOrganization(String value) {
    setIssuerOrganization(value);
    return this;
  }

  public CertificateXO withIssuerOrganizationalUnit(String value) {
    setIssuerOrganizationalUnit(value);
    return this;
  }

  public CertificateXO withIssuedOn(long value) {
    setIssuedOn(value);
    return this;
  }

  public CertificateXO withExpiresOn(long value) {
    setExpiresOn(value);
    return this;
  }

  public CertificateXO withInNexusSSLTrustStore(boolean value) {
    setInNexusSSLTrustStore(value);
    return this;
  }
}
