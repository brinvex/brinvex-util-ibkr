/*
 * Copyright © 2023 Brinvex (dev@brinvex.com)
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
package com.brinvex.util.ibkr.api.model;


public enum Country {

    US(Currency.USD),

    CZ(Currency.CZK),

    DE(Currency.EUR),
    ;

    private final Currency ccy;

    Country(Currency ccy) {
        this.ccy = ccy;
    }

    public Currency getCcy() {
        return ccy;
    }
}
