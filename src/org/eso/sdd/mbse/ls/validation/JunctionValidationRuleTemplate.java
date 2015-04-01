/**
 * $Id: $
 */
package org.eso.sdd.mbse.ls.validation;




import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses.AssociationClass;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.impl.AssociationImpl;

import java.util.*;

/**
  This is a collection of methods to be used as rules for Junctions.
 
 */
public class JunctionValidationRuleTemplate {
	private static Stereotype oneEnd = null;
	private static Stereotype twoEnd = null;
	public static String stereoOneName = "STEREOONENAME";   
	public static String stereoTwoName = "STEREOTWONAME";
	private static boolean oneFound = false;
	private static boolean twoFound = false;
	public static String myRuleName = "RULE_NAME";
	public static String profileName = "PROFILE_NAME";
	
	/*
	 * the constructor is never called.
	 */
     public JunctionValidationRuleTemplate()     {
    	 // no code
     }
     
     private static void logThis(String msg) {
    	 System.out.println(myRuleName+":"+msg);
     }
     
     private static void logThisError(String msg) {
    	 System.err.println(myRuleName+":"+msg);
     }
     

     public static Boolean isAssociationTyped(Class ib) { 
    	 //if(ib.hasRelatedElement()) {
    	 if(ib.has_relationshipOfRelatedElement()) {
    		 List<Relationship> eList = null;
        	 eList = (List<Relationship>)ib.get_relationshipOfRelatedElement();
        	 logThis("** Related Relationships  found for this Interface Block:" + ib.getHumanName()) ;
       		 for(Iterator<Relationship> i = eList.iterator(); i.hasNext();) { 
    			 Element theElem = i.next();
    			 if(theElem != null    ) { 
    				 if (theElem.getHumanType().equals("Generalization"))  { 
    					continue; 
    				 }
    				 if(! theElem.getHumanType().equals("Block")) {
    					 // found one not typed relation, flag it.
    					 logThis("==> " + theElem.getHumanName() + " "+ theElem.getClass().getName());
    					 if(theElem instanceof AssociationImpl ) { 
    						 List<Property> theEnds = null;
    						 Property theProp = null;
    						 AssociationImpl theAssociationImpl = (AssociationImpl)theElem;
    						 
    						 theEnds = theAssociationImpl.getOwnedEnd();
    						 if(theEnds.size() > 1) { 
    							 theProp = theEnds.get(0);
    							 if(theProp != null ) { 
    								 //theProp.get
    								 logThis("\t\tPROPERTY: " + theProp.getHumanName());
    							 }
    							 
								 theProp = theEnds.get(1);
    							 if(theProp != null ) { 
    								 //theProp.get
    								 logThis("\t\tPROPERTY: " + theProp.getHumanName());    								 
    							 }
    						 }
    					 } else {
    						 logThis("\t\tIs not an AssociationImpl");
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
     
     public static Boolean hasCorrectParticipants(AssociationClass ac) {
    	 //return classifierNameError(ac) == null;
    	 // 
    	 oneEnd = StereotypesHelper.getStereotype(Application.getInstance().getProject(), stereoOneName);
    	 twoEnd = StereotypesHelper.getStereotype(Application.getInstance().getProject(), stereoTwoName);
  	     oneFound = false;
  	     twoFound = false;
  	     
    	 // do not used the hasOwnedEnd(), that's not the right one.
    	 if(oneEnd == null || twoEnd == null) { 
    		 logThisError("*** Validation failure: one or more stereotypes are empty!!!");
    	 }
    	 if(ac.hasRelatedElement()) {
    		 List<Element> eList = null;
        	 eList = (List<Element>)ac.getRelatedElement();
        	 System.out.println("** Related Elements found for this Assocations Class:" + ac.getHumanName()) ;
        	 if( eList.size() > 2 ) {
            	 logThisError("** Related Elements found are too many");
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
    			 logThis("** Found correct participants");    			 
    			 return Boolean.TRUE;
    		 } else {
    			 logThis("** One or more correct participants were not found");    			     			 
    			 return Boolean.FALSE;
    		 }
    	 } else {
    		 org.eso.sdd.mbse.ls.Livingstone.logger.info("Test");
    		 logThis("No Related Elements for this Assocations Class:" + ac.getHumanName()) ;
    		 return Boolean.FALSE;
    	 }
    }
}
