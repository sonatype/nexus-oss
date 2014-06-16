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
package com.sonatype.security.ldap.api;

import java.util.ArrayList;

import com.sonatype.security.ldap.api.dto.LdapSchemaTemplateDTO;
import com.sonatype.security.ldap.api.dto.LdapSchemaTemplateListResponse;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerOrderRequest;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;

import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MarshalUnmarchalTest
{

  private XStream xstreamXML;

  private XStream xstreamJSON;

  @Before
  public void setUp()
      throws Exception
  {

    xstreamXML = new XStream(new LookAheadXppDriver());
    new XStreamInitalizer().initXStream(xstreamXML);

    xstreamJSON = new XStream(new JsonOrgHierarchicalStreamDriver());
    new XStreamInitalizer().initXStream(xstreamJSON);
  }

  @Test
  public void testDamiansProblem()
      throws Exception
  {
    LdapServerRequest resource = new LdapServerRequest();
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    resource.setData(dto);

    dto.setId("id");

    validateMarshalAndUnmarchal(resource);
  }

  @Test
  public void testTemplates() {

    LdapSchemaTemplateListResponse resource = new LdapSchemaTemplateListResponse();
    // LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    resource.setData(new ArrayList<LdapSchemaTemplateDTO>());

    LdapSchemaTemplateDTO template1Dto = new LdapSchemaTemplateDTO();
    resource.getData().add(template1Dto);
    template1Dto.setName("name");
    template1Dto.setUserAndGroupConfig(new LdapUserAndGroupAuthConfigurationDTO());
    template1Dto.getUserAndGroupConfig().setUserSubtree(true);

    validateMarshalAndUnmarchal(resource);
  }

  @Test
  public void testOrder() {
    LdapServerOrderRequest resource = new LdapServerOrderRequest();
    resource.getData().add("one");
    resource.getData().add("two");
    resource.getData().add("three");

    validateMarshalAndUnmarchal(resource);
  }

  @Test
  public void testEmptyUserAndGroupConfig() {
    LdapServerRequest request = new LdapServerRequest();
    request.setData(new LdapServerConfigurationDTO());

    // and with groupBaseDn set to null
    LdapUserAndGroupAuthConfigurationDTO dto = new LdapUserAndGroupAuthConfigurationDTO();
    dto.setGroupBaseDn(null);
    dto.setGroupIdAttribute("groupIdAttribute");
    request.getData().setUserAndGroupConfig(dto);

    validateMarshalAndUnmarchal(request);

    // and with groupBaseDn set
    dto = new LdapUserAndGroupAuthConfigurationDTO();
    dto.setGroupBaseDn("groupBaseDn");
    dto.setGroupIdAttribute("groupIdAttribute");
    request.getData().setUserAndGroupConfig(dto);

    validateMarshalAndUnmarchal(request);

    // simple json string with an explicit null value (generated from the Nexus UI)
    String payload = "{\"data\":{\"userAndGroupConfig\":{\"ldapFilter\":null}}}";

    StringBuffer sb =
        new StringBuffer("{ \"").append(LdapServerRequest.class.getName()).append("\" : ").append(payload).append(
            " }");
    // validate this parses without error
    xstreamJSON.fromXML(sb.toString());
  }

  private void validateMarshalAndUnmarchal(Object obj) {
    String xml = this.xstreamXML.toXML(obj);
    this.compareObjects(obj, xstreamXML.fromXML(xml));

    String json = this.xstreamJSON.toXML(obj);
    try {
      StringBuffer sb =
          new StringBuffer("{ \"").append(obj.getClass().getName()).append("\" : ").append(json).append(" }");

      this.compareObjects(obj, xstreamJSON.fromXML(sb.toString(), obj.getClass().newInstance()));
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }

    this.validateXmlHasNoPackageNames(obj);
  }

  private void compareObjects(Object expected, Object actual) {
    // ignore the modelEncoding field
    String[] ignoredFields = {"modelEncoding"};

    if (!DeepEqualsBuilder.reflectionDeepEquals(expected, actual, false, null, ignoredFields)) {
      // Print out the objects so we can compare them if it fails.
      Assert.fail("Expected objects to be equal: \nExpected:\n" + this.toDebugString(expected)
          + "\n\nActual:\n" + this.toDebugString(actual) + "\n\nExpected XML: "
          + this.xstreamXML.toXML(expected) + "\n\nActual:\n" + this.xstreamXML.toXML(actual));
    }

  }

  private void validateXmlHasNoPackageNames(Object obj) {
    String xml = this.xstreamXML.toXML(obj);

    // quick way of looking for the class="org attribute
    // i don't want to parse a dom to figure this out

    int totalCount = StringUtils.countMatches(xml, "org.sonatype");
    totalCount += StringUtils.countMatches(xml, "com.sonatype");

    // check the counts
    Assert.assertFalse("Found package name in XML:\n" + xml, totalCount > 0);

    // // print out each type of method, so i can rafb it
    //        System.out.println( "\n\nClass: " + obj.getClass() + "\n" );
    //        System.out.println( xml + "\n" );
    //
    // Assert.assertFalse( "Found <string> XML: " + obj.getClass() + "\n" + xml, xml.contains( "<string>" ) );
  }

  private String toDebugString(Object obj) {
    return ToStringBuilder.reflectionToString(obj, ToStringStyle.MULTI_LINE_STYLE);
  }

}
