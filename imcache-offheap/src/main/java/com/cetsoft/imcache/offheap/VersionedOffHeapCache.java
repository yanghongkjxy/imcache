/**
 * Copyright © 2013 Cetsoft. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cetsoft.imcache.offheap;

import com.cetsoft.imcache.cache.CacheLoader;
import com.cetsoft.imcache.cache.CacheStats;
import com.cetsoft.imcache.cache.EvictionListener;
import com.cetsoft.imcache.cache.SearchableCache;
import com.cetsoft.imcache.cache.SimpleItem;
import com.cetsoft.imcache.cache.VersionedItem;
import com.cetsoft.imcache.cache.search.IndexHandler;
import com.cetsoft.imcache.cache.search.Query;
import com.cetsoft.imcache.cache.search.index.IndexType;
import com.cetsoft.imcache.cache.util.SerializationUtils;
import com.cetsoft.imcache.concurrent.StripedReadWriteLock;
import com.cetsoft.imcache.offheap.bytebuffer.OffHeapByteBufferStore;
import com.cetsoft.imcache.serialization.Serializer;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The Class VersionedOffHeapCache is a type of offheap cache where cache items have versions that
 * are incremented for each update.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class VersionedOffHeapCache<K, V> implements SearchableCache<K, VersionedItem<V>> {

  /**
   * The off heap cache.
   */
  protected OffHeapCache<K, VersionedItem<V>> offHeapCache;

  /**
   * The read write lock.
   */
  private StripedReadWriteLock readWriteLock;

  /**
   * Instantiates a new versioned off heap cache.
   *
   * @param name the name
   * @param serializer the serializer
   * @param byteBufferStore the byte buffer store
   * @param cacheLoader the cache loader
   * @param evictionListener the eviction listener
   * @param indexHandler the index handler
   * @param bufferCleanerPeriod the buffer cleaner period
   * @param bufferCleanerThreshold the buffer cleaner threshold
   * @param concurrencyLevel the concurrency level
   * @param evictionPeriod the eviction period
   */
  public VersionedOffHeapCache(final String name, final Serializer<VersionedItem<V>> serializer,
      final OffHeapByteBufferStore byteBufferStore,
      final CacheLoader<K, VersionedItem<V>> cacheLoader,
      final EvictionListener<K, VersionedItem<V>> evictionListener,
      final IndexHandler<K, VersionedItem<V>> indexHandler,
      final long bufferCleanerPeriod, final float bufferCleanerThreshold,
      final int concurrencyLevel, final long evictionPeriod) {
    offHeapCache = new OffHeapCache<>(name, cacheLoader, evictionListener,
        indexHandler, byteBufferStore, serializer, bufferCleanerPeriod,
        bufferCleanerThreshold, concurrencyLevel, evictionPeriod);
    this.readWriteLock = new StripedReadWriteLock(concurrencyLevel);
  }

  /**
   * Instantiates a new versioned off heap cache.
   *
   * @param name the name
   * @param byteBufferStore the byte buffer store
   * @param serializer the serializer
   * @param cacheLoader the cache loader
   * @param evictionListener the eviction listener
   * @param indexHandler the index handler
   * @param bufferCleanerPeriod the buffer cleaner period
   * @param bufferCleanerThreshold the buffer cleaner threshold
   * @param concurrencyLevel the concurrency level
   * @param evictionPeriod the eviction period
   */
  public VersionedOffHeapCache(final String name, final OffHeapByteBufferStore byteBufferStore,
      final Serializer<V> serializer, final CacheLoader<K, V> cacheLoader,
      final EvictionListener<K, V> evictionListener, final IndexHandler<K, V> indexHandler,
      final long bufferCleanerPeriod, final float bufferCleanerThreshold,
      final int concurrencyLevel,
      final long evictionPeriod) {
    this(name, new CacheItemSerializer<>(serializer), byteBufferStore,
        new CacheItemCacheLoader<>(cacheLoader),
        new CacheItemEvictionListener<>(evictionListener),
        new CacheItemIndexHandler<>(indexHandler),
        bufferCleanerPeriod, bufferCleanerThreshold, concurrencyLevel, evictionPeriod);
  }


  public void put(K key, VersionedItem<V> value) {
    putInternal(key, value, versionedItem -> offHeapCache.put(key, versionedItem));
  }

  @Override
  public void put(final K key, VersionedItem<V> value, final TimeUnit timeUnit,
      final long duration) {
    putInternal(key, value,
        versionedItem -> offHeapCache.put(key, versionedItem, timeUnit, duration));
  }

  @FunctionalInterface
  private interface PutOperation<V> {

    void put(VersionedItem<V> versionedItem);
  }

  protected void putInternal(final K key, final VersionedItem<V> value,
      final PutOperation<V> putOperation) {
    final int version = value.getVersion();
    VersionedItem<V> exValue = get(key);
    if (exValue != null && version < exValue.getVersion()) {
      throw new StaleItemException(version, exValue.getVersion());
    }
    writeLock(key);
    try {
      exValue = get(key);
      if (exValue != null && version < exValue.getVersion()) {
        throw new StaleItemException(version, exValue.getVersion());
      }
      putOperation.put(value);
    } finally {
      writeUnlock(key);
    }
  }

  /**
   * Write Lock for key is locked.
   *
   * @param key the key
   */
  protected void writeLock(K key) {
    readWriteLock.writeLock(Math.abs(key.hashCode()));
  }

  /**
   * Write Lock for key is unlocked.
   *
   * @param key the key
   */
  protected void writeUnlock(K key) {
    readWriteLock.writeUnlock(Math.abs(key.hashCode()));
  }


  public VersionedItem<V> get(K key) {
    return offHeapCache.get(key);
  }


  public VersionedItem<V> invalidate(K key) {
    return offHeapCache.invalidate(key);
  }


  public boolean contains(K key) {
    return offHeapCache.contains(key);
  }


  public void clear() {
    offHeapCache.clear();
  }


  public long size() {
    return offHeapCache.size();
  }


  public List<VersionedItem<V>> execute(Query query) {
    return offHeapCache.execute(query);
  }


  public String getName() {
    return this.offHeapCache.getName();
  }

  @Override
  public CacheStats stats() {
    return offHeapCache.stats();
  }

  /**
   * The listener interface for receiving cacheItemEviction events. The class that is interested in
   * processing a cacheItemEviction event implements this interface, and the object created with
   * that class is registered with a component using the constructors. When the cacheItemEviction
   * event occurs, that object's appropriate method is invoked.
   *
   * @param <K> the key type
   * @param <V> the value type
   */
  private static class CacheItemEvictionListener<K, V> implements
      EvictionListener<K, VersionedItem<V>> {

    /**
     * The eviction listener.
     */
    private final EvictionListener<K, V> evictionListener;

    /**
     * Instantiates a new cache item eviction listener.
     *
     * @param evictionListener the eviction listener
     */
    public CacheItemEvictionListener(EvictionListener<K, V> evictionListener) {
      this.evictionListener = evictionListener;
    }


    public void onEviction(K key, VersionedItem<V> value) {
      evictionListener.onEviction(key, value.getValue());
    }
  }

  /**
   * The Class CacheItemCacheLoader.
   *
   * @param <K> the key type
   * @param <V> the value type
   */
  protected static class CacheItemCacheLoader<K, V> implements CacheLoader<K, VersionedItem<V>> {

    /**
     * The cache loader.
     */
    private final CacheLoader<K, V> cacheLoader;

    /**
     * Instantiates a new cache item cache loader.
     *
     * @param cacheLoader the cache loader
     */
    public CacheItemCacheLoader(CacheLoader<K, V> cacheLoader) {
      this.cacheLoader = cacheLoader;
    }


    public VersionedItem<V> load(K key) {
      final V value = cacheLoader.load(key);
      if (value == null) {
        return null;
      }
      return new SimpleItem<>(value);
    }

  }

  /**
   * The Class CacheItemSerializer.
   *
   * @param <V> the value type
   */
  protected static class CacheItemSerializer<V> implements Serializer<VersionedItem<V>> {

    /**
     * The serializer.
     */
    private final Serializer<V> serializer;

    /**
     * Instantiates a new cache item serializer.
     *
     * @param serializer the serializer
     */
    public CacheItemSerializer(Serializer<V> serializer) {
      this.serializer = serializer;
    }


    public byte[] serialize(VersionedItem<V> value) {
      byte[] payload = serializer.serialize(value.getValue());
      byte[] newPayload = new byte[payload.length + 4];
      System.arraycopy(payload, 0, newPayload, 0, payload.length);
      System.arraycopy(SerializationUtils.serializeInt(value.getVersion()), 0, newPayload,
          payload.length, 4);
      return newPayload;
    }


    public VersionedItem<V> deserialize(byte[] payload) {
      byte[] newPayload = new byte[payload.length - 4];
      byte[] version = new byte[4];
      System.arraycopy(payload, 0, newPayload, 0, payload.length - 4);
      System.arraycopy(payload, payload.length - 4, version, 0, 4);
      return new SimpleItem<>(SerializationUtils.deserializeInt(version),
          serializer.deserialize(newPayload));
    }

  }

  /**
   * The Class CacheItemIndexHandler.
   *
   * @param <K> the key type
   * @param <V> the value type
   */
  protected static class CacheItemIndexHandler<K, V> implements IndexHandler<K, VersionedItem<V>> {

    /**
     * The query executor.
     */
    private final IndexHandler<K, V> indexHandler;

    /**
     * Instantiates a new cache item query executor.
     *
     * @param indexHandler the query executor
     */
    public CacheItemIndexHandler(IndexHandler<K, V> indexHandler) {
      this.indexHandler = indexHandler;
    }


    public void addIndex(String attributeName, IndexType type) {
      indexHandler.addIndex(attributeName, type);
    }


    public void add(K key, VersionedItem<V> value) {
      this.indexHandler.add(key, value.getValue());
    }


    public void remove(K key, VersionedItem<V> value) {
      this.indexHandler.remove(key, value.getValue());
    }


    public void clear() {
      this.indexHandler.clear();
    }


    public List<K> execute(Query query) {
      return indexHandler.execute(query);
    }

  }

}
