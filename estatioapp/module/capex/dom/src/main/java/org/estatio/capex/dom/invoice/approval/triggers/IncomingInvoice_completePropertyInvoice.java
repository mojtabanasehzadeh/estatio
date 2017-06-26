package org.estatio.capex.dom.invoice.approval.triggers;

import org.apache.isis.applib.annotation.Mixin;

import org.estatio.capex.dom.invoice.IncomingInvoice;
import org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalStateTransitionType;

@Mixin(method = "act")
public class IncomingInvoice_completePropertyInvoice extends IncomingInvoice_triggerAbstract {

    private final IncomingInvoice incomingInvoice;

    public IncomingInvoice_completePropertyInvoice(IncomingInvoice incomingInvoice) {
        super(incomingInvoice, IncomingInvoiceApprovalStateTransitionType.COMPLETE_PROPERTY_INVOICE);
        this.incomingInvoice = incomingInvoice;
    }


}