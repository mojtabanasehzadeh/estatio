/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
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
package org.estatio.fixture.project;

import javax.inject.Inject;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancyRepository;

import org.estatio.dom.asset.PropertyRepository;
import org.estatio.dom.party.Party;
import org.estatio.dom.party.PartyRepository;
import org.estatio.dom.project.Program;
import org.estatio.dom.project.ProgramRepository;
import org.estatio.dom.project.ProgramRoleType;

import static org.incode.module.base.integtests.VT.ld;

/**
 * Sets up the {@link org.estatio.dom.project.Program} 
 */
public abstract class ProgramAbstract extends FixtureScript {

    protected Program createProgram(
            final String atPath,
            final String reference, 
            final String name, 
            final String programGoal,
            final Party owner,
            final Party boardMember,
            final ExecutionContext fixtureResults) {
        final ApplicationTenancy applicationTenancy = applicationTenancyRepository.findByPath(atPath);
        Program program = programRepository.newProgram(reference, name, programGoal, applicationTenancy);
        program.programRoleRepository.createRole(program, ProgramRoleType.PROGRAM_OWNER, owner, ld(1999, 1, 1), ld(2000, 1, 1));
        program.programRoleRepository.createRole(program, ProgramRoleType.PROGRAM_BOARDMEMBER, boardMember, ld(1999, 7, 1), ld(2000, 1, 1));
        return fixtureResults.addResult(this, program.getReference(), program);
    }

    // //////////////////////////////////////

  @Inject
    protected ProgramRepository programRepository;
  
  @Inject
  	protected PartyRepository partyRepository;
  
  @Inject
  	protected PropertyRepository propertyRepository;

    @Inject
    protected ApplicationTenancyRepository applicationTenancyRepository;


}
