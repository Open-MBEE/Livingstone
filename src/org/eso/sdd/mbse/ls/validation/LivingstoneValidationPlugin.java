package org.eso.sdd.mbse.ls.validation;

import org.eso.sdd.mbse.ls.validation.CMFAction1;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.actions.ActionsID;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.evaluation.EvaluationConfigurator;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AssociationClass;



public class LivingstoneValidationPlugin extends Plugin {
    @Override
	public void init()     {
        EvaluationConfigurator.getInstance().registerBinaryImplementers(LivingstoneValidationPlugin.class.getClassLoader());
        doTheConfiguration();
    }

    @Override
	public boolean close()
    {
        return true;
    }

    @Override
	public boolean isSupported()
    {
        return true;
    }
    private void doTheConfiguration() { 
    	
    	final CMFAction1 cmfAction = new CMFAction1("CMFAction1","GenerateProfile");
    	//final MDWSFRepairAction  wsfRepairAction   =  new MDWSFRepairAction("MDWSF2","WSF Repair");
    	
    	
    	AMConfigurator mikeConf = new AMConfigurator() { 
    		String EXAMPLES="CMF";
    		public void configure(ActionsManager mngr) { 
    			ActionsCategory category = (ActionsCategory) mngr.getCategory(ActionsID.TOOLS);

    			if( category == null ) {
    				// creating new category
    				category = new MDActionsCategory(EXAMPLES,EXAMPLES);
    				category.setNested(true);
    				mngr.addCategory(category);
    			}
    			category.addAction(cmfAction);	
    			//category.addAction(wsfRepairAction);
    			mngr.addCategory(category);
    			return;
    		}

    		public int getPriority() { 
    			return 0;
    		}
    	};
    	ActionsConfiguratorsManager.getInstance().addMainMenuConfigurator( mikeConf);

    	
        } // end doTheConfiguration();



}
