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
package com.sonatype.security.ldap.persist.validation;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sonatype.security.ldap.AbstractLdapConfigurationTest;
import com.sonatype.security.ldap.persist.LdapConfigurationSource;
import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class LdapConfigurationValidationTest
    extends AbstractLdapConfigurationTest
{

  private Map<String, ExpectedResult> expectedResultMap = new LinkedHashMap<>();

  public LdapConfigurationValidationTest() {
    super();
    this.expectedResultMap.put("0", new ExpectedResult(0, 0, "No Servers"));
    this.expectedResultMap.put("1", new ExpectedResult(0, 1, "missing protocol"));
    this.expectedResultMap.put("2",
        new ExpectedResult(0, 0, "missing id"));// on load warnings are not returned, but this should have 1 warning
    this.expectedResultMap.put("3", new ExpectedResult(0, 1, "missing name"));
    this.expectedResultMap.put("4", new ExpectedResult(0, 1, "missing conection info"));
    this.expectedResultMap.put("5", new ExpectedResult(0, 1, "Missing userAndGroupConfig"));
    this.expectedResultMap.put("6",
        new ExpectedResult(0, 4, "Missing a bunch of conn info (user/pass is only checked if scheme is set)"));
    this.expectedResultMap.put("7", new ExpectedResult(0, 0, "missing caching info"));
    this.expectedResultMap.put("8", new ExpectedResult(0, 2, "missing user/pass"));
    this.expectedResultMap.put("9", new ExpectedResult(0, 8,
        "missing a buch of user / group (userbase + groupbase DN, userPasswordAttribute, not requried"));

  }

  @Test
  public void testValidation()
      throws Exception
  {
    File validationDirectory = new File("target/test-classes/validation");

    for (Entry<String, ExpectedResult> entry : expectedResultMap.entrySet()) {
      File validationFile = new File(validationDirectory, "ldap-" + entry.getKey() + ".xml");

      //Assert.assertTrue("File: " + validationFile.getAbsolutePath() + " does not exists", validationFile.exists());

      ExpectedResult actualResult = this.runValidation(validationFile.getAbsoluteFile());
      Assert.assertEquals("File " + validationFile, entry.getValue(), actualResult);
    }
  }

  private ExpectedResult runValidation(File ldapXml)
      throws Exception
  {
    // copy ldap.xml to conf dir
    File inplaceLdapXml = new File(getConfHomeDir(), "ldap.xml");
    FileUtils.copyFile(ldapXml, inplaceLdapXml);

    LdapConfigurationSource source = this.lookup(LdapConfigurationSource.class);
    try {
      // Note: This test original also used configuration source, but it does not validate anymore
      // it is done by manager. Hence, validatio code added here below
      final CLdapConfiguration conf = source.load();
      final ValidationResponse vr = lookup(LdapConfigurationValidator.class).validateModel(new ValidationRequest<CLdapConfiguration>(conf));
      if (!vr.isValid()) {
        throw new InvalidConfigurationException(vr);
      }
    }
    catch (InvalidConfigurationException e) {
      return new ExpectedResult(e.getValidationResponse());
    }

    return new ExpectedResult(0, 0, "No Config Error");

  }

  class ExpectedResult
  {
    private int warnings;

    private int errors;

    private String notes;

    private ValidationResponse validationResponse;

    public ExpectedResult(int warnings, int errors, String notes) {
      this.warnings = warnings;
      this.errors = errors;
      this.notes = notes;
    }

    public ExpectedResult(ValidationResponse validationResponse) {
      this.warnings = validationResponse.getValidationWarnings().size();
      this.errors = validationResponse.getValidationErrors().size();
      this.validationResponse = validationResponse;
    }

    public int getWarnings() {
      return warnings;
    }

    public int getErrors() {
      return errors;
    }

    public String toString() {
      return "result: [Errors: " + this.errors + ", Warnings: " + this.warnings + " Notes: " + this.notes + "]" + (
          (validationResponse == null) ? "" : this.getValidationMessage());
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + errors;
      result = prime * result + warnings;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ExpectedResult other = (ExpectedResult) obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (errors != other.errors) {
        return false;
      }
      if (warnings != other.warnings) {
        return false;
      }
      return true;
    }

    private LdapConfigurationValidationTest getOuterType() {
      return LdapConfigurationValidationTest.this;
    }

    public String getValidationMessage() {
      StringWriter sw = new StringWriter();

      if (this.validationResponse != null) {
        if (validationResponse.getValidationErrors().size() > 0) {
          sw.append("\nValidation errors follows:\n");

          for (ValidationMessage error : validationResponse.getValidationErrors()) {
            sw.append(error.toString());
          }
          sw.append("\n");
        }

        if (validationResponse.getValidationWarnings().size() > 0) {
          sw.append("\nValidation warnings follows:\n");

          for (ValidationMessage warning : validationResponse.getValidationWarnings()) {
            sw.append(warning.toString());
          }
          sw.append("\n");
        }
      }

      return sw.toString();
    }

  }

}
