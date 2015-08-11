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
* Date   : Aug 4, 2015
*/
package com.cetsoft.imcache.spring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cetsoft.imcache.cache.SearchableCache;
import com.cetsoft.imcache.cache.builder.CacheBuilder;

public class ImcacheCacheManagerTest {

	
	@Mock
	CacheBuilder builder;
	
	@Mock
	SearchableCache<Object, Object> cache;
	
	ImcacheCacheManager cacheManager;
	
	String cacheName = "cache";
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		doReturn(cache).when(builder).build(cacheName);
		cacheManager = new ImcacheCacheManager(builder);
	}
	
	@Test
	public void getCache(){
		assertEquals(cache, cacheManager.getCache(cacheName).getNativeCache());
	}
	
	@Test
	public void getCacheNames(){
		cacheManager.getCache(cacheName);
		assertEquals(1, cacheManager.getCacheNames().size());
		assertTrue(cacheManager.getCacheNames().contains(cacheName));
	}
	
	@Test
	public void setGetCacheBuilder(){
		cacheManager = new ImcacheCacheManager();
		cacheManager.setCacheBuilder(builder);
		assertEquals(builder, cacheManager.getCacheBuilder());
	}
	
}