/*
* Copyright (C) 2015 Cetsoft, http://www.cetsoft.com
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
* 
* Author : Yusuf Aytas
* Date   : Aug 3, 2015
*/
package com.cetsoft.imcache.cache.builder;

import org.junit.Test;

import com.cetsoft.imcache.cache.Cache;
import com.cetsoft.imcache.cache.SearchableCache;
import com.cetsoft.imcache.cache.heap.TransactionalHeapCache;
import com.cetsoft.imcache.cache.heap.tx.TransactionCommitter;
import com.cetsoft.imcache.cache.search.index.IndexType;

public class TransactionalHeapCacheBuilderTest {
	
	@Test
	public void build(){
		Cache<Object, Object> cache = CacheBuilder.transactionalHeapCache()
		.cacheLoader(CacheBuilder.CACHE_LOADER)
		.evictionListener(CacheBuilder.EVICTION_LISTENER)
		.indexHandler(DummyIndexHandler.getInstance())
		.transactionCommitter(new TransactionCommitter<Object, Object>() {
			@Override
			public void doPut(Object key, Object value) {}
		})
		.addIndex("age", IndexType.RANGE_INDEX).capacity(1000).build();
		assert(cache instanceof SearchableCache);
		assert(cache instanceof TransactionalHeapCache);
	}
}
