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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import junit.framework.AssertionFailedError;
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
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * The Class ServletServer. Heavily based on Joakim Erfeldt's work in wagon-webdav tests.
 *
 * @author cstamas
 */
public class LdapServer
    implements Startable, LogEnabled, Disposable
{

  /**
   * The Constant ROLE.
   */
  public static final String ROLE = LdapServer.class.getName();

  private static final List<LdifEntry> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<LdifEntry>(0));

  private static final String CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

  private Logger logger;

  /**
   * The working directory.
   */
  private File workingDirectory;

  private File temporayWorkDir;

  /**
   * The partitions.
   */
  protected List<org.sonatype.ldaptestsuite.Partition> partitions;

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

  /**
   * flag whether to delete database files for each test or not
   */
  protected boolean doDelete = true;

  protected int port = 1024;

  private String saslHost;

  private String saslPrincipal;

  private String searchBaseDn;

  private List<String> saslRealms;

  protected DirectoryService directoryService;

  protected org.apache.directory.server.ldap.LdapServer ldapService;

  protected List<String> additionalSchemas;

  private boolean ssl = false;

  public File getWorkingDirectory() {
    if (temporayWorkDir == null) {
      temporayWorkDir = FileUtils.createTempFile("ldap-", "-server", workingDirectory);
      temporayWorkDir.mkdirs();
    }
    return temporayWorkDir;
  }

  /**
   * If there is an LDIF file with the same name as the test class but with the .ldif extension then it is read and
   * the entries it contains are added to the server. It appears as though the administor adds these entries to the
   * server.
   *
   * @param verifyEntries whether or not all entry additions are checked to see if they were in fact correctly added
   *                      to the server
   * @return a list of entries added to the server in the order they were added
   * @throws NamingException of the load fails
   */
  protected List<LdifEntry> loadTestLdif(boolean verifyEntries)
      throws Exception
  {
    return loadLdif(getClass().getResourceAsStream(getClass().getSimpleName() + ".ldif"), verifyEntries);
  }

  /**
   * Loads an LDIF from an input stream and adds the entries it contains to the server. It appears as though the
   * administrator added these entries to the server.
   *
   * @param in            the input stream containing the LDIF entries to load
   * @param verifyEntries whether or not all entry additions are checked to see if they were in fact correctly added
   *                      to the server
   * @return a list of entries added to the server in the order they were added
   * @throws NamingException of the load fails
   */
  protected List<LdifEntry> loadLdif(InputStream in, boolean verifyEntries)
      throws Exception
  {
    if (in == null) {
      return EMPTY_LIST;
    }

    LdifReader ldifReader = new LdifReader(in);
    List<LdifEntry> entries = new ArrayList<LdifEntry>();

    for (LdifEntry entry : ldifReader) {
      rootDSE.add(new DefaultServerEntry(directoryService.getRegistries(), entry.getEntry()));

      if (verifyEntries) {
        verify(entry);
        logger.info("Successfully verified addition of entry: " + entry.getDn());
      }
      else {
        logger.info("Added entry: " + entry.getDn() + " without verification");
      }

      entries.add(entry);
    }

    return entries;
  }

  /**
   * Verifies that an entry exists in the directory with the specified attributes.
   *
   * @param entry the entry to verify
   * @throws NamingException if there are problems accessing the entry
   */
  protected void verify(LdifEntry entry)
      throws Exception
  {
    Entry readEntry = rootDSE.lookup(entry.getDn());

    for (EntryAttribute readAttribute : readEntry) {
      String id = readAttribute.getId();
      EntryAttribute origAttribute = entry.getEntry().get(id);

      for (Value<?> value : origAttribute) {
        if (!readAttribute.contains(value)) {
          logger.error("Failed to verify entry addition of " + entry.getDn() + ". " + id
              + " attribute in original " + "entry missing from read entry.");
          throw new AssertionFailedError("Failed to verify entry addition of " + entry.getDn());
        }
      }
    }
  }

  /**
   * Common code to get an initial context via a simple bind to the server over the wire using the SUN JNDI LDAP
   * provider. Do not use this method until after the setUp() method is called to start the server otherwise it will
   * fail.
   *
   * @return an LDAP context as the the administrator to the rootDSE
   * @throws NamingException if the server cannot be contacted
   */
  protected LdapContext getWiredContext()
      throws Exception
  {
    return getWiredContext(ServerDNConstants.ADMIN_SYSTEM_DN, "secret");
  }

  /**
   * Common code to get an initial context via a simple bind to the server over the wire using the SUN JNDI LDAP
   * provider. Do not use this method until after the setUp() method is called to start the server otherwise it will
   * fail.
   *
   * @param bindPrincipalDn the DN of the principal to bind as
   * @param password        the password of the bind principal
   * @return an LDAP context as the the administrator to the rootDSE
   * @throws NamingException if the server cannot be contacted
   */
  protected LdapContext getWiredContext(String bindPrincipalDn, String password)
      throws Exception
  {
    // if ( ! apacheDS.isStarted() )
    // {
    // throw new ConfigurationException( "The server is not online! Cannot connect to it." );
    // }

    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, CTX_FACTORY);
    env.put(Context.PROVIDER_URL, "ldap://localhost:" + port);
    env.put(Context.SECURITY_PRINCIPAL, bindPrincipalDn);
    env.put(Context.SECURITY_CREDENTIALS, password);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    return new InitialLdapContext(env, null);
  }

  private void setupSaslMechanisms(org.apache.directory.server.ldap.LdapServer server) {

    // only do this if sasl is configured in this bean
    if (StringUtils.isNotEmpty(this.saslHost)) {
      ldapService.setSaslHost(this.saslHost);
      ldapService.setSaslPrincipal(this.saslPrincipal);
      ldapService.setSaslRealms(this.saslRealms);
      ldapService.setSearchBaseDn(this.searchBaseDn);

      Map<String, MechanismHandler> mechanismHandlerMap = new HashMap<String, MechanismHandler>();

      mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN, new PlainMechanismHandler());

      CramMd5MechanismHandler cramMd5MechanismHandler = new CramMd5MechanismHandler();
      mechanismHandlerMap.put(SupportedSaslMechanisms.CRAM_MD5, cramMd5MechanismHandler);

      DigestMd5MechanismHandler digestMd5MechanismHandler = new DigestMd5MechanismHandler();
      mechanismHandlerMap.put(SupportedSaslMechanisms.DIGEST_MD5, digestMd5MechanismHandler);

      GssapiMechanismHandler gssapiMechanismHandler = new GssapiMechanismHandler();
      mechanismHandlerMap.put(SupportedSaslMechanisms.GSSAPI, gssapiMechanismHandler);

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

  protected void configureDirectoryService()
      throws Exception
  {
  }

  protected void configureLdapServer() {
  }

  protected void addAdditionalSchema()
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
    PartitionSchemaLoader schemaLoader =
        SchemaPartitionAccessor.getSchemaLoader((DefaultRegistries) directoryService.getRegistries());

    if (additionalSchemas != null && !additionalSchemas.isEmpty()) {
      for (String schemaClass : this.additionalSchemas) {
        this.logger.debug("Adding schema class: " + schemaClass);

        Schema schema = (Schema) Class.forName(schemaClass).newInstance();
        schemaLoader.load(schema, directoryService.getRegistries(), true);

        this.logger.debug("Schema '" + schema.getSchemaName() + " added to LDAP Server.");
      }
    }
  }

  /**
   * Deletes the Eve working directory.
   */
  protected void doDelete(File wkdir)
      throws IOException
  {
    if (doDelete) {
      if (wkdir.exists()) {
        FileUtils.deleteDirectory(wkdir);
      }

      if (wkdir.exists()) {
        throw new IOException("Failed to delete: " + wkdir);
      }
    }
  }

  /**
   * Sets the contexts for this base class. Values of user and password used to set the respective JNDI properties.
   * These values can be overriden by the overrides properties.
   *
   * @param user   the username for authenticating as this user
   * @param passwd the password of the user
   * @throws NamingException if there is a failure of any kind
   */
  protected void setContexts(String user, String passwd)
      throws Exception
  {
    Hashtable<String, Object> env = new Hashtable<String, Object>();
    env.put(DirectoryService.JNDI_KEY, directoryService);
    env.put(Context.SECURITY_PRINCIPAL, user);
    env.put(Context.SECURITY_CREDENTIALS, passwd);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName());
    setContexts(env);
  }

  /**
   * Sets the contexts of this class taking into account the extras and overrides properties.
   *
   * @param env an environment to use while setting up the system root.
   * @throws NamingException if there is a failure of any kind
   */
  protected void setContexts(Hashtable<String, Object> env)
      throws Exception
  {
    Hashtable<String, Object> envFinal = new Hashtable<String, Object>(env);
    envFinal.put(Context.PROVIDER_URL, ServerDNConstants.SYSTEM_DN);
    sysRoot = new InitialLdapContext(envFinal, null);

    envFinal.put(Context.PROVIDER_URL, "");
    rootDSE = directoryService.getAdminSession();

    envFinal.put(Context.PROVIDER_URL, ServerDNConstants.OU_SCHEMA_DN);
    schemaRoot = new InitialLdapContext(envFinal, null);
  }

  public void init()
      throws InitializationException
  {

    if (this.port == 0) {
      ServerSocket socket = null;
      try {
        socket = new ServerSocket(0);
        this.port = socket.getLocalPort();
      }
      catch (IOException e) {
        throw new InitializationException("Failed to find free port.", e);
      }
      finally {
        try {
          socket.close();
        }
        catch (IOException e) {
          throw new InitializationException("Failed to close port.", e);
        }
      }
    }

    directoryService = new DefaultDirectoryService();
    directoryService.setShutdownHookEnabled(false);
    ldapService = new org.apache.directory.server.ldap.LdapServer();
    TcpTransport tcp = new TcpTransport(this.port);
    tcp.enableSSL(ssl);
    ldapService.setTransports(tcp);
    ldapService.setDirectoryService(directoryService);

    setupSaslMechanisms(ldapService);

    if (getPartitions() != null) {
      Set<JdbmPartition> partitions = new HashSet<JdbmPartition>();
      for (org.sonatype.ldaptestsuite.Partition partition : getPartitions()) {
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

        }
        catch (Exception e) {
          throw new InitializationException("Unable to initialize partition " + partition.getName(), e);
        }
      }
      // add all the partitions
      this.directoryService.setPartitions(partitions);
    }

    // Create a working directory
    this.directoryService.setWorkingDirectory(getWorkingDirectory());
  }

  // ===
  // Startable iface

  /*
   * (non-Javadoc)
   * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#start()
   */
  public void start()
      throws StartingException
  {
    try {
      this.doDelete(this.workingDirectory);

      // reconfigure the server
      this.init();

      this.configureDirectoryService();

      directoryService.startup();

      this.configureLdapServer();

      this.addAdditionalSchema();

      // TODO shouldn't this be before calling configureLdapServer() ???
      ldapService.addExtendedOperationHandler(new StartTlsHandler());
      ldapService.addExtendedOperationHandler(new StoredProcedureExtendedOperationHandler());

      ldapService.start();
      this.setContexts(ServerDNConstants.ADMIN_SYSTEM_DN, "secret");

      // load needed ldifs
      for (org.sonatype.ldaptestsuite.Partition partition : getPartitions()) {
        if (partition.getLdifFile() != null) {
          FileInputStream ldifStream = null;
          try {
            ldifStream = new FileInputStream(partition.getLdifFile());
            this.importLdif(ldifStream);
          }
          finally {
            IOUtil.close(ldifStream);
          }
        }
      }

    }
    catch (Exception e) {
      try {
        this.stop();
      }
      catch (StoppingException e1) {
        this.logger.error(
            "Trying to stop the LDAP Server after a startup exception failed: " + e.getMessage(), e1);
      }

      throw new StartingException("Error starting embedded ApacheDS server.", e);
    }
  }

  public boolean isStarted() {
    return directoryService.isStarted();
  }

  /*
   * (non-Javadoc)
   * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable#stop()
   */
  public void dispose() {
    try {
      ldapService.stop();
      if (schemaRoot != null) {
        try {
          schemaRoot.close();
        }
        catch (NamingException e) {
          System.out.println("Failed to close schemaRoot");
        }
      }

      for (org.apache.directory.server.core.partition.Partition partition : this.directoryService.getPartitions()) {
        try {
          partition.destroy();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }

      this.directoryService.getPartitions().clear();
    }
    finally {
      try {
        sysRoot = null;
        directoryService.shutdown();
      }
      catch (Exception e) {
        System.out.println("Failed to stop directoryService");
      }
    }
  }

  public void stop()
      throws StoppingException
  {
    try {
      dispose();
    }
    catch (Exception e) {
      throw new StoppingException("Error stopping embedded ApacheDS server.", e);
    }
  }

  public void enableLogging(Logger logger) {
    this.logger = logger;
  }

  /**
   * Imports the LDIF entries packaged with the Eve JNDI provider jar into the newly created system partition to
   * prime
   * it up for operation. Note that only ou=system entries will be added - entries for other partitions cannot be
   * imported and will blow chunks.
   *
   * @param in the input stream with the ldif
   * @throws NamingException if there are problems reading the ldif file and adding those entries to the system
   *                         partition
   */
  protected void importLdif(InputStream in)
      throws NamingException
  {
    try {
      for (LdifEntry ldifEntry : new LdifReader(in)) {
        rootDSE.add(new DefaultServerEntry(rootDSE.getDirectoryService().getRegistries(),
            ldifEntry.getEntry()));
      }
    }
    catch (Exception e) {
      String msg = "failed while trying to parse system ldif file";
      NamingException ne = new LdapConfigurationException(msg, e);
      throw ne;
    }
  }

  /**
   * Inject an ldif String into the server. DN must be relative to the root.
   *
   * @param ldif the entries to inject
   * @throws NamingException if the entries cannot be added
   */
  protected void injectEntries(String ldif)
      throws Exception
  {
    LdifReader reader = new LdifReader();
    List<LdifEntry> entries = reader.parseLdif(ldif);

    for (LdifEntry entry : entries) {
      rootDSE.add(new DefaultServerEntry(rootDSE.getDirectoryService().getRegistries(), entry.getEntry()));
    }
  }

  public List<org.sonatype.ldaptestsuite.Partition> getPartitions() {
    return partitions;
  }

  public void setPartitions(List<org.sonatype.ldaptestsuite.Partition> partitions) {
    this.partitions = partitions;
  }

  private void printSchemas() {
    if (directoryService != null) {

      Map<String, Schema> schemas = directoryService.getRegistries().getLoadedSchemas();

      for (java.util.Map.Entry<String, Schema> entry : schemas.entrySet()) {
        this.logger.error("entry: " + entry.getValue().getSchemaName());
      }
    }
  }

  public int getPort() {
    return this.port;
  }
}
