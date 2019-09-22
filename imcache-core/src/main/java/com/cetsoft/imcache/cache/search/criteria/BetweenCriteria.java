/**
 * Copyright © 2013 Cetsoft. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.cetsoft.imcache.cache.search.criteria;

import com.cetsoft.imcache.cache.search.index.CacheIndex;
import java.util.List;

/**
 * The Class BetweenCriteria is used to retrieve items greater than lowerBound and less than
 * upperBound.
 */
public class BetweenCriteria extends ArithmeticCriteria {

  /**
   * The upper bound.
   */
  private Object upperBound;

  /**
   * Instantiates a new between criteria.
   *
   * @param attributeName the attribute name
   * @param lowerBound the lower bound
   * @param upperBound the upper bound
   */
  public BetweenCriteria(String attributeName, Object lowerBound, Object upperBound) {
    super(attributeName, lowerBound);
    this.upperBound = upperBound;
  }


  public List<Object> meets(CacheIndex cacheIndex) {
    return cacheIndex.between(value, upperBound);
  }

}
