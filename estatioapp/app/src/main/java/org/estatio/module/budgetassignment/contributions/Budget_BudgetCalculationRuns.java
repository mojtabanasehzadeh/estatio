package org.estatio.module.budgetassignment.contributions;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRun;
import org.estatio.module.budgetassignment.dom.calculationresult.BudgetCalculationRunRepository;
import org.estatio.module.budget.dom.budget.Budget;

/**
 * This cannot be inlined (needs to be a mixin) because Budget doesn't know about BudgetCalculationRun
 */
@Mixin
public class Budget_BudgetCalculationRuns {

    private final Budget budget;
    public Budget_BudgetCalculationRuns(Budget budget){
        this.budget = budget;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    public List<BudgetCalculationRun> budgetCalculationRuns() {
        return budgetCalculationRunRepository.findByBudget(budget);
    }

    @Inject
    private BudgetCalculationRunRepository budgetCalculationRunRepository;

}
