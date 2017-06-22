package org.estatio.capex.dom.invoice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.SortedSet;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.schema.utils.jaxbadapters.PersistentEntityAdapter;

import org.estatio.capex.dom.documents.categorisation.invoice.SellerBankAccountCreator;
import org.estatio.capex.dom.invoice.approval.IncomingInvoiceApprovalState;
import org.estatio.capex.dom.invoice.approval.triggers.IncomingInvoice_triggerAbstract;
import org.estatio.capex.dom.orderinvoice.OrderItemInvoiceItemLinkRepository;
import org.estatio.capex.dom.project.Project;
import org.estatio.dom.asset.Property;
import org.estatio.dom.budgeting.budgetitem.BudgetItem;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.financial.bankaccount.BankAccount;
import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.party.Party;
import org.estatio.dom.tax.Tax;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE
        // unused since rolled-up to superclass:
        //,schema = "dbo"
        //,table = "IncomingInvoice"
)
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.SUPERCLASS_TABLE)
@javax.jdo.annotations.Discriminator(
        "incomingInvoice.IncomingInvoice"
)
@Queries({
        @Query(
                name = "findByInvoiceNumber", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.capex.dom.invoice.IncomingInvoice "
                        + "WHERE invoiceNumber == :invoiceNumber "),
        @Query(
                name = "findByInvoiceNumberAndSellerAndInvoiceDate", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.capex.dom.invoice.IncomingInvoice "
                        + "WHERE invoiceNumber == :invoiceNumber && seller == :seller && invoiceDate == :invoiceDate "),
        @Query(
                name = "findByBankAccount", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.capex.dom.invoice.IncomingInvoice "
                        + "WHERE bankAccount == :bankAccount ")
})
// unused, since rolled-up
//@Unique(name = "IncomingInvoice_invoiceNumber_UNQ", members = { "invoiceNumber" })
@DomainObject(
        editing = Editing.DISABLED,
        objectType = "incomingInvoice.IncomingInvoice",
        persistedLifecycleEvent = IncomingInvoice.ObjectPersistedEvent.class
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class IncomingInvoice extends Invoice<IncomingInvoice> implements SellerBankAccountCreator {



    public static class ObjectPersistedEvent
            extends org.apache.isis.applib.services.eventbus.ObjectPersistedEvent <IncomingInvoice> {
    }

    public IncomingInvoice() {
        super("invoiceNumber");
    }

    public IncomingInvoice(
            final Type type,
            final String invoiceNumber,
            final String atPath,
            final Party buyer,
            final Party seller,
            final LocalDate invoiceDate,
            final LocalDate dueDate,
            final PaymentMethod paymentMethod,
            final InvoiceStatus invoiceStatus,
            final LocalDate dateReceived,
            final BankAccount bankAccount){
        super("invoiceNumber");
        setType(type);
        setInvoiceNumber(invoiceNumber);
        setApplicationTenancyPath(atPath);
        setBuyer(buyer);
        setSeller(seller);
        setInvoiceDate(invoiceDate);
        setDueDate(dueDate);
        setPaymentMethod(paymentMethod);
        setStatus(invoiceStatus);
        setDateReceived(dateReceived);
        setBankAccount(bankAccount);
    }

    @MemberOrder(name="items", sequence = "1")
    public IncomingInvoice addItem(
            final Charge charge,
            final String description,
            final BigDecimal netAmount,
            final BigDecimal vatAmount,
            final BigDecimal grossAmount,
            final Tax tax,
            final LocalDate dueDate,
            final LocalDate startDate,
            final LocalDate endDate,
            final Property property,
            final Project project,
            final BudgetItem budgetItem) {
        addItem(this, charge, description, netAmount, vatAmount, grossAmount, tax, dueDate, startDate, endDate, property, project, budgetItem);
        return this;
    }

    @Programmatic
    public void addItem(
            final IncomingInvoice invoice,   // REVIEW: this looks odd; why isn't this just gonna use 'this'?
            // this should be an incoming charge
            final Charge charge,
            final String description,
            final BigDecimal netAmount,
            final BigDecimal vatAmount,
            final BigDecimal grossAmount,
            final Tax tax,
            final LocalDate dueDate,
            final LocalDate startDate,
            final LocalDate endDate,
            @Parameter(optionality = Optionality.OPTIONAL)
            final Property property,
            @Parameter(optionality = Optionality.OPTIONAL)
            final Project project,
            @Parameter(optionality = Optionality.OPTIONAL)
            final BudgetItem budgetItem
    ) {
        final BigInteger sequence = nextItemSequence();
        incomingInvoiceItemRepository.upsert(
                sequence,
                invoice,
                charge,
                description,
                netAmount,
                vatAmount,
                grossAmount,
                tax,
                dueDate,
                startDate,
                endDate,
                property,
                project,
                budgetItem);
    }

    public String title() {
        // TODO: need to refine, obviously...
        return "Incoming Invoice";
    }

    //region > _changeBankAccount (action)
    @Mixin(method="act")
    public static class _changeBankAccount extends IncomingInvoice_triggerAbstract {

        private final IncomingInvoice incomingInvoice;

        public _changeBankAccount(final IncomingInvoice incomingInvoice) {
            super(incomingInvoice, Arrays.asList(IncomingInvoiceApprovalState.NEW));
            this.incomingInvoice = incomingInvoice;
        }

        @Action(semantics = SemanticsOf.IDEMPOTENT)
        @ActionLayout(contributed= Contributed.AS_ACTION)
        public IncomingInvoice act(
                final BankAccount bankAccount,
                @Nullable final String comment){
            incomingInvoice.setBankAccount(bankAccount);
            trigger(comment);
            return  incomingInvoice;
        }

        public boolean hideAct() {
            return cannotTransition();
        }

    }

    public enum Type {
        LEGAL,
        CAPEX,
        ASSET,
        LOCAL,
        CORPORATE;

        public boolean isToCompleteByPropertyManagers() {
            return this == LEGAL || this == CAPEX || this == ASSET;
        }
        public boolean isToCompleteByOfficeAdministrator() {
            return this == LOCAL;
        }
        public boolean isToCompleteByCorporateAdministrator() {
            return this == CORPORATE;
        }

        public boolean relatesToProperty() {
            return this == CAPEX || this == ASSET;
        }

        public static Type parse(final String value) {
            if(value == null) {
                return CAPEX;
            }
            String trimmedLowerValue = value.trim().toUpperCase();
            try {
                return valueOf(trimmedLowerValue);
            } catch(IllegalArgumentException ex) {
                return CAPEX;
            }
        }
    }

    @Getter @Setter
    @Column(allowsNull = "false")
    private Type type;

    /**
     * This relates to the owning property, while the child items may either also relate to the property,
     * or could potentially relate to individual units within the property.
     *
     * <p>
     *     Note that InvoiceForLease also has a reference to FixedAsset.  It's not possible to move this
     *     up to the Invoice superclass because invoicing module does not "know" about fixed assets.
     * </p>
     */
    @javax.jdo.annotations.Column(name = "propertyId", allowsNull = "true")
    @org.apache.isis.applib.annotation.Property(hidden = Where.PARENTED_TABLES)
    @Getter @Setter
    private Property property;

    @Getter @Setter
    @Column(allowsNull = "true", name = "bankAccountId")
    private BankAccount bankAccount;

    @Getter @Setter
    @Column(allowsNull = "true")
    private LocalDate dateReceived;

    @Getter @Setter
    @Column(allowsNull = "true", name="invoiceId")
    private IncomingInvoice relatesTo;

    // need to remove this from superclass, ie push down to InvoiceForLease subclass so not in this subtype
    @org.apache.isis.applib.annotation.Property(hidden = Where.EVERYWHERE)
    @Override
    public InvoiceStatus getStatus() {
        return super.getStatus();
    }


    @Programmatic
    public boolean hasProject() {
        final SortedSet<IncomingInvoiceItem> items = getItemsRaw();
        for (IncomingInvoiceItem item : items) {
            final Project project = item.getProject();
            if(project != null) {
                return true;
            }
        }
        return false;
    }

    // cheating
    private SortedSet getItemsRaw() {
        return getItems();
    }

    @Programmatic
    public String reasonInComplete(){
        if (getBankAccount() == null) {
            return "Bank account is required";
        }
        return null;
    }

    //endregion

    @Inject
    private IncomingInvoiceItemRepository incomingInvoiceItemRepository;
    @Inject
    private OrderItemInvoiceItemLinkRepository orderItemInvoiceItemLinkRepository;

}
