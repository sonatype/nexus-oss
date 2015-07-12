/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.node;

import java.security.cert.Certificate;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.capability.CapabilitySupport;
import org.sonatype.nexus.common.node.LocalNodeAccess;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;
import org.sonatype.sisu.goodies.ssl.keystore.CertificateUtil;
import org.sonatype.sisu.goodies.ssl.keystore.KeyStoreManager;
import org.sonatype.sisu.goodies.ssl.keystore.KeystoreException;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Capability for generating identity key-pair.
 *
 * @since 3.0
 */
@Named(IdentityCapabilityDescriptor.TYPE_ID)
public class IdentityCapability
    extends CapabilitySupport<IdentityCapabilityConfiguration>
{
  private interface Messages
      extends MessageBundle
  {
    @DefaultMessage("%s")
    String description(String nodeId);
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final KeyStoreManager keyStoreManager;

  private final LocalNodeAccess localNode;

  private String fingerprint;

  @Inject
  public IdentityCapability(final @Named(KeyStoreManagerImpl.NAME) KeyStoreManager keyStoreManager,
                            final LocalNodeAccess localNode)
  {
    this.keyStoreManager = checkNotNull(keyStoreManager);
    this.localNode = checkNotNull(localNode);
  }

  @Override
  protected IdentityCapabilityConfiguration createConfig(final Map<String, String> properties) {
    return new IdentityCapabilityConfiguration(properties);
  }

  public Certificate certificate() throws KeystoreException {
    return keyStoreManager.getCertificate();
  }

  public String certificateAsPem() throws Exception {
    return CertificateUtil.serializeCertificateInPEM(certificate());
  }

  public String certificateFingerprint() throws Exception {
    return fingerprint;
  }

  @Override
  protected void configure(final IdentityCapabilityConfiguration config) throws Exception {
    // Generate identity key-pair if not already created
    if (!keyStoreManager.isKeyPairInitialized()) {
      log.info("Generating identity certificate");

      // For now give something unique to the cert for additional identification purposes
      UUID cn = UUID.randomUUID();
      keyStoreManager.generateAndStoreKeyPair(
          cn.toString(),
          "Nexus",
          "Sonatype",
          "Silver Spring",
          "MD",
          "US");
    }

    Certificate cert = certificate();
    log.trace("Certificate:\n{}", cert);

    fingerprint = CertificateUtil.calculateFingerprint(cert);
    log.debug("Fingerprint: {}", fingerprint);
  }

  @Override
  protected void onActivate(final IdentityCapabilityConfiguration config) throws Exception {
    // prime local node-id now
    localNode.getId();
  }

  @Override
  protected void onPassivate(final IdentityCapabilityConfiguration config) throws Exception {
    localNode.reset();
  }

  @Override
  protected void onRemove(final IdentityCapabilityConfiguration config) throws Exception {
    if (keyStoreManager.isKeyPairInitialized()) {
      log.debug("Clearing identity keys");
      keyStoreManager.removePrivateKey();
    }
    fingerprint = null;
  }

  @Override
  protected String renderDescription() throws Exception {
    return messages.description(localNode.getId());
  }

  @Override
  protected String renderStatus() throws Exception {
    return render(IdentityCapabilityDescriptor.TYPE_ID + "-status.vm", new TemplateParameters()
        .set("nodeId", localNode.getId())
        .set("fingerprint", fingerprint)
        .set("pem", certificateAsPem())
        .set("detail", certificate().toString())
    );
  }
}
