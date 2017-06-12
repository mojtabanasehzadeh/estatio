package org.estatio.dom.lease;

import org.estatio.dom.agreement.IAgreementType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AgreementTypeEnum implements IAgreementType {
    LEASE("Lease");

    @Getter
    private String title;
}