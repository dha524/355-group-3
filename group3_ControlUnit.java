package labs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jaamsim.BasicObjects.ExpressionEntity;

import hccm.controlunits.ControlUnit;
import hccm.entities.ActiveEntity;


public class group3_ControlUnit extends ControlUnit {

    public void OnStartWaitForDeposit(List<ActiveEntity> ents, double simTime) {
        ArrayList<ActiveEntity> idleStaff = this.getEntitiesInActivity("StaffEntity", "WaitForTaskStaff", simTime);
        ArrayList<ActiveEntity> depositStaff = this.getEntitiesInActivity("StaffEntity", "Depositing", simTime);
        ActiveEntity tin = ents.get(0);
        
        ExpressionEntity providedValues = (ExpressionEntity) getSubmodelEntity("ProvidedValues");
        double depositorLevel = getNumAttribute(providedValues, "DepositorLevel", simTime, -1);

        AttributeCompare depositorComp = new AttributeCompare("previouslyDepositing");

        if (idleStaff.size() > 0 && depositStaff.size() == 0 && depositorLevel > 0 ) {
            Collections.sort(idleStaff, depositorComp);
            
            ActiveEntity deposit_staff = idleStaff.getLast();
            int previouslyDepositing = (int) getNumAttribute(deposit_staff, "previouslyDepositing", simTime, -1);    

            if (previouslyDepositing == 1) {
                transitionTo("Depositing", deposit_staff, tin);
            } else {
                ActiveEntity randomStaff = idleStaff.get(0);
                transitionTo("Depositing", randomStaff, tin);
            }
        }
    }

    public void OnStartWaitForMixing(List<ActiveEntity> ents, double simTime) {
        ArrayList<ActiveEntity> idleStaff = this.getEntitiesInActivity("StaffEntity", "WaitForTaskStaff", simTime);
        ArrayList<ActiveEntity> mixingStaff = this.getEntitiesInActivity("StaffEntity", "Mixing", simTime);
        ActiveEntity ingredients = ents.get(0);

        ExpressionEntity providedValues = (ExpressionEntity) getSubmodelEntity("ProvidedValues");
        double mixerLevel = getNumAttribute(providedValues, "MixerLevel", simTime, -1);
        
        ActivityStartCompare actStartComp = this.new ActivityStartCompare();

        if (idleStaff.size() > 0 && mixingStaff.size() == 0 && mixerLevel == 0) {
            Collections.sort(idleStaff, actStartComp);

            ActiveEntity mixStaff = idleStaff.get(0);

            transitionTo("Mixing", mixStaff, ingredients);
        }

    }

    public void OnStartWaitForTask(List<ActiveEntity> ents, double simTime) {
        ArrayList<ActiveEntity> idleTins = this.getEntitiesInActivity("TinEntity", "WaitForDepositTins", simTime);
        ArrayList<ActiveEntity> idleIngredients = this.getEntitiesInActivity("IngredientEntity", "WaitForMixingIngredients", simTime);
        ArrayList<ActiveEntity> depositStaff = this.getEntitiesInActivity("StaffEntity", "Depositing", simTime);
        ArrayList<ActiveEntity> refilStaff = this.getEntitiesInActivity("StaffEntity", "Refilling", simTime);
        ArrayList<ActiveEntity> mixStaff = this.getEntitiesInActivity("StaffEntity", "Mixing", simTime);

        ExpressionEntity providedValues = (ExpressionEntity) getSubmodelEntity("ProvidedValues");
        double depositorLevel = getNumAttribute(providedValues, "DepositorLevel", simTime, -1);
        double mixerLevel = getNumAttribute(providedValues, "MixerLevel", simTime, -1);

        ActivityStartCompare actStartComp = this.new ActivityStartCompare();

        if (idleTins.size() > 0 && depositStaff.size() == 0 && depositorLevel > 0) {
            Collections.sort(idleTins, actStartComp);

            ActiveEntity staff = ents.get(0);
            ActiveEntity depositTin = idleTins.get(0);

            transitionTo("Depositing", staff, depositTin);

        // CAPACITY LEVEL OF DEPOSITOR AND MIXER, AMOUNT OF INGREDIENTS CHANGED
        } else if (mixerLevel > 0 && refilStaff.size() == 0 && depositStaff.size() == 0 && depositorLevel != 30) {
            ActiveEntity staff = ents.get(0);

            transitionTo("Refilling", staff);

        }

        ArrayList<ActiveEntity> idleStaff = this.getEntitiesInActivity("StaffEntity", "WaitForTaskStaff", simTime);

        if (idleIngredients.size() > 0 && mixerLevel == 0 && mixStaff.size() == 0 && idleStaff.size() > 0) {
            Collections.sort(idleStaff, actStartComp);
            Collections.sort(idleIngredients, actStartComp);

            ActiveEntity staff = idleStaff.get(0);
            ActiveEntity ingredients = idleIngredients.get(0);
            
            transitionTo("Mixing", staff, ingredients);
        }
    }
}
