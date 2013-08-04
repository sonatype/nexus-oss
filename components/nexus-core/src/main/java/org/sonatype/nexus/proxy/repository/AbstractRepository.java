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

package org.sonatype.nexus.proxy.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryEventExpireNotFoundCaches;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryEventRecreateAttributes;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDeleteRoot;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreCreate;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStoreUpdate;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageNotFoundItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.ReadLockingContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.RepositoryItemUidAttributeManager;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.DefaultLocalStorageContext;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.targets.TargetRegistry;
import org.sonatype.nexus.proxy.targets.TargetSet;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.proxy.walker.WalkerFilter;
import org.sonatype.nexus.scheduling.DefaultRepositoryTaskActivityDescriptor;
import org.sonatype.nexus.scheduling.DefaultRepositoryTaskFilter;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

import com.google.common.collect.Maps;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

/**
 * <p>
 * A common base for Proximity repository. It defines all the needed properties and main methods as in
 * ProximityRepository interface.
 * <p>
 * This abstract class handles the following functionalities:
 * <ul>
 * <li>Holds base properties like repo ID, group ID, rank</li>
 * <li>Manages AccessManager</li>
 * <li>Manages notFoundCache to speed up responses</li>
 * <li>Manages event listeners</li>
 * </ul>
 * <p>
 * The subclasses only needs to implement the abstract method focusing on item retrieaval and other "basic" functions.
 *
 * @author cstamas
 */
public abstract class AbstractRepository
    extends ConfigurableRepository
    implements Repository
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  @Requirement
  private CacheManager cacheManager;

  @Requirement
  private TargetRegistry targetRegistry;

  @Requirement
  private RepositoryItemUidFactory repositoryItemUidFactory;

  @Requirement
  private RepositoryItemUidAttributeManager repositoryItemUidAttributeManager;

  @Requirement
  private AccessManager accessManager;

  @Requirement
  private Walker walker;

  @Requirement
  private MimeSupport mimeSupport;

  @Requirement(role = ContentGenerator.class)
  private Map<String, ContentGenerator> contentGenerators;

  @Requirement
  private AttributesHandler attributesHandler;

  /**
   * Local storage context to store storage-wide configs.
   */
  private LocalStorageContext localStorageContext;

  /**
   * The local storage.
   */
  private LocalRepositoryStorage localStorage;

  /**
   * The not found cache.
   */
  private PathCache notFoundCache;

  /**
   * Request strategies map. Supersedes RequestProcessors.
   *
   * @since 2.5
   */
  private final Map<String, RequestStrategy> requestStrategies = Maps.newHashMap();

  /**
   * if local url changed, need special handling after save
   */
  private boolean localUrlChanged = false;

  /**
   * if non-indexable -> indexable change occured, need special handling after save
   */
  private boolean madeSearchable = false;

  /**
   * if local status changed, need special handling after save
   */
  private boolean localStatusChanged = false;

  // --

  protected Logger getLogger() {
    return logger;
  }

  protected MimeSupport getMimeSupport() {
    return mimeSupport;
  }

  @Override
  public MimeRulesSource getMimeRulesSource() {
    return MimeRulesSource.NOOP;
  }

  // ==

  @Override
  protected abstract Configurator getConfigurator();

  @Override
  protected abstract CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory();

  @Override
  public boolean commitChanges()
      throws ConfigurationException
  {
    boolean wasDirty = super.commitChanges();

    if (wasDirty) {
      eventBus().post(getRepositoryConfigurationUpdatedEvent());
    }

    this.localUrlChanged = false;

    this.madeSearchable = false;

    this.localStatusChanged = false;

    return wasDirty;
  }

  @Override
  public boolean rollbackChanges() {
    this.localUrlChanged = false;

    this.madeSearchable = false;

    this.localStatusChanged = false;

    return super.rollbackChanges();
  }

  @Override
  protected ApplicationConfiguration getApplicationConfiguration() {
    return applicationConfiguration;
  }

  protected RepositoryConfigurationUpdatedEvent getRepositoryConfigurationUpdatedEvent() {
    RepositoryConfigurationUpdatedEvent event = new RepositoryConfigurationUpdatedEvent(this);

    event.setLocalUrlChanged(this.localUrlChanged);
    event.setMadeSearchable(this.madeSearchable);
    event.setLocalStatusChanged(localStatusChanged);

    return event;
  }

  protected AbstractRepositoryConfiguration getExternalConfiguration(boolean forModification) {
    final CoreConfiguration cc = getCurrentCoreConfiguration();
    if (cc != null) {
      ExternalConfiguration<?> ec = cc.getExternalConfiguration();
      if (ec != null) {
        return (AbstractRepositoryConfiguration) ec.getConfiguration(forModification);
      }
    }
    return null;
  }

  // ==

  public RepositoryTaskFilter getRepositoryTaskFilter() {
    // we are allowing all, and subclasses will filter as they want
    return new DefaultRepositoryTaskFilter().setAllowsRepositoryScanning(true).setAllowsScheduledTasks(true)
        .setAllowsUserInitiatedTasks(
            true).setContentOperators(DefaultRepositoryTaskActivityDescriptor.ALL_CONTENT_OPERATIONS)
        .setAttributeOperators(
            DefaultRepositoryTaskActivityDescriptor.ALL_ATTRIBUTES_OPERATIONS);
  }

  @Override
  public RequestStrategy registerRequestStrategy(final String key, final RequestStrategy strategy) {
    checkNotNull(key);
    checkNotNull(strategy);
    synchronized (requestStrategies) {
      return requestStrategies.put(key, strategy);
    }
  }

  @Override
  public RequestStrategy unregisterRequestStrategy(final String key) {
    checkNotNull(key);
    synchronized (requestStrategies) {
      return requestStrategies.remove(key);
    }
  }

  @Override
  public Map<String, RequestStrategy> getRegisteredStrategies() {
    synchronized (requestStrategies) {
      return Maps.newHashMap(requestStrategies);
    }
  }

  /**
   * Gets the cache manager.
   *
   * @return the cache manager
   */
  protected CacheManager getCacheManager() {
    return cacheManager;
  }

  /**
   * Sets the cache manager.
   *
   * @param cacheManager the new cache manager
   */
  protected void setCacheManager(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Returns the repository Item Uid Factory.
   */
  protected RepositoryItemUidFactory getRepositoryItemUidFactory() {
    return repositoryItemUidFactory;
  }

  /**
   * Gets the not found cache.
   *
   * @return the not found cache
   */
  public PathCache getNotFoundCache() {
    if (notFoundCache == null) {
      // getting it lazily
      notFoundCache = getCacheManager().getPathCache(getId());
    }

    return notFoundCache;
  }

  /**
   * Sets the not found cache.
   *
   * @param notFoundcache the new not found cache
   */
  public void setNotFoundCache(PathCache notFoundcache) {
    this.notFoundCache = notFoundcache;
  }

  @Override
  public void setIndexable(boolean indexable) {
    if (!isIndexable() && indexable) {
      // we have a non-indexable -> indexable transition
      madeSearchable = true;
    }

    super.setIndexable(indexable);
  }

  @Override
  public void setLocalUrl(String localUrl)
      throws StorageException
  {
    String newLocalUrl = null;

    if (localUrl != null) {
      newLocalUrl = localUrl.trim();
    }

    if (newLocalUrl != null) {
      if (newLocalUrl.endsWith(RepositoryItemUid.PATH_SEPARATOR)) {
        newLocalUrl = newLocalUrl.substring(0, newLocalUrl.length() - 1);
      }

      getLocalStorage().validateStorageUrl(newLocalUrl);
    }

    // Dont use getLocalUrl since that applies default
    if (getCurrentConfiguration(false).getLocalStorage() != null
        && !StringUtils.equals(newLocalUrl, getCurrentConfiguration(false).getLocalStorage().getUrl())) {
      this.localUrlChanged = true;
    }

    super.setLocalUrl(localUrl);
  }

  @Override
  public void setLocalStatus(LocalStatus localStatus) {
    if (!localStatus.equals(getLocalStatus())) {
      LocalStatus oldLocalStatus = getLocalStatus();

      super.setLocalStatus(localStatus);

      localStatusChanged = true;

      eventBus().post(new RepositoryEventLocalStatusChanged(this, oldLocalStatus, localStatus));
    }
  }

  @SuppressWarnings("unchecked")
  public <F> F adaptToFacet(Class<F> t) {
    if (getRepositoryKind().isFacetAvailable(t)) {
      return (F) this;
    }
    else {
      return null;
    }
  }

  protected Walker getWalker() {
    return walker;
  }

  protected Map<String, ContentGenerator> getContentGenerators() {
    return contentGenerators;
  }

  // ===================================================================================
  // Repository iface

  public AccessManager getAccessManager() {
    return accessManager;
  }

  public void setAccessManager(AccessManager accessManager) {
    this.accessManager = accessManager;
  }

  @Override
  public void expireCaches(final ResourceStoreRequest request) {
    expireCaches(request, null);
  }

  @Override
  public boolean expireCaches(final ResourceStoreRequest request, final WalkerFilter filter) {
    if (!getLocalStatus().shouldServiceRequest()) {
      return false;
    }

    // at this level (we are not proxy) expireCaches() actually boils down to "expire NFC" only
    // we are NOT crawling local storage content to flip the isExpired flags to true on a hosted
    // repo, since those attributes in case of hosted (or any other non-proxy) repositories does not have any
    // meaning

    // 2nd, remove the items from NFC
    return expireNotFoundCaches(request, filter);
  }

  @Override
  public void expireNotFoundCaches(final ResourceStoreRequest request) {
    expireNotFoundCaches(request, null);
  }

  @Override
  public boolean expireNotFoundCaches(final ResourceStoreRequest request, final WalkerFilter filter) {
    if (!getLocalStatus().shouldServiceRequest()) {
      return false;
    }

    if (StringUtils.isBlank(request.getRequestPath())) {
      request.setRequestPath(RepositoryItemUid.PATH_ROOT);
    }

    getLogger().debug(
        String.format("Clearing NFC cache in repository %s from path=\"%s\"",
            RepositoryStringUtils.getHumanizedNameString(this), request.getRequestPath()));

    boolean cacheAltered = false;
    // remove the items from NFC
    if (filter == null) {
      if (RepositoryItemUid.PATH_ROOT.equals(request.getRequestPath())) {
        // purge all
        if (getNotFoundCache() != null) {
          cacheAltered = getNotFoundCache().purge();
        }
      }
      else {
        // purge below and above path only
        if (getNotFoundCache() != null) {
          boolean altered1 = getNotFoundCache().removeWithParents(request.getRequestPath());
          boolean altered2 = getNotFoundCache().removeWithChildren(request.getRequestPath());
          cacheAltered = altered1 || altered2;
        }
      }
    }
    else {
      final ParentOMatic parentOMatic = new ParentOMatic(false);
      final DefaultWalkerContext context = new DefaultWalkerContext(this, request);
      final Collection<String> nfcPaths = getNotFoundCache().listKeysInCache();

      for (String nfcPath : nfcPaths) {
        final DefaultStorageNotFoundItem nfcItem =
            new DefaultStorageNotFoundItem(this, new ResourceStoreRequest(nfcPath));

        if (filter.shouldProcess(context, nfcItem)) {
          parentOMatic.addAndMarkPath(nfcPath);
        }
      }

      for (String path : parentOMatic.getMarkedPaths()) {
        boolean removed = getNotFoundCache().remove(path);
        cacheAltered = cacheAltered || removed;
      }
    }

    if (getLogger().isDebugEnabled()) {
      if (cacheAltered) {
        getLogger().info(
            String.format("NFC for repository %s from path=\"%s\" was cleared.",
                RepositoryStringUtils.getHumanizedNameString(this), request.getRequestPath()));
      }
      else {
        getLogger().debug(
            String.format("Clear NFC for repository %s from path=\"%s\" did not alter cache.",
                RepositoryStringUtils.getHumanizedNameString(this), request.getRequestPath()));
      }
    }

    eventBus().post(
        new RepositoryEventExpireNotFoundCaches(this, request.getRequestPath(),
            request.getRequestContext().flatten(), cacheAltered));

    return cacheAltered;
  }

  @Override
  public RepositoryMetadataManager getRepositoryMetadataManager() {
    return new NoopRepositoryMetadataManager();
  }

  public Collection<String> evictUnusedItems(ResourceStoreRequest request, final long timestamp) {
    // this is noop at hosted level
    return Collections.emptyList();
  }

  public boolean recreateAttributes(ResourceStoreRequest request, final Map<String, String> initialData) {
    if (!getLocalStatus().shouldServiceRequest()) {
      return false;
    }

    if (StringUtils.isEmpty(request.getRequestPath())) {
      request.setRequestPath(RepositoryItemUid.PATH_ROOT);
    }

    getLogger().info(
        String.format("Rebuilding item attributes in repository %s from path=\"%s\"",
            RepositoryStringUtils.getHumanizedNameString(this), request.getRequestPath()));

    RecreateAttributesWalker walkerProcessor = new RecreateAttributesWalker(this, initialData);

    DefaultWalkerContext ctx = new DefaultWalkerContext(this, request);

    ctx.getProcessors().add(walkerProcessor);

    // let it loose
    try {
      getWalker().walk(ctx);
    }
    catch (WalkerException e) {
      if (!(e.getWalkerContext().getStopCause() instanceof ItemNotFoundException)) {
        // everything that is not ItemNotFound should be reported,
        // otherwise just neglect it
        throw e;
      }
    }

    eventBus().post(new RepositoryEventRecreateAttributes(this));

    return true;
  }

  public AttributesHandler getAttributesHandler() {
    return attributesHandler;
  }

  public void setAttributesHandler(AttributesHandler attributesHandler) {
    this.attributesHandler = attributesHandler;
  }

  public LocalStorageContext getLocalStorageContext() {
    if (localStorageContext == null) {
      localStorageContext =
          new DefaultLocalStorageContext(getApplicationConfiguration().getGlobalLocalStorageContext());
    }

    return localStorageContext;
  }

  public LocalRepositoryStorage getLocalStorage() {
    return localStorage;
  }

  public void setLocalStorage(LocalRepositoryStorage localStorage) {
    getCurrentConfiguration(true).getLocalStorage().setProvider(localStorage.getProviderId());

    this.localStorage = localStorage;
  }

  // ===================================================================================
  // Store iface

  public StorageItem retrieveItem(ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
  {
    checkConditions(request, Action.read);

    StorageItem item = retrieveItem(false, request);

    if (StorageCollectionItem.class.isAssignableFrom(item.getClass()) && !isBrowseable()) {
      getLogger().debug(
          getId() + " retrieveItem() :: FOUND a collection on " + request.toString()
              + " but repository is not Browseable.");

      throw new ItemNotFoundException(reasonFor(request, this, "Repository %s is not browsable",
          this));
    }

    checkPostConditions(request, item);

    return item;
  }

  public void copyItem(ResourceStoreRequest from, ResourceStoreRequest to)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
             StorageException, AccessDeniedException
  {
    checkConditions(from, Action.read);
    checkConditions(to, getResultingActionOnWrite(to));

    copyItem(false, from, to);
  }

  public void moveItem(ResourceStoreRequest from, ResourceStoreRequest to)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
             StorageException, AccessDeniedException
  {
    checkConditions(from, Action.read);
    checkConditions(from, Action.delete);
    checkConditions(to, getResultingActionOnWrite(to));

    moveItem(false, from, to);
  }

  public void deleteItem(ResourceStoreRequest request)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
             StorageException, AccessDeniedException
  {
    checkConditions(request, Action.delete);

    deleteItem(false, request);
  }

  public void storeItem(ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes)
      throws UnsupportedStorageOperationException, IllegalOperationException, StorageException, AccessDeniedException
  {
    try {
      checkConditions(request, getResultingActionOnWrite(request));
    }
    catch (ItemNotFoundException e) {
      throw new AccessDeniedException(request, e.getMessage());
    }

    DefaultStorageFileItem fItem =
        new DefaultStorageFileItem(this, request, true, true, new PreparedContentLocator(is,
            getMimeSupport().guessMimeTypeFromPath(getMimeRulesSource(), request.getRequestPath())));

    if (userAttributes != null) {
      fItem.getRepositoryItemAttributes().putAll(userAttributes);
    }

    storeItem(false, fItem);
  }

  public void createCollection(ResourceStoreRequest request, Map<String, String> userAttributes)
      throws UnsupportedStorageOperationException, IllegalOperationException, StorageException, AccessDeniedException
  {
    try {
      checkConditions(request, getResultingActionOnWrite(request));
    }
    catch (ItemNotFoundException e) {
      throw new AccessDeniedException(request, e.getMessage());
    }

    DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(this, request, true, true);

    if (userAttributes != null) {
      coll.getRepositoryItemAttributes().putAll(userAttributes);
    }

    storeItem(false, coll);
  }

  public Collection<StorageItem> list(ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
  {
    checkConditions(request, Action.read);

    Collection<StorageItem> items = null;

    if (isBrowseable()) {
      items = list(false, request);
    }
    else {
      throw new ItemNotFoundException(reasonFor(request, this, "Repository %s is not browsable", this));
    }

    return items;
  }

  public TargetSet getTargetsForRequest(ResourceStoreRequest request) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("getTargetsForRequest() :: " + this.getId() + ":" + request.getRequestPath());
    }

    return targetRegistry.getTargetsForRepositoryPath(this, request.getRequestPath());
  }

  public boolean hasAnyTargetsForRequest(ResourceStoreRequest request) {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("hasAnyTargetsForRequest() :: " + this.getId());
    }

    return targetRegistry.hasAnyApplicableTarget(this);
  }

  public Action getResultingActionOnWrite(final ResourceStoreRequest rsr)
      throws LocalStorageException
  {
    final boolean isInLocalStorage = getLocalStorage().containsItem(this, rsr);

    if (isInLocalStorage) {
      return Action.update;
    }
    else {
      return Action.create;
    }
  }

  // ===================================================================================
  // Repositry store-like

  public StorageItem retrieveItem(boolean fromTask, ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug(getId() + ".retrieveItem() :: " + request.toString());
    }

    if (!getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    request.addProcessedRepository(this);

    maintainNotFoundCache(request);

    final RepositoryItemUid uid = createUid(request.getRequestPath());

    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.read);

    try {
      StorageItem item = doRetrieveItem(request);

      // file with generated content?
      if (item instanceof StorageFileItem && ((StorageFileItem) item).isContentGenerated()) {
        StorageFileItem file = (StorageFileItem) item;

        String key = file.getContentGeneratorId();

        if (getContentGenerators().containsKey(key)) {
          ContentGenerator generator = getContentGenerators().get(key);

          try {
            file.setContentLocator(generator.generateContent(this, uid.getPath(), file));
          }
          catch (Exception e) {
            throw new LocalStorageException("Could not generate content:", e);
          }
        }
        else {
          getLogger().info(
              String.format(
                  "The file in repository %s on path=\"%s\" should be generated by ContentGeneratorId=%s, but component does not exists!",
                  RepositoryStringUtils.getHumanizedNameString(this), uid.getPath(), key));

          throw new ItemNotFoundException(reasonFor(request, this,
              "The generator for generated path %s with key %s not found in %s", request.getRequestPath(),
              key, this));
        }
      }

      eventBus().post(new RepositoryItemEventRetrieve(this, item));

      if (getLogger().isDebugEnabled()) {
        getLogger().debug(getId() + " retrieveItem() :: FOUND " + uid.toString());
      }

      return item;
    }
    catch (ItemNotFoundException ex) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug(getId() + " retrieveItem() :: NOT FOUND " + uid.toString());
      }

      if (shouldAddToNotFoundCache(request)) {
        addToNotFoundCache(request);
      }

      throw ex;
    }
    finally {
      uidLock.unlock();
    }
  }

  public void copyItem(boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug(getId() + ".copyItem() :: " + from.toString() + " --> " + to.toString());
    }

    if (!getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    maintainNotFoundCache(from);

    final RepositoryItemUid fromUid = createUid(from.getRequestPath());

    final RepositoryItemUid toUid = createUid(to.getRequestPath());

    final RepositoryItemUidLock fromUidLock = fromUid.getLock();

    final RepositoryItemUidLock toUidLock = toUid.getLock();

    fromUidLock.lock(Action.read);
    toUidLock.lock(Action.create);

    try {
      StorageItem item = retrieveItem(fromTask, from);

      if (StorageFileItem.class.isAssignableFrom(item.getClass())) {
        try {
          DefaultStorageFileItem target =
              new DefaultStorageFileItem(this, to, true, true, new PreparedContentLocator(
                  ((StorageFileItem) item).getInputStream(), ((StorageFileItem) item).getMimeType()));

          target.getItemContext().putAll(item.getItemContext());

          storeItem(fromTask, target);

          // remove the "to" item from n-cache if there
          removeFromNotFoundCache(to);
        }
        catch (IOException e) {
          throw new LocalStorageException("Could not get the content of source file (is it file?)!", e);
        }
      }
    }
    finally {
      toUidLock.unlock();

      fromUidLock.unlock();
    }
  }

  public void moveItem(boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug(getId() + ".moveItem() :: " + from.toString() + " --> " + to.toString());
    }

    if (!getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    copyItem(fromTask, from, to);

    deleteItem(fromTask, from);
  }

  public void deleteItem(boolean fromTask, ResourceStoreRequest request)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug(getId() + ".deleteItem() :: " + request.toString());
    }

    if (!getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    maintainNotFoundCache(request);

    final RepositoryItemUid uid = createUid(request.getRequestPath());

    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.delete);

    try {
      StorageItem item = null;
      try {
        // determine is the thing to be deleted a collection or not
        item = getLocalStorage().retrieveItem(this, request);
      }
      catch (ItemNotFoundException ex) {
        if (shouldNeglectItemNotFoundExOnDelete(request, ex)) {
          item = null;
        }
        else {
          throw ex;
        }
      }

      if (item != null) {
        // fire the event for file being deleted
        eventBus().post(new RepositoryItemEventDeleteRoot(this, item));

        // if we are deleting a collection, perform recursive notification about this too
        if (item instanceof StorageCollectionItem) {
          if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                "We are deleting a collection, starting a walker to send delete notifications per-file.");
          }

          // it is collection, walk it and below and fire events for all files
          DeletionNotifierWalker dnw = new DeletionNotifierWalker(eventBus(), request);

          DefaultWalkerContext ctx = new DefaultWalkerContext(this, request);

          ctx.getProcessors().add(dnw);

          try {
            getWalker().walk(ctx);
          }
          catch (WalkerException e) {
            if (!(e.getWalkerContext().getStopCause() instanceof ItemNotFoundException)) {
              // everything that is not ItemNotFound should be reported,
              // otherwise just neglect it
              throw e;
            }
          }
        }

        doDeleteItem(request);
      }
    }
    finally {
      uidLock.unlock();
    }
  }

  /**
   * Decides should a {@link ItemNotFoundException} be neglected on
   * {@link #deleteItem(boolean, org.sonatype.nexus.proxy.ResourceStoreRequest)} method invocation or not. Nexus
   * was always throwing this exception when deletion of non existent item was tried, but since 2.4 in Maven support
   * the Maven checksum files are not existing anymore as standalone items (in local storage), hence default
   * implementation of this method simply returns {@code false} to retain this behaviour, but still, to make
   * Repository implementations able to override this.
   *
   * @since 2.6
   */
  protected boolean shouldNeglectItemNotFoundExOnDelete(ResourceStoreRequest request, ItemNotFoundException ex) {
    return false;
  }

  public void storeItem(boolean fromTask, StorageItem item)
      throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug(getId() + ".storeItem() :: " + item.getRepositoryItemUid().toString());
    }

    if (!getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    final RepositoryItemUid uid = createUid(item.getPath());

    // replace UID to own one
    item.setRepositoryItemUid(uid);

    // NEXUS-4550: This "fake" UID/lock here is introduced only to serialize uploaders
    // This will catch immediately an uploader if an upload already happens
    // and prevent deadlocks, since uploader still does not have
    // shared lock
    final RepositoryItemUid uploaderUid = createUid(item.getPath() + ".storeItem()");

    final RepositoryItemUidLock uidUploaderLock = uploaderUid.getLock();

    uidUploaderLock.lock(Action.create);

    final Action action = getResultingActionOnWrite(item.getResourceStoreRequest());

    try {
      // NEXUS-4550: we are shared-locking the actual UID (to not prevent downloaders while
      // we save to temporary location. But this depends on actual LS backend actually...)
      // but we exclusive lock uploaders to serialize them!
      // And the LS has to take care of whatever stricter locking it has to use or not
      // Think: RDBMS LS or some trickier LS implementations for example
      final RepositoryItemUidLock uidLock = uid.getLock();

      uidLock.lock(Action.read);

      try {
        // store it
        getLocalStorage().storeItem(this, item);
      }
      finally {
        uidLock.unlock();
      }
    }
    finally {
      uidUploaderLock.unlock();
    }

    // remove the "request" item from n-cache if there
    removeFromNotFoundCache(item.getResourceStoreRequest());

    if (Action.create.equals(action)) {
      eventBus().post(new RepositoryItemEventStoreCreate(this, item));
    }
    else {
      eventBus().post(new RepositoryItemEventStoreUpdate(this, item));
    }
  }

  public Collection<StorageItem> list(boolean fromTask, ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug(getId() + ".list() :: " + request.toString());
    }

    if (!getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    request.addProcessedRepository(this);

    StorageItem item = retrieveItem(fromTask, request);

    if (item instanceof StorageCollectionItem) {
      return list(fromTask, (StorageCollectionItem) item);
    }
    else {
      throw new ItemNotFoundException(reasonFor(request, this, "Path %s in repository %s is not a collection",
          request.getRequestPath(), this));
    }
  }

  public Collection<StorageItem> list(boolean fromTask, StorageCollectionItem coll)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug(getId() + ".list() :: " + coll.getRepositoryItemUid().toString());
    }

    if (!getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    maintainNotFoundCache(coll.getResourceStoreRequest());

    Collection<StorageItem> items = doListItems(new ResourceStoreRequest(coll));

    for (StorageItem item : items) {
      item.getItemContext().putAll(coll.getItemContext());
    }

    return items;
  }

  @Override
  public RepositoryItemUid createUid(final String path) {
    return getRepositoryItemUidFactory().createUid(this, path);
  }

  public RepositoryItemUidAttributeManager getRepositoryItemUidAttributeManager() {
    return repositoryItemUidAttributeManager;
  }

  // ===================================================================================
  // Inner stuff

  /**
   * Maintains not found cache.
   *
   * @throws ItemNotFoundException the item not found exception
   */
  public void maintainNotFoundCache(ResourceStoreRequest request)
      throws ItemNotFoundException
  {
    if (isNotFoundCacheActive()) {
      if (getNotFoundCache().contains(request.getRequestPath())) {
        if (getNotFoundCache().isExpired(request.getRequestPath())) {
          if (getLogger().isDebugEnabled()) {
            getLogger().debug("The path " + request.getRequestPath() + " is in NFC but expired.");
          }

          removeFromNotFoundCache(request);
        }
        else {
          if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                "The path " + request.getRequestPath()
                    + " is in NFC and still active, throwing ItemNotFoundException.");
          }

          throw new ItemNotFoundException(reasonFor(request, this,
              "The path %s is still cached as not found for repository %s", request.getRequestPath(), this));
        }
      }
    }
  }

  @Deprecated
  public void addToNotFoundCache(String path) {
    addToNotFoundCache(new ResourceStoreRequest(path));
  }

  @Deprecated
  public void removeFromNotFoundCache(String path) {
    removeFromNotFoundCache(new ResourceStoreRequest(path));
  }

  /**
   * Adds the uid to not found cache.
   */
  @Override
  public void addToNotFoundCache(ResourceStoreRequest request) {
    if (isNotFoundCacheActive()) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Adding path " + request.getRequestPath() + " to NFC.");
      }

      getNotFoundCache().put(request.getRequestPath(), Boolean.TRUE, getNotFoundCacheTimeToLive() * 60);
    }
  }

  /**
   * Removes the uid from not found cache.
   */
  public void removeFromNotFoundCache(ResourceStoreRequest request) {
    if (isNotFoundCacheActive()) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Removing path " + request.getRequestPath() + " from NFC.");
      }

      getNotFoundCache().removeWithParents(request.getRequestPath());
    }
  }

  /**
   * Check conditions, such as availability, permissions, etc.
   *
   * @param request the request
   * @throws RepositoryNotAvailableException
   *                               the repository not available exception
   * @throws AccessDeniedException the access denied exception
   */
  protected void checkConditions(ResourceStoreRequest request, Action action)
      throws ItemNotFoundException, IllegalOperationException, AccessDeniedException
  {
    if (!this.getLocalStatus().shouldServiceRequest()) {
      throw new RepositoryNotAvailableException(this);
    }

    // check for writing to read only repo
    // Readonly is ALWAYS read only
    if (RepositoryWritePolicy.READ_ONLY.equals(getWritePolicy()) && !isActionAllowedReadOnly(action)) {
      throw new IllegalRequestException(request, "Repository with ID='" + getId()
          + "' is Read Only, but action was '" + action.toString() + "'!");
    }
    // but Write/write once may need to allow updating metadata
    // check the write policy
    enforceWritePolicy(request, action);

    // NXCM-3600: this if was an old remnant, is not needed
    // if ( isExposed() )
    // {
    getAccessManager().decide(this, request, action);
    // }

    checkRequestStrategies(request, action);
  }

  protected void checkRequestStrategies(final ResourceStoreRequest request, final Action action)
      throws ItemNotFoundException, IllegalOperationException
  {
    final Map<String, RequestStrategy> effectiveRequestStrategies = getRegisteredStrategies();
    for (RequestStrategy strategy : effectiveRequestStrategies.values()) {
      strategy.onHandle(this, request, action);
    }
  }

  protected void checkPostConditions(final ResourceStoreRequest request, final StorageItem item)
      throws IllegalOperationException, ItemNotFoundException
  {
    final Map<String, RequestStrategy> effectiveRequestStrategies = getRegisteredStrategies();
    for (RequestStrategy strategy : effectiveRequestStrategies.values()) {
      strategy.onServing(this, request, item);
    }
  }

  protected void enforceWritePolicy(ResourceStoreRequest request, Action action)
      throws IllegalRequestException
  {
    // check for write once (no redeploy)
    if (Action.update.equals(action) && !RepositoryWritePolicy.ALLOW_WRITE.equals(this.getWritePolicy())) {
      throw new IllegalRequestException(request, "Repository with ID='" + getId()
          + "' does not allow updating artifacts.");
    }
  }

  public boolean isCompatible(Repository repository) {
    return getRepositoryContentClass().isCompatible(repository.getRepositoryContentClass());
  }

  protected void doDeleteItem(ResourceStoreRequest request)
      throws UnsupportedStorageOperationException, ItemNotFoundException, StorageException
  {
    getLocalStorage().deleteItem(this, request);
  }

  protected Collection<StorageItem> doListItems(ResourceStoreRequest request)
      throws ItemNotFoundException, StorageException
  {
    return getLocalStorage().listItems(this, request);
  }

  protected StorageItem doRetrieveItem(ResourceStoreRequest request)
      throws IllegalOperationException, ItemNotFoundException, StorageException
  {
    AbstractStorageItem localItem = null;

    try {
      localItem = getLocalStorage().retrieveItem(this, request);

      // plain file? wrap it
      if (localItem instanceof StorageFileItem) {
        StorageFileItem file = (StorageFileItem) localItem;

        // wrap the content locator if needed
        if (!(file.getContentLocator() instanceof ReadLockingContentLocator)) {
          final RepositoryItemUid uid = createUid(request.getRequestPath());
          file.setContentLocator(new ReadLockingContentLocator(uid, file.getContentLocator()));
        }
      }

      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Item " + request.toString() + " found in local storage.");
      }
    }
    catch (ItemNotFoundException ex) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Item " + request.toString() + " not found in local storage.");
      }

      throw ex;
    }

    return localItem;
  }

  protected boolean isActionAllowedReadOnly(Action action) {
    return action.isReadAction();
  }

  /**
   * Whether or not the requested path should be added to NFC. Item will be added to NFC if is not local/remote only.
   *
   * @param request resource store request
   * @return true if requested path should be added to NFC
   * @since 2.0
   */
  protected boolean shouldAddToNotFoundCache(final ResourceStoreRequest request) {
    // if not local/remote only, add it to NFC
    return !request.isRequestLocalOnly() && !request.isRequestRemoteOnly();
  }

}
