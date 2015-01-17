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
package org.sonatype.ldaptestsuite;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.sonatype.nexus.common.io.DirSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.DefaultServerEntry;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.schema.PartitionSchemaLoader;
import org.apache.directory.server.ldap.handlers.bind.MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.cramMD5.CramMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.digestMD5.DigestMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.gssapi.GssapiMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.ntlm.NtlmMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
import org.apache.directory.server.ldap.handlers.extended.StartTlsHandler;
import org.apache.directory.server.ldap.handlers.extended.StoredProcedureExtendedOperationHandler;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.schema.bootstrap.Schema;
import org.apache.directory.server.schema.registries.DefaultRegistries;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple wrapper for ApacheDS LDAP server.
 */
public class LdapServer
{
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final LdapServerConfiguration configuration;

  private int port;

  private DirectoryService directoryService;

  private org.apache.directory.server.ldap.LdapServer ldapService;

  /**
   * the context root for the system partition
   */
  protected LdapContext sysRoot;

  /**
   * the context root for the rootDSE
   */
  protected CoreSession rootDSE;

  /**
   * the context root for the schema
   */
  protected LdapContext schemaRoot;

  public LdapServer(final LdapServerConfiguration configuration) {
    this.configuration = checkNotNull(configuration);
    this.port = configuration.getPort();
  }

  public LdapServer start() {
    if (this.configuration.isDeleteOnStart()) {
      try {
        DirSupport.deleteIfExists(configuration.getWorkingDirectory().toPath());
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    this.configuration.getWorkingDirectory().mkdirs();
    if (this.port < 1) {
      try (ServerSocket socket = new ServerSocket(0)) {
        this.port = socket.getLocalPort();
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    // init stuff
    directoryService = new DefaultDirectoryService();
    directoryService.setWorkingDirectory(this.configuration.getWorkingDirectory());
    directoryService.setShutdownHookEnabled(false);
    ldapService = new org.apache.directory.server.ldap.LdapServer();
    TcpTransport tcp = new TcpTransport(port);
    tcp.enableSSL(this.configuration.isEnableSsl());
    ldapService.setTransports(tcp);
    ldapService.setDirectoryService(directoryService);

    maySetupSaslMechanisms();

    Set<JdbmPartition> partitions = Sets.newHashSet();
    for (org.sonatype.ldaptestsuite.Partition partition : this.configuration.getPartitions()) {
      try {
        // Add partition
        JdbmPartition jbdmPartition = new JdbmPartition();
        jbdmPartition.setId(partition.getName());
        jbdmPartition.setSuffix(partition.getSuffix());

        // Create indices
        if (partition.getIndexedAttributes() != null && partition.getIndexedAttributes().size() > 0) {
          Set<Index<?, ServerEntry>> indexedAttrs = new HashSet<Index<?, ServerEntry>>();
          for (String attr : partition.getIndexedAttributes()) {
            indexedAttrs.add(new JdbmIndex<String, ServerEntry>(attr));
          }
          jbdmPartition.setIndexedAttributes(indexedAttrs);
        }
        partitions.add(jbdmPartition);
        log.info("Added partition {} ({})", partition.getName(), partition.getSuffix());
      }
      catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
    // add all the partitions
    directoryService.setPartitions(partitions);

    try {
      directoryService.startup();
      mayAddAdditionalSchemas();

      // TODO shouldn't this be before calling configureLdapServer() ???
      ldapService.addExtendedOperationHandler(new StartTlsHandler());
      ldapService.addExtendedOperationHandler(new StoredProcedureExtendedOperationHandler());

      ldapService.start();
      setContexts(ServerDNConstants.ADMIN_SYSTEM_DN, "secret");

      // load needed ldifs
      for (org.sonatype.ldaptestsuite.Partition partition : this.configuration.getPartitions()) {
        if (partition.getLdifFile() != null) {
          log.info("Loading LDIF {} into partition {}", partition.getLdifFile().getAbsoluteFile(), partition.getName());
          try (FileInputStream ldifStream = new FileInputStream(partition.getLdifFile())) {
            for (LdifEntry ldifEntry : new LdifReader(ldifStream)) {
              rootDSE.add(new DefaultServerEntry(rootDSE.getDirectoryService().getRegistries(),
                  ldifEntry.getEntry()));
            }
          }
          catch (Exception e) {
            throw Throwables.propagate(e);
          }
        }
      }
    }
    catch (Exception e) {
      try {
        stop();
      }
      finally {
        throw Throwables.propagate(e);
      }
    }
    return this;
  }

  public LdapServer stop() {
    try {
      ldapService.stop();
      if (schemaRoot != null) {
        try {
          schemaRoot.close();
          schemaRoot = null;
        }
        catch (NamingException e) {
          log.warn("Could not close schema root", e);
        }
      }

      for (org.apache.directory.server.core.partition.Partition partition : directoryService.getPartitions()) {
        try {
          partition.destroy();
        }
        catch (Exception e) {
          log.warn("Could not close {}", partition.getId(), e);
        }
      }

      directoryService.getPartitions().clear();
    }
    finally {
      try {
        sysRoot = null;
        directoryService.shutdown();
      }
      catch (Exception e) {
        log.error("Failed to stop directoryService", e);
      }
    }
    return this;
  }

  public boolean isStarted() {
    return directoryService != null && directoryService.isStarted();
  }

  /**
   * Returns the actual port LDAP server is using.
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the configuration.
   */
  public LdapServerConfiguration getConfiguration() {
    return configuration;
  }

  // ==

  @VisibleForTesting
  protected DirectoryService getDirectoryService() {
    return directoryService;
  }

  // ==

  private void maySetupSaslMechanisms() {
    // only do this if sasl is configured
    if (!Strings.isNullOrEmpty(configuration.getSaslHost())) {
      ldapService.setSaslHost(configuration.getSaslHost());
      ldapService.setSaslPrincipal(configuration.getSaslPrincipal());
      ldapService.setSaslRealms(configuration.getSaslRealms());
      ldapService.setSearchBaseDn(configuration.getSaslSearchBaseDn());

      final Map<String, MechanismHandler> mechanismHandlerMap = Maps.newHashMap();
      mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN, new PlainMechanismHandler());
      mechanismHandlerMap.put(SupportedSaslMechanisms.CRAM_MD5, new CramMd5MechanismHandler());
      mechanismHandlerMap.put(SupportedSaslMechanisms.DIGEST_MD5, new DigestMd5MechanismHandler());
      mechanismHandlerMap.put(SupportedSaslMechanisms.GSSAPI, new GssapiMechanismHandler());

      NtlmMechanismHandler ntlmMechanismHandler = new NtlmMechanismHandler();
      // TODO - set some sort of default NtlmProvider implementation here
      // ntlmMechanismHandler.setNtlmProvider( provider );
      // TODO - or set FQCN of some sort of default NtlmProvider implementation here
      // ntlmMechanismHandler.setNtlmProviderFqcn( "com.foo.BarNtlmProvider" );
      mechanismHandlerMap.put(SupportedSaslMechanisms.NTLM, ntlmMechanismHandler);
      mechanismHandlerMap.put(SupportedSaslMechanisms.GSS_SPNEGO, ntlmMechanismHandler);

      ldapService.setSaslMechanismHandlers(mechanismHandlerMap);
    }
  }

  private void mayAddAdditionalSchemas()
      throws Exception
  {
    // apacheds has a test: org/apache/directory/server/core/operations/search/SearchWithIndicesITest.java
    // that does something like:
    // Attributes nisAttrs = schemaRoot.getAttributes( "cn=nis" );
    // boolean isNisDisabled = false;
    // if ( nisAttrs.get( "m-disabled" ) != null )
    // {
    // isNisDisabled = ( ( String ) nisAttrs.get( "m-disabled" ).get() ).equalsIgnoreCase( "TRUE" );
    // }
    //
    // // if nis is disabled then enable it
    // if ( isNisDisabled )
    // {
    // Attribute disabled = new BasicAttribute( "m-disabled" );
    // ModificationItem[] mods = new ModificationItem[] {
    // new ModificationItem( DirContext.REMOVE_ATTRIBUTE, disabled ) };
    // schemaRoot.modifyAttributes( "cn=nis", mods );
    // }
    // its still a bit of a hack, but way better then below. however this is not really class based, and relies on
    // the schema's
    // alreadying being in the ldap server, i don't know if thats an issue or not.

    // dirty hack
    Field field = DefaultRegistries.class.getDeclaredField("schemaLoader");
    field.setAccessible(true);
    PartitionSchemaLoader schemaLoader = (PartitionSchemaLoader) field
        .get(directoryService.getRegistries());

    for (String schemaClass : configuration.getAdditionalSchemas()) {
      log.debug("Adding schema class: {}", schemaClass);
      Schema schema = (Schema) Class.forName(schemaClass, true, getClass().getClassLoader()).newInstance();
      schemaLoader.load(schema, directoryService.getRegistries(), true);
      log.debug("Schema '{}' added to LDAP server", schema.getSchemaName());
    }
  }

  private void setContexts(final String user, final String passwd)
      throws Exception
  {
    final Hashtable<String, Object> env = new Hashtable<>();
    env.put(DirectoryService.JNDI_KEY, directoryService);
    env.put(Context.SECURITY_PRINCIPAL, user);
    env.put(Context.SECURITY_CREDENTIALS, passwd);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName());
    env.put(Context.PROVIDER_URL, ServerDNConstants.SYSTEM_DN);
    sysRoot = new InitialLdapContext(env, null);

    env.put(Context.PROVIDER_URL, "");
    rootDSE = directoryService.getAdminSession();

    env.put(Context.PROVIDER_URL, ServerDNConstants.OU_SCHEMA_DN);
    schemaRoot = new InitialLdapContext(env, null);
  }
}
