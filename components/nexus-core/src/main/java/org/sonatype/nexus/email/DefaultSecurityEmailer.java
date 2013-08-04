/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.email;

import java.util.List;

import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.security.email.SecurityEmailer;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * The default emailer that is "stolen" by Security. Look at the NexusEmailer for the real thing.
 *
 * @author cstamas
 */
@Component(role = SecurityEmailer.class)
public class DefaultSecurityEmailer
    implements SecurityEmailer
{
  @Requirement
  private NexusEmailer nexusEmailer;

  public void sendNewUserCreated(String email, String userid, String password) {
    StringBuilder body = new StringBuilder();
    body.append("User Account ");
    body.append(userid);
    body.append(" has been created.  Another email will be sent shortly containing your password.");

    MailRequest request = nexusEmailer.getDefaultMailRequest("Nexus: New user account created.", body.toString());

    request.getToAddresses().add(new Address(email));

    nexusEmailer.sendMail(request);

    body = new StringBuilder();
    body.append("Your new password is ");
    body.append(password);

    request = nexusEmailer.getDefaultMailRequest("Nexus: New user account created.", body.toString());

    request.getToAddresses().add(new Address(email));

    nexusEmailer.sendMail(request);
  }

  public void sendForgotUsername(String email, List<String> userIds) {
    StringBuilder body = new StringBuilder();

    body.append("Your email is associated with the following Nexus User Id(s):\n ");
    for (String userId : userIds) {
      body.append("\n - \"");
      body.append(userId);
      body.append("\"");
    }

    MailRequest request = nexusEmailer.getDefaultMailRequest("Nexus: User account notification.", body.toString());

    request.getToAddresses().add(new Address(email));

    nexusEmailer.sendMail(request);
  }

  public void sendResetPassword(String email, String password) {
    StringBuilder body = new StringBuilder();
    body.append("Your password has been reset.  Your new password is: ");
    body.append(password);

    MailRequest request = nexusEmailer.getDefaultMailRequest("Nexus: User account notification.", body.toString());

    request.getToAddresses().add(new Address(email));

    nexusEmailer.sendMail(request);
  }
}
