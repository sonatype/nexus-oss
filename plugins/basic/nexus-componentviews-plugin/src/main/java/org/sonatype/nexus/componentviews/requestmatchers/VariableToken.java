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
package org.sonatype.nexus.componentviews.requestmatchers;

/**
 * A named variable that matches a regular expression.
 *
 * @since 3.0
 */
public class VariableToken
    extends Token
{
  private final String name;

  public VariableToken(final String name, final String regexp) {
    super(regexp);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toRegexp() {
    return "(" + value + ")";
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VariableToken)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return name.equals(((VariableToken) o).name) && super.equals(o);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  public String toString() {
    return "var(" + name + "," + value + ")";
  }
}
