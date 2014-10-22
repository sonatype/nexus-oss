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
package org.sonatype.nexus.component.services.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.ComponentId;

import org.joda.time.DateTime;

/**
 * Component subclass for testing.
 */
public class TestComponent
    implements Component
{
  private ComponentId id;
  private byte[] binaryProp;
  private Boolean booleanProp;
  private Byte byteProp;
  private DateTime datetimeProp;
  private Double doubleProp;
  private List<String> embeddedListProp;
  private Map<String, String> embeddedMapProp;
  private Set<String> embeddedSetProp;
  private Float floatProp;
  private Integer integerProp;
  private Long longProp;
  private Short shortProp;
  private String stringProp;
  private Object unregisteredProp;

  @Nullable
  @Override
  public ComponentId getId() {
    return new ComponentId() {
      @Override
      public String asUniqueString() {
        return id.asUniqueString();
      }
    };
  }

  public void setId(final ComponentId id) {
    this.id = id;
  }

  public byte[] getBinaryProp() {
    return binaryProp;
  }

  public void setBinaryProp(final byte[] binaryProp) {
    this.binaryProp = binaryProp;
  }

  public Boolean getBooleanProp() {
    return booleanProp;
  }

  public void setBooleanProp(final Boolean booleanProp) {
    this.booleanProp = booleanProp;
  }

  public Byte getByteProp() {
    return byteProp;
  }

  public void setByteProp(final Byte byteProp) {
    this.byteProp = byteProp;
  }

  public DateTime getDatetimeProp() {
    return datetimeProp;
  }

  public void setDatetimeProp(final DateTime datetimeProp) {
    this.datetimeProp = datetimeProp;
  }

  public Double getDoubleProp() {
    return doubleProp;
  }

  public void setDoubleProp(final Double doubleProp) {
    this.doubleProp = doubleProp;
  }

  public List<String> getEmbeddedListProp() {
    return embeddedListProp;
  }

  public void setEmbeddedListProp(final List<String> embeddedListProp) {
    this.embeddedListProp = embeddedListProp;
  }

  public Map<String, String> getEmbeddedMapProp() {
    return embeddedMapProp;
  }

  public void setEmbeddedMapProp(final Map<String, String> embeddedMapProp) {
    this.embeddedMapProp = embeddedMapProp;
  }

  public Set<String> getEmbeddedSetProp() {
    return embeddedSetProp;
  }

  public void setEmbeddedSetProp(final Set<String> embeddedSetProp) {
    this.embeddedSetProp = embeddedSetProp;
  }

  public Float getFloatProp() {
    return floatProp;
  }

  public void setFloatProp(final Float floatProp) {
    this.floatProp = floatProp;
  }

  public Integer getIntegerProp() {
    return integerProp;
  }

  public void setIntegerProp(final Integer integerProp) {
    this.integerProp = integerProp;
  }

  public Long getLongProp() {
    return longProp;
  }

  public void setLongProp(final Long longProp) {
    this.longProp = longProp;
  }

  public Short getShortProp() {
    return shortProp;
  }

  public void setShortProp(final Short shortProp) {
    this.shortProp = shortProp;
  }

  public String getStringProp() {
    return stringProp;
  }

  public void setStringProp(final String stringProp) {
    this.stringProp = stringProp;
  }

  public Object getUnregisteredProp() {
    return unregisteredProp;
  }

  public void setUnregisteredProp(final Object unregisteredProp) {
    this.unregisteredProp = unregisteredProp;
  }
}
