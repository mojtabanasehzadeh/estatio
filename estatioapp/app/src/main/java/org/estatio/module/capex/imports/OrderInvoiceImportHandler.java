package org.estatio.module.capex.imports;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.excel.dom.ExcelFixture2;
import org.isisaddons.module.excel.dom.ExcelMetaDataEnabled;
import org.isisaddons.module.excel.dom.FixtureAwareRowHandler;

import org.estatio.module.capex.dom.order.OrderRepository;

import lombok.Getter;
import lombok.Setter;

public class OrderInvoiceImportHandler implements FixtureAwareRowHandler<OrderInvoiceImportHandler>, ExcelMetaDataEnabled {

    @Getter @Setter @Nullable
    private String excelSheetName;
    @Getter @Setter @Nullable
    private Integer excelRowNumber;
    @Getter @Setter @Nullable
    private String status;
    @Getter @Setter @Nullable
    private String orderNumber; // can be populated; called sellerOrderReference on OrderInvoiceLine
    @Getter @Setter @Nullable
    private String charge;
    @Getter @Setter @Nullable
    private LocalDate entryDate;
    @Getter @Setter @Nullable
    private LocalDate orderDate;
    @Getter @Setter @Nullable
    private String seller;
    @Getter @Setter @Nullable
    private String orderDescription;
    @Getter @Setter @Nullable
    private BigDecimal netAmount;
    @Getter @Setter @Nullable
    private BigDecimal vatAmount;
    @Getter @Setter @Nullable
    private BigDecimal grossAmount;
    @Getter @Setter @Nullable
    private String orderApprovedBy;
    @Getter @Setter @Nullable
    private LocalDate orderApprovedOn;
    @Getter @Setter @Nullable
    private String projectReference;
    @Getter @Setter @Nullable
    private String period;
    @Getter @Setter @Nullable
    private String tax;
    @Getter @Setter @Nullable
    private String invoiceNumber;
    @Getter @Setter @Nullable
    private String invoiceType;
    @Getter @Setter @Nullable
    private String invoiceDescription;
    @Getter @Setter @Nullable
    private BigDecimal invoiceNetAmount;
    @Getter @Setter @Nullable
    private BigDecimal invoiceVatAmount;
    @Getter @Setter @Nullable
    private BigDecimal invoiceGrossAmount;
    @Getter @Setter @Nullable
    private String invoiceTax;
    @Getter @Setter @Nullable
    private String orderReference; // generated here; called order number on OrderInvoiceLine

    /**
     * To allow for usage within fixture scripts also.
     */
    @Setter
    private FixtureScript.ExecutionContext executionContext;

    /**
     * To allow for usage within fixture scripts also.
     */
    @Setter
    private ExcelFixture2 excelFixture2;

    public OrderInvoiceLine handle(final OrderInvoiceImportHandler previousRow){

        // try to derive order date
        if (getOrderDate()==null) {
            setOrderDate(getEntryDate());
        }

        if (previousRow != null) {

            // support sparse population for order date (or derived)
            if (getOrderDate() == null && previousRow.getOrderDate() != null){
                setOrderDate(previousRow.getOrderDate());
            }
            // support sparse population for charge
            if (getCharge() == null && previousRow.getCharge() != null) {
                setCharge(previousRow.getCharge());
            }
            // support sparse population for project reference
            if (getProjectReference() == null && previousRow.getProjectReference() != null){
                setProjectReference(previousRow.getProjectReference());
            }
            // support sparse population for period
            if (getPeriod() == null && previousRow.getPeriod() != null){
                setPeriod(previousRow.getPeriod());
            }
            // support sparse population for tax
            if (getTax() == null && previousRow.getTax() != null){
                setTax(previousRow.getTax());
            }

            // copy or generate order reference and copy seller and order description when multiple invoice lines
            if (getEntryDate()==null && invoiceNumberToUse()!=null){
                if (previousRow.getOrderReference()!=null){
                    setOrderReference(previousRow.getOrderReference());
                }
                if (getSeller()==null && previousRow.getSeller()!=null){
                    setSeller(previousRow.getSeller());
                }
                if (getOrderDescription()==null && previousRow.getOrderDescription()!=null){
                    setOrderDescription(previousRow.getOrderDescription());
                }
            }
        }

        // try to generate order reference unless one is passed through from previous row
        if (getOrderReference()==null && getOrderDate()!=null){
            setOrderReference(determineOrderNumber2());
        }

        OrderInvoiceLine lineItem = null;

        if (getEntryDate()!=null || invoiceNumberToUse()!=null) {
            lineItem = new OrderInvoiceLine(
                    getExcelSheetName(),
                    getExcelRowNumber(),
                    validateRow(),
                    getCharge(),
                    getOrderReference(), // called order number on OrderInvoiceLine
                    clean(getOrderNumber()), // called sellerOrderReference on OrderInvoiceLine
                    getEntryDate(),
                    getOrderDate(),
                    getSeller(),
                    getOrderDescription(),
                    netAmountToUse(),
                    vatAmountToUse(),
                    grossAmountToUse(),
                    clean(getOrderApprovedBy()),
                    getOrderApprovedOn(),
                    getProjectReference(),
                    getPeriod(),
                    taxToUse(),
                    invoiceNumberToUse(),
                    getInvoiceType(),
                    invoiceDescriptionToUse(),
                    invoiceNetAmountToUse(),
                    invoiceVatAmountToUse(),
                    invoiceGrossAmountToUse(),
                    invoiceTaxToUse()
            );
        }

        return lineItem;
    }

    private String clean(final String input){
        if (input==null){
            return null;
        }
        String result = input.trim();
        result = result.replace("Devis n°","");
        result = result.replace("Devis ","");
        result = result.replace("Devis","");
        result = result.replace("Accord ","");
        result = result.replace("Facture n°","");
        result = result.replace("Facture ","");
        result = result.replace("Facture","");
        return result.trim();
    }

    private String convert(final String input){

        if (input==null){
            return null;
        }

        // tax
        String result = input;
        if (input.toLowerCase().equals("tva normale")){
            result = "FRF";
        }
        if (input.toLowerCase().equals("exempt")){
            result = "FRE";
        }
        return result;
    }

    private BigDecimal netAmountToUse(){
        return getNetAmount()!=null ? getNetAmount().setScale(2, BigDecimal.ROUND_HALF_UP):null;
    }

    private BigDecimal vatAmountToUse(){
        return getVatAmount()!=null ? getVatAmount().setScale(2, BigDecimal.ROUND_HALF_UP):null;
    }

    private BigDecimal grossAmountToUse(){
        if (getGrossAmount() != null){
            return getGrossAmount().setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (getNetAmount()!= null && getVatAmount() != null) {
            return getNetAmount().add(getVatAmount()).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (getNetAmount()!= null && Objects.equals(taxToUse(), "FRE")){
            return getNetAmount().setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return null;
    }

    private String taxToUse(){
        return getTax() != null ? convert(getTax()) : null;
    }

    private String invoiceNumberToUse(){
        if (getInvoiceNumber()==null || !getInvoiceNumber().matches(".*\\d.*")){
            return null;
        }
        return clean(getInvoiceNumber());
    }

    private String invoiceTaxToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceTax() != null ? convert(getInvoiceTax()) : taxToUse();
    }

    private BigDecimal invoiceNetAmountToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceNetAmount() != null ? getInvoiceNetAmount().setScale(2, BigDecimal.ROUND_HALF_UP) : netAmountToUse();
    }

    private BigDecimal invoiceGrossAmountToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceGrossAmount() !=null ? getInvoiceGrossAmount().setScale(2, BigDecimal.ROUND_HALF_UP) : grossAmountToUse();
    }

    private BigDecimal invoiceVatAmountToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceVatAmount() != null ? getInvoiceVatAmount().setScale(2, BigDecimal.ROUND_HALF_UP) : vatAmountToUse();
    }

    private String invoiceDescriptionToUse(){
        if (getInvoiceNumber()==null){
            return null;
        }
        return getInvoiceDescription() != null ? getInvoiceDescription() : getOrderDescription();
    }

    private String validateRow(){
        StringBuilder b = new StringBuilder();

        // both order and invoice validation
        if (getCharge()==null || getCharge().equals("")){
            b.append("no charge; ");
        }
        if (getProjectReference()==null || getProjectReference().equals("")){
            b.append("no project reference; ");
        }
        if (getSeller() == null || getSeller().equals("")) {
            b.append("no seller; ");
        }
        if (getPeriod() == null || getPeriod().equals("")) {
            b.append("no period; ");
        }
        // order validation
        if (getEntryDate()!=null) {
            if (getVatAmount() == null && taxToUse()!=null && !taxToUse().equals("FRE")) {
                b.append("no vat amount; ");
            }
            if (getNetAmount() == null) {
                b.append("no net amount; ");
            }
            if (grossAmountToUse() == null){
                b.append("no gross amount; ");
            }
            if (getTax() == null || getTax().equals("")) {
                b.append("no tax; ");
            }
            if (getOrderDescription() == null || getOrderDescription().equals("")){
                b.append("no order description; ");
            }
        }

        // invoice validation
        if (getInvoiceNumber()!=null){
            if (invoiceNetAmountToUse()==null){
                b.append("no invoice net amount; ");
            }
            if (invoiceVatAmountToUse()==null && invoiceTaxToUse()!=null && !invoiceTaxToUse().equals("FRE")){
                b.append("no invoice vat amount; ");
            }
            if (invoiceGrossAmountToUse()==null){
                b.append("no invoice gross amount; ");
            }
            if (invoiceTaxToUse()==null || invoiceTaxToUse().equals("")){
                b.append("no invoice tax; ");
            }
        }

        //charge validation
        List<String> charges = Arrays.asList(
                "PROJECT MANAGEMENT",
                "TAX",
                "WORKS",
                "RELOCATION / DISPOSSESSION INDEMNITY",
                "ARCHITECT / GEOMETRICIAN FEES",
                "LEGAL / BAILIFF FEES",
                "MARKETING",
                "TENANT INSTALLATION WORKS",
                "SECURITY AGENTS",
                "LETTING FEES",
                "INSURANCE",
                "FURNITURES / DECORATION",
                "OTHER"
        );
        if (getCharge()!=null && !charges.contains(getCharge())){
            b.append("charge unknown; ");
        }

        //tax validation
        List<String> taxcodes = Arrays.asList(
                "FRA",
                "FRB",
                "FRC",
                "FRD",
                "FRE",
                "FRF",
                "FRO",
                "FRR",
                "FRS",
                "GBR-VATSTD",   // added for Open source version, integ testing
                "NLD-VATSTD"
        );
        if (taxToUse()!=null && !taxcodes.contains(taxToUse())){
            b.append("tax unknown; ");
        }
        if (invoiceTaxToUse()!=null && !taxcodes.contains(invoiceTaxToUse())){
            b.append("invoice tax unknown; ");
        }

        //period validation
        if (getPeriod()!=null && !getPeriod().matches("F\\d{4}")){
            b.append("period unknown; ");
        }

        //project reference validation
        if (getProjectReference()!=null && !getProjectReference().matches("^([^-]+)[-].*$")){
            b.append("project reference not correct; ");
        }

        return b.length()==0 ? "OK" : b.toString();
    }

    // TODO: this method is of no use here, because orders are created on OrderInvoiceLine; replaced by determineOrderNumber2
    String determineOrderNumber() {
        Integer counter = 1;
        String suffix = "-".concat(String.format("%03d", counter));
        String result = getOrderDate().toString().replace("-","").concat(suffix);
        while (orderRepository.findByOrderNumber(result)!=null){
            counter = counter + 1;
            suffix = "-".concat(String.format("%03d", counter));
            result = getOrderDate().toString().replace("-","").concat(suffix);
        }
        return result;
    }

    String determineOrderNumber2() {
        if (getProjectReference()==null) return null;
        final Pattern projectReferencePattern = Pattern.compile("^([^-]+)[-].*$");
        final Matcher matcher = projectReferencePattern.matcher(getProjectReference());
        if(!matcher.matches()) {
            return null;
        }
        final String propertyReference = matcher.group(1);
        String suffix = "-".concat(String.format("%03d", getExcelRowNumber()));
        String prefix = propertyReference.concat("-");
        String result = prefix.concat(getOrderDate().toString().replace("-","")).concat(suffix);
        return result;
    }

    @Override
    public void handleRow(final OrderInvoiceImportHandler previousRow) {

            if(executionContext != null && excelFixture2 != null) {
                executionContext.addResult(excelFixture2,this.handle(previousRow));
            }

    }

    @Inject
    OrderRepository orderRepository;

}

