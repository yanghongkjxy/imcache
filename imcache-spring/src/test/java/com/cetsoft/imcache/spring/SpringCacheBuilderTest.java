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
package com.cetsoft.imcache.spring;

import static org.junit.Assert.assertTrue;

import com.cetsoft.imcache.heap.HeapCache;
import com.cetsoft.imcache.offheap.OffHeapCache;
import com.cetsoft.imcache.offheap.VersionedOffHeapCache;
import com.cetsoft.imcache.offheap.bytebuffer.OffHeapByteBufferStore;
import com.cetsoft.imcache.redis.RedisCache;
import org.junit.Test;

public class SpringCacheBuilderTest {

  @Test
  public void build() {
    SpringCacheBuilder builder = new SpringCacheBuilder();

    builder.setType("heap");
    assertTrue(builder.build() instanceof HeapCache);

    builder.setType("redis");
    builder.setConcurrencyLevel(2);
    assertTrue(builder.build() instanceof RedisCache);

    builder.setType("offheap");
    builder.setEvictionPeriod(2);
    builder.setBufferCleanerPeriod(1000);
    builder.setBufferCleanerThreshold(0.6F);
    OffHeapByteBufferStore bufferStore = new OffHeapByteBufferStore(8388608, 2);
    builder.setBufferStore(bufferStore);
    assertTrue(builder.build() instanceof OffHeapCache);

    builder.setType("versioned_offheap");
    assertTrue(builder.build() instanceof VersionedOffHeapCache);
  }
}
