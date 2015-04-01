/**
 * $Id: $
 */
package org.eso.sdd.mbse.ls.validation;




import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdassociationclasses.AssociationClass;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.impl.AssociationImpl;

import com.nomagic.uml2.ext.magicdraw.deployments.mdartifacts.Artifact;
import com.nomagic.uml2.ext.magicdraw.deployments.mdnodes.Node;

import org.apache.log4j.Logger;

import java.util.*;

/**
  This is a collection of methods to be used as rules for Junctions.
 
 */
public class BlockValidationRuleTemplate {
	private static String myRuleName = "RULE_NAME";
	private static String profileName = "PROFILE_NAME";
	
	/*
	 * the constructor is never called.
	 */
     public BlockValidationRuleTemplate()     {
    	 // no code
     }
     
      public static Boolean hasInnerElementTemplate(Element ib) {
    	 String ruleName   = "BRULE_NAME";
    	 String ieName     = "INNER_ELEMENT_NAME";
    	 String ieTypeName = "INNER_ELEMENT_TYPE";
    	 Profile theProfile = StereotypesHelper.getProfile(Application.getInstance().getProject(),profileName);
    	 Stereotype theStereo = StereotypesHelper.getStereotype(Application.getInstance().getProject(), ieTypeName,theProfile);
    	 System.out.println(myRuleName + " talking... ("+ib.getClass().getName()+")");
    	 System.out.println(myRuleName + " ruleName:" + ruleName);
    	 System.out.println(myRuleName + " ieName:" + ieName);
    	 System.out.println(myRuleName + " ieTypeName:" + ieTypeName);
    	 
    	 //Logger.getLogger(myRuleName).info("I am rule (ATTR) "+ruleName+": being called on "+ ib.getHumanName() + " with: "+ieName+":"+ieTypeName);
    	 System.out.println("I am rule (ATTR) "+ruleName+": being called on "+ ib.getHumanName() + " with: "+ieName+":"+ieTypeName);
    	 //System.out.println(myRuleName + " still talking... ("+ruleName+")");    	 
    	 if(! ib.hasOwnedElement()) {
    		 System.out.println(ruleName+"\tNo owned elements. Nothing to do for "+ib.getHumanName());
    		 System.out.flush();
    		 return Boolean.FALSE;
    	 }
    	 
		 if(ieName.equals("type") && ib instanceof TypedElement) { 
			 Type theType = ((TypedElement)ib).getType();
			 if(StereotypesHelper.hasStereotypeOrDerived(theType, theStereo)  ) {
				 // at least one element is fulfilling, we are happy.
	    		 System.out.println(ruleName+"\t"+ib.getHumanName() + 
	    				 " is a typed element and its type is stereotyped by: "     + theStereo.getName());
	    		 System.out.flush();
				 return Boolean.TRUE;
			 } else {
	    		 System.out.println(ruleName+"\t"+ib.getHumanName() +
	    				 " is a typed element but its type is not stereotyped by: " + theStereo.getName());
	    		 System.out.flush();
				 return Boolean.FALSE;
			 }
		 }

    	 for(Element theEle: ib.getOwnedElement()) {
    		 System.out.print(ruleName+" => inspecting "+theEle.getHumanName()+ ".");
    		 // attention: the getHumanType() has the nasty habit of putting an intermediate space
    		 
    		 if(theEle.getHumanType().replace(" ","").equals(ieName)) {
    			 System.out.println(" Interesting.");
    			 // le's see if its type is stereotyped by what we need
    			 System.out.println(ruleName+"\tfound at "+ieName);
    			 if(theEle instanceof TypedElement) { 
    				 TypedElement te = (TypedElement)theEle;
    				 Element theTypeElement = te.getType();
    				 if(theTypeElement != null) { 
        				 // now try to determine whether this element is stereotyped correctly
        				 if(StereotypesHelper.hasStereotypeOrDerived(theTypeElement, theStereo)  ) {
        					 // at least one element is fulfilling, we are happy.
        		    		 System.out.println(ruleName+"\t"+theEle.getHumanName() +
        		    				 " is a typed element and  its type is stereotyped by: " + theStereo.getName());
        		    		 System.out.flush();
        					 return Boolean.TRUE;
        				 } else {
        	    			 System.out.println(ruleName+"\tdoes not have stereoytpe "+ieTypeName);
        	    			 System.out.flush();
        				 }
    				 } else {
    	    			 System.out.println(ruleName+"\tdoes not have a type set.");    					 
    				 }
    			 } else {
        			 System.out.println(ruleName+"\tbut it's not typed");
        			 System.out.flush();
    			 }
    		 } else {
    			 System.out.println(" Discarded.");
    			 System.out.flush();
    		 }
    	 }
    	 System.out.println(ruleName + " checking completed.");    	 
    	 System.out.flush();    	 
    	 return Boolean.FALSE;
     }
  
     public static Boolean hasInnerRelationshipTemplate(Element ib) {
    	 String ruleName   = "BRULE_NAME";
    	 String ieName     = "INNER_ELEMENT_NAME";
    	 String ieTypeName = "INNER_ELEMENT_TYPE";
    	 Profile theProfile = StereotypesHelper.getProfile(Application.getInstance().getProject(),profileName);
    	 Stereotype theStereo = StereotypesHelper.getStereotype(Application.getInstance().getProject(), ieTypeName,theProfile);
     	 // so far so good.
    	 
    	 Logger.getLogger(myRuleName).info("I am rule (REL) "+ruleName+": being called on "+ ib.getHumanName() + " with: "+ieName+":"+ieTypeName);
    	 if(! ib.has_directedRelationshipOfSource() ) {
    		 return Boolean.FALSE;
    	 }
    	 

    	 for(DirectedRelationship  dr: ib.get_directedRelationshipOfSource()) { 
    		 System.out.print(ruleName+" => inspecting "+dr.getHumanName() + ".");
    		 if(dr.getHumanType().replace(" ","").equals(ieName)) {
    			 System.out.println(" Interesting.");
    			 // le's see if its type is stereotyped by what we need
    			 System.out.println(ruleName+"\tfound "+ieName);
    			 Element theEle = dr.getTarget().iterator().next();

    			 /*
    			 if(theEle instanceof TypedElement) { 
    				 TypedElement te = (TypedElement)theEle;
    				 Element theTypeElement = te.getType();
    				 // now try to determine whether this element is stereotyped correctly
    				 if(StereotypesHelper.hasStereotypeOrDerived(theTypeElement, theStereo)  ) {
    					 // at least one element is fulfilling, we are happy.
    		    		 System.out.println(ruleName+"\t"+theEle.getHumanName() +
    		    				 " is a typed element and  its type is stereotyped by: " + theStereo.getName());
    		    		 System.out.flush();
    					 return Boolean.TRUE;
    				 } else {
    	    			 System.out.println(ruleName+"\tdoes not have stereoytpe "+ieTypeName);    					 
    				 }
    			 } else {
        			 System.out.println(ruleName+"\tbut it's not typed");
    			 }
    			 */	    		
    			 
    			 if(StereotypesHelper.hasStereotypeOrDerived(theEle, theStereo)  ) {
    				 // at least one element is fulfilling, we are happy.
		    		 System.out.println(ruleName+"\t"+theEle.getHumanName() +
		    				 " is a typed element and  its type is stereotyped by: " + theStereo.getName());
		    		 System.out.flush();
    				 return Boolean.TRUE;
    			 } else {
    				 System.out.println(ruleName+"\tdoes not have stereoytpe "+ieTypeName);    					 
    			 }    			 

    		 } else {
    			 System.out.println(" Discarded.");
    		 }

    	 }
    	 System.out.println(ruleName + " checking completed.");    	 
    	 System.out.flush();    	 
    	 return Boolean.FALSE;
     }    
}
