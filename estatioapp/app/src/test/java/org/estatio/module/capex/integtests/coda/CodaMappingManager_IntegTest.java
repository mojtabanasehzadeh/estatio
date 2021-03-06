package org.estatio.module.capex.integtests.coda;

import javax.inject.Inject;

import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.value.Blob;

import org.estatio.module.capex.dom.coda.CodaMappingRepository;
import org.estatio.module.capex.imports.CodaMappingManager;
import org.estatio.module.capex.integtests.CapexModuleIntegTestAbstract;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class CodaMappingManager_IntegTest extends CapexModuleIntegTestAbstract {

    @Before
    public void setupData() {
    }

    @Test
    public void upload() throws Exception {

        String fileName = "CODAMappings.xlsx";

        final byte[] pdfBytes = Resources.toByteArray(
                Resources.getResource(CodaMappingManager_IntegTest.class, fileName));
        final Blob blob = new Blob(fileName, "application/pdf", pdfBytes);


        // When
        wrap(new CodaMappingManager()).upload(blob);

        // Then
        assertThat(codaMappingRepository.all()).hasSize(46);

    }

    @Inject CodaMappingRepository  codaMappingRepository;

}
