/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
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
package org.estatio.module.lease;

import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.incode.module.fixturesupport.dom.scripts.TeardownFixtureAbstract;

import org.estatio.module.assetfinancial.EstatioAssetFinancialModule;
import org.estatio.module.base.platform.applib.Module;
import org.estatio.module.base.platform.applib.ModuleAbstract;
import org.estatio.module.event.EstatioEventModule;
import org.estatio.module.index.EstatioIndexModule;
import org.estatio.module.invoice.EstatioInvoiceModule;
import org.estatio.module.invoice.dom.InvoiceAttribute;
import org.estatio.module.invoice.dom.paperclips.PaperclipForInvoice;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseItem;
import org.estatio.module.lease.dom.LeaseItemSource;
import org.estatio.module.lease.dom.LeaseTerm;
import org.estatio.module.lease.dom.LeaseType;
import org.estatio.module.lease.dom.breaks.BreakOption;
import org.estatio.module.lease.dom.breaks.EventSourceLinkForBreakOption;
import org.estatio.module.lease.dom.invoicing.InvoiceForLease;
import org.estatio.module.lease.dom.invoicing.InvoiceItemForLease;
import org.estatio.module.lease.dom.occupancy.Occupancy;
import org.estatio.module.lease.dom.occupancy.tags.Activity;
import org.estatio.module.lease.dom.occupancy.tags.Brand;
import org.estatio.module.lease.dom.occupancy.tags.Sector;
import org.estatio.module.lease.dom.occupancy.tags.UnitSize;
import org.estatio.module.lease.fixtures.DocFragmentDemoFixture;
import org.estatio.module.settings.EstatioSettingsModule;

public final class EstatioLeaseModule extends ModuleAbstract {

    public EstatioLeaseModule(){}

    @Override
    public Set<Module> getDependencies(){
        return Sets.newHashSet(
                new EstatioInvoiceModule(),
                new EstatioAssetFinancialModule(),
                new EstatioIndexModule(),
                new EstatioEventModule(),
                new EstatioSettingsModule()
                );
    }

    @Override
    public FixtureScript getRefDataSetupFixture(){
        return new DocFragmentDemoFixture();
    }

    @Override
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureAbstract() {
            @Override
            protected void execute(final FixtureScript.ExecutionContext executionContext) {

                deleteFrom(EventSourceLinkForBreakOption.class);

                // TODO: convert to appropriate SQL
                deleteFrom(InvoiceAttribute.class);    // for this module's subtype
                deleteFrom(PaperclipForInvoice.class); // for this module's subtype
                deleteFrom(InvoiceItemForLease.class);
                deleteFrom(InvoiceForLease.class);

                deleteFrom(BreakOption.class);
                deleteFrom(LeaseItemSource.class);
                deleteFrom(LeaseTerm.class);
                deleteFrom(LeaseItemSource.class);
                deleteFrom(LeaseItem.class);
                deleteFrom(Occupancy.class);

                deleteFrom(Activity.class);
                deleteFrom(Brand.class);
                deleteFrom(Sector.class);
                deleteFrom(UnitSize.class);

                deleteFrom(Lease.class);
                deleteFrom(LeaseType.class);
            }
        };
    }



    public abstract static class ActionDomainEvent<S>
            extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
            extends org.apache.isis.applib.services.eventbus.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
            extends org.apache.isis.applib.services.eventbus.PropertyDomainEvent<S,T> { }

}
