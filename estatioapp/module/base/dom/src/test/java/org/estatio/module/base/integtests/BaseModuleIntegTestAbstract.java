/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.module.base.integtests;

import org.isisaddons.module.security.SecurityModule;

import org.incode.module.country.dom.CountryModule;

import org.estatio.module.base.EstatioBaseModule;
import org.estatio.module.base.platform.integtestsupport.IntegrationTestAbstract3;

public abstract class BaseModuleIntegTestAbstract
        extends IntegrationTestAbstract3<EstatioBaseModule> {

    public BaseModuleIntegTestAbstract() {
        super(new EstatioBaseModule(), SecurityModule.class, CountryModule.class);
    }

}