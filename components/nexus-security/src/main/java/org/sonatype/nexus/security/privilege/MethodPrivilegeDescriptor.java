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
package org.sonatype.nexus.security.privilege;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.security.config.CPrivilege;
import org.sonatype.nexus.security.config.SecurityConfigurationValidationContext;
import org.sonatype.nexus.validation.ValidationMessage;
import org.sonatype.nexus.validation.ValidationResponse;

@Named
@Singleton
public class MethodPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
  public static final String TYPE = "method";

  public static final String P_METHOD = "method";

  public static final String P_PERMISSION = "permission";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  protected String buildPermission(CPrivilege privilege) {
    if (!TYPE.equals(privilege.getType())) {
      return null;
    }

    String permission = privilege.getProperty(P_PERMISSION);
    String method = privilege.getProperty(P_METHOD);

    if (Strings2.isEmpty(permission)) {
      permission = "*:*";
    }

    if (Strings2.isEmpty(method)) {
      method = "*";
    }

    return permission + ":" + method;
  }

  @Override
  public ValidationResponse validatePrivilege(CPrivilege privilege, SecurityConfigurationValidationContext ctx, boolean update) {
    ValidationResponse response = super.validatePrivilege(privilege, ctx, update);

    if (!TYPE.equals(privilege.getType())) {
      return response;
    }

    // validate method
    // method is of form ('*' | 'read' | 'create' | 'update' | 'delete' [, method]* )
    // so, 'read' method is correct, but so is also 'create,update,delete'
    // '*' means ALL POSSIBLE value for this "field"
    String method = privilege.getProperty(P_METHOD);
    String permission = privilege.getProperty(P_PERMISSION);

    if (Strings2.isEmpty(permission)) {
      response.addError("Permission cannot be empty on a privilege!");
    }

    if (Strings2.isEmpty(method)) {
      response.addError("Method cannot be empty on a privilege!");
    }
    else {
      String[] methods = null;

      if (method.contains(",")) {
        // it is a list of methods
        methods = method.split(",");
      }
      else {
        // it is a single method
        methods = new String[]{method};
      }

      boolean valid = true;

      for (String singlemethod : methods) {
        if (!"create".equals(singlemethod) && !"delete".equals(singlemethod)
            && !"read".equals(singlemethod) && !"update".equals(singlemethod) && !"*".equals(singlemethod)) {
          valid = false;

          break;
        }
      }

      if (!valid) {
        ValidationMessage message =
            new ValidationMessage("method", "Privilege ID '" + privilege.getId()
                + "' Method is wrong! (Allowed methods are: create, delete, read and update)");
        response.addError(message);
      }
    }

    return response;
  }
}
