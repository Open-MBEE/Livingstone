/**
 * $Id: $
 */
package org.eso.sdd.mbse.ls.validation;

import com.nomagic.actions.NMAction;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses.AssociationClass;
import com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses.impl.AssociationClassImpl;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.impl.AssociationImpl;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import org.eso.sdd.mbse.ls.Livingstone;

import java.lang.Class;
import java.util.*;

/**
 * This is an example of binary validation rule. The rule checks whether all
 * operations which are setters (starts with prefix "set") has input parameters.
 *
 * @author Rimvydas Vaidelis
 * @version $Revision: $, $Date: $
 */
public class TestRule2 {
	private static Stereotype oneEnd = null;
	private static Stereotype twoEnd = null;
	private static String stereoOneName = "Tel2InsMechIF";
	private static String stereoTwoName = "Ins2TelMechIF";
	private static boolean oneFound = false;
	private static boolean twoFound = false;

	
     public TestRule2()     {
    	 // seemingly this code never gets called.
    	 oneEnd = StereotypesHelper.getStereotype(Application.getInstance().getProject(), stereoOneName);
    	 twoEnd = StereotypesHelper.getStereotype(Application.getInstance().getProject(), stereoTwoName);
    	 
     }
     
     
     public static Boolean isAssociationTyped(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class ib) { 
    	 //if(ib.hasRelatedElement()) {
    	 if(ib.has_relationshipOfRelatedElement()) {
    		 List<Relationship> eList = null;
        	 eList = (List<Relationship>)ib.get_relationshipOfRelatedElement();
        	 System.out.println("** Related Relationships  found for this Interface Block:" + ib.getHumanName()) ;
       		 for(Iterator<Relationship> i = eList.iterator(); i.hasNext();) { 
    			 Element theElem = i.next();
    			 if(theElem != null    ) { 
    				 if (theElem.getHumanType().equals("Generalization"))  { 
    					continue; 
    				 }
    				 if(! theElem.getHumanType().equals("Block")) {
    					 // found one not typed relation, flag it.
    					 System.out.println("==> " + theElem.getHumanName() + " "+ theElem.getClass().getName());
    					 if(theElem instanceof AssociationImpl ) { 
    						 List<Property> theEnds = null;
    						 Property theProp = null;
    						 AssociationImpl theAssociationImpl = (AssociationImpl)theElem;
    						 
    						 theEnds = theAssociationImpl.getOwnedEnd();
    						 if(theEnds.size() > 1) { 
    							 theProp = theEnds.get(0);
    							 if(theProp != null ) { 
    								 //theProp.get
    								 System.out.println("\t\tPROPERTY: " + theProp.getHumanName());
    							 }
    							 
								 theProp = theEnds.get(1);
    							 if(theProp != null ) { 
    								 //theProp.get
    								 System.out.println("\t\tPROPERTY: " + theProp.getHumanName());    								 
    							 }
    						 }
    					 } else {
    						 System.out.println("\t\tIs not an AssociationImpl");
    					 }
    					 System.out.flush();
    					 return Boolean.FALSE;
    				 }
    			 }
       		 }

    	 }
		 System.out.flush();    	 
    	 return Boolean.TRUE;
     }
     
     public static Boolean isValid(AssociationClass ac) {
    	 //return classifierNameError(ac) == null;
    	 // 
    	 oneEnd = StereotypesHelper.getStereotype(Application.getInstance().getProject(), stereoOneName);
    	 twoEnd = StereotypesHelper.getStereotype(Application.getInstance().getProject(), stereoTwoName);
  	     oneFound = false;
  	     twoFound = false;
  	     
    	 // do not used the hasOwnedEnd(), that's not the right one.
    	 if(oneEnd == null || twoEnd == null) { 
    		 System.out.println("*** Validation failure: one or more stereotypes are empty!!!");
    	 }
    	 if(ac.hasRelatedElement()) {
    		 List<Element> eList = null;
        	 eList = (List<Element>)ac.getRelatedElement();
        	 System.out.println("** Related Elements found for this Assocations Class:" + ac.getHumanName()) ;
        	 if( eList.size() > 2 ) {
            	 System.out.println("** Related Elements found are too many");
        		 return Boolean.FALSE;
        	 }
    		 for(Iterator<Element> i = eList.iterator(); i.hasNext();) { 
    			 Element theElem = i.next();
    			 if(theElem != null) { 
    				 System.out.println(theElem.getHumanName());
    				 if(StereotypesHelper.hasStereotype(theElem, oneEnd)) { 
    	    			 System.out.println("** has stereotype: " + stereoOneName); 					 
    					 oneFound = true;
    				 }
       				 if(StereotypesHelper.hasStereotype(theElem, twoEnd)) { 
    	    			 System.out.println("** has stereotype: " + stereoTwoName); 					 
    					 twoFound = true;
    				 }
    			 } 			 
    		 }
    		 if(oneFound && twoFound) {
    			 System.out.println("** Found correct participants");    			 
    			 return Boolean.TRUE;
    		 } else {
    			 System.out.println("** One or more correct participants were not found");    			     			 
    			 return Boolean.FALSE;
    		 }
    	 } else {
    		 org.eso.sdd.mbse.ls.Livingstone.logger.info("Test");
    		 System.out.println("No Related Elements for this Assocations Class:" + ac.getHumanName()) ;
    		 return Boolean.FALSE;
    	 }
    }
}
