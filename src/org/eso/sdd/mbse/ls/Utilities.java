/*
 * 
 *    (c) European Southern Observatory, 2011
 *    Copyright by ESO 
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *    
 *    $Id: TestMetricCompute.java 562 2012-05-01 16:25:12Z nb-linux $
 *
*/
package org.eso.sdd.mbse.ls;

/**

 *
 * Copyright (c) 2004 NoMagic, Inc. All Rights Reserved.
 */

// present in the example
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.runtime.ApplicationExitedException;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;


// added by me.
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.dependency.DependencyCheckResult;
import com.nomagic.magicdraw.dependency.DependencyCheckerHelper;
import com.nomagic.magicdraw.dependency.ElementLocation;
import com.nomagic.magicdraw.dependency.ElementLocationDependency;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;
import com.nomagic.magicdraw.openapi.uml.PresentationElementsManager;

import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.PropertyID;

import com.nomagic.magicdraw.sysml.util.SysMLConstants ;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
// utility
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;

/**
 * @author Michele Zamparelli
 * @version $Revision: 562 $
 */

public class Utilities {
    private static final boolean Debug = true;
	
	private static String theProfileToBeProbed;
	
	
	
	// PROFILES 
	private Profile OOSEMProfile = null;
	private Profile SE2Profile = null;
	private Profile SysMLProfile = null;	
	
	
	
	// Stereotypes
	
	
	private Project theProject = null;

	

	
	// boolean markers for ease of reference
	private boolean isOOSEM = false;
	private boolean isSysML = false;
	private boolean isSE2  = false;
	private boolean isCOWL = false;
	
	private HashMap<String,Stereotype> stereoOOSEMHash = new HashMap<String,Stereotype>();
	private HashMap<String,Integer> metricsStereoHash = new HashMap<String,Integer>(); 
	
	private Logger logger = null;
	private Stereotype theRequirementStereo = null;
	private Stereotype theSE2CDStereo = null; // Context Diagram
	private Stereotype theSE2PTStereo = null; // Product Tree
	private Stereotype theSE2PAStereo = null; // performance aspect
	private Stereotype theSE2SAStereo = null; // structure aspect
	private Stereotype theSE2BAStereo = null; // behavior aspect
	
	
	public Utilities() { 
		
		
	}
	
//	private void oldCode() { 
//
//		if(dependsOn(Constants.oosemName)) {
//			Iterator<String> ii = null;
//			OOSEMProfile = StereotypesHelper.getProfile(Application.getInstance()
//					.getProject(), Constants.oosemName);
//			// initialize the hash table
//
//			stereoOOSEMHash.put(opPhysicalName,null);
//			stereoOOSEMHash.put(opConceptualName,null);
//			stereoOOSEMHash.put(opSOSName,null);
//			stereoOOSEMHash.put(opMOPName,null);
//			stereoOOSEMHash.put(opMOEName,null);
//			stereoOOSEMHash.put(opDataName,null);
//
//			for(ii = stereoOOSEMHash.keySet().iterator(); ii.hasNext(); ) { 
//				String stereoName = ii.next();
//				Stereotype aStereotype = StereotypesHelper.getStereotype(theProject, stereoName, OOSEMProfile);
//				if(aStereotype != null) { 
//					stereoOOSEMHash.put(stereoName,aStereotype);
//				} else {
//					logThis("MBSE The stereotype " +stereoName+ " could not be found.");
//				}
//			}
//
//			isOOSEM = true;
//			logThis("MBSE project "+theProject.getName() + " depends on OOSEM");
//		} else {
//			logThis("MBSE project "+theProject.getName() + " does not depend on OOSEM");			
//		}
//		
//		if(dependsOn(sysmlName)) { 		
//			SysMLProfile = StereotypesHelper.getProfile(Application.getInstance()
//					.getProject(), sysmlName);
//			isSysML = true;
//			theRequirementStereo = StereotypesHelper.getStereotype (Application
//					.getInstance().getProject(), "Requirement", SysMLProfile);
//			// and probably others later on
//		}
//		
//		if(dependsOn(se2Name)) { 
//			SE2Profile = StereotypesHelper.getProfile(Application.getInstance()
//					.getProject(), se2Name);
//			isSE2 = true;
//			theSE2CDStereo = StereotypesHelper.getStereotype (Application
//					.getInstance().getProject(), "se2.ContentDiagram", SE2Profile);
//			
//			theSE2SAStereo = StereotypesHelper.getStereotype (Application
//					.getInstance().getProject(), "se2.Structure Aspect", SE2Profile);
//		}
//	}
//	

     /*
      @params: the string of the package to be inspected
      @params: the model, provided by the application

    */
    
	 private int isOOSEMStereotyped(Element el) {
    	NamedElement ne = null;
    	if(el instanceof NamedElement ) {
    		ne = (NamedElement)el;
    	} 
    	if(StereotypesHelper.hasStereotypeOrDerived(el,"Requirement")) {
    		return 1;
    	}
    	return 0;
    }

	 public boolean  dependsOn(Project theProject, String theModule) {
		 if(theProject.isTeamworkServerProject()) { 
			 Iterator<ElementLocationDependency> i = null;
			 DependencyCheckResult results = DependencyCheckerHelper.checkDependencies(theProject);
			 if(!results.hasDependencies()) { 
				 return false;
			 } else { 
				 Collection<ElementLocationDependency> myDeps = results.getDependencies();
				 for(i = myDeps.iterator(); i.hasNext(); ) {
					 ElementLocationDependency eld = null;
					 eld = i.next();
					 ElementLocation el = eld.getElementLocation();
					 ElementLocation del = eld.getDependsOnLocation();
					 if(del.toString().equals(theModule)) { 
						 return true;
					 }
				 }
				 return false;
			 }
		 } else {
			 Profile theProfile = StereotypesHelper.getProfile(theProject,theModule);
			 if(theProfile == null ) {
				 return false;
			 }
			 return true;
		 }
	 }

    
	 private void logThis(String log) { 
		 //System.out.println(log);
		 logger.info(log);
	 }
	 
    private  void  recurseCompute(Element el) {
    	int count = 0;
    	//System.out.print("*** " + el.getHumanName());
    	if(el instanceof NamedElement) {
    		//logThis(" (" + ((NamedElement)el).getName()+")");
    	} else {
    		//logThis("");
    	}
    	
    	inspectStereotypes(el);
    	
    	if(el instanceof Diagram) { 
       		Diagram theDiagram = (Diagram)el;
    		com.nomagic.magicdraw.properties.Property property = null;  		

			DiagramPresentationElement diagramPE = Application.getInstance().getProject().getDiagram(
					theDiagram);
			property = diagramPE.getProperty(PropertyID.SHOW_DIAGRAM_INFO) ;
			
			logThis("Diagram: " + theDiagram.getName()+ " (" + el.getHumanType() + ")" );
			logThis("\t"+ diagramPE.getDiagramType().getType());
    		

			if(! property.getValueStringRepresentation().equals("true")  ) {
	    		logThis("\tthis diagram has no Diagram Info");
	    		//logThis("\tits class is "+ el.getClass().toString());
			}
    		
    		if (diagramPE.getDiagramType().getType().equals(Constants.DT_PKD)) {
    			logThis("MBSE Found a SysML package Diagram named "+ theDiagram.getName());
    			if(! StereotypesHelper.hasStereotype(theDiagram, theSE2CDStereo)) {
        			logThis("\tWARNING: is is not a content description");
    				
    			}
    		}
    	}
    	
    	 	
    	
    	if(el.hasOwnedElement()) { 
    		for(Iterator<Element> it = el.getOwnedElement().iterator(); it.hasNext(); ) { 
    			Element ownedElement = it.next();
    			recurseCompute(ownedElement);
    		}
    	} else {
    		//logThis("*** " + el.getHumanName() + " TERMINATION CONDITION");
    		//termination condition
    	}
     }


	private void inspectStereotypes(Element el) {
		if(isOOSEM)  {
			Iterator<String> ii = null;
			for(ii = stereoOOSEMHash.keySet().iterator(); ii.hasNext(); ) { 
				String stereoName = ii.next();
				Stereotype aStereotype = stereoOOSEMHash.get(stereoName);
				if(aStereotype != null) { 
					if(StereotypesHelper.hasStereotype(el, aStereotype)) {
						logThis("MBSE: element "+el.getHumanName() + " is stereotyped by "+ stereoName);
						Integer theMetric = metricsStereoHash.get(stereoName);				
						if( theMetric != null) { 
							theMetric++;
							metricsStereoHash.put(stereoName, theMetric);
						} else {
							metricsStereoHash.put(stereoName, new Integer(1));							
						}
					}
				} else {
					//logThis("Could not retrieve stereotype for " + stereoName);
				}
			}
			
		}
		
		// same should be done for SysML stereotypes
		if(isSysML) { 
			
		}
		
	}

	private int isStateMachine(Element el) {
		// TODO Auto-generated method stub
    	if(el.getHumanType().equals("State Machine")) {
    		if(el instanceof StateMachine) {
    			StateMachine stm = (StateMachine)el;
    			
    		}
    		return 1;
    	}
		
		return 0;
	}

	private int countStateMachineDepth(State theState) {
		Collection<Region> theRegions= theState.getRegion();
		for(Iterator i = theRegions.iterator(); i.hasNext(); ) {
			Region theRegion = (Region)i.next();
			if(theRegion.isLeaf()) { 
				return 0;
			} 
			//theRegion.get
		}
		
				
		return 0;
	}
	
	public void displayStereotypeMetrics() {
		if(isOOSEM)  {
			Iterator<String> ii = null;
			if(metricsStereoHash.size() == 0) { 
				logThis("MBSE: no metrics for stereotypes recorded");
			} else { 
				for(ii = metricsStereoHash.keySet().iterator(); ii.hasNext(); ) { 
					String stereoName = ii.next();
					Integer theMetric = metricsStereoHash.get(stereoName);				
					if( theMetric != null) { 
						logThis("MBSE\t"+stereoName+" "+theMetric.toString());
					}  
				}
			}
		} else {
			logThis("MBSE project is not OOSEM");
		}
	}
	
	 private int hasStereotypeFromPackage(Element el) {
    	NamedElement ne = null;
    	Iterator<Stereotype> ii = null;
    	if(el instanceof NamedElement ) {
    		ne = (NamedElement)el;
    		List<Stereotype> theList = StereotypesHelper.getStereotypes(el);
    		if(theList.size() == 0) { 
    			return 0;
    		} else {
				Stereotype theStereotype = null;
    			for(ii = theList.iterator(); ii.hasNext(); ) {
    				theStereotype = ii.next();
    				Profile theProfile = null;
    				theProfile = theStereotype.getProfile();
    				if(theProfile != null  && theProfile.getName().equals(getTheProfileToBeProbed())){ 
    						return 1;	
    				}
    			}
    			return 0;
    		}
    		
    	} 
    	return 0;
	}

	 private int isSatisfy(Element el) { 
		 // satisfy
		 if(el.getHumanType().startsWith("Satisfy")) { 
			 logThis("MBSE found some satisfy :" + el.getHumanName()+ "(" + el.getHumanType() + ")" );
			 logThis("\tits class is:"+ el.getClass().toString());
			 return 1;
		 }
		 return 0;
	 }

	 
	 private int isVerify(Element el) { 
		 if(el.getHumanType().startsWith("Verify")) { 
			 logThis("MBSE found some verify :" + el.getHumanName()+ "(" + el.getHumanType() + ")" );
			 logThis("\tits class is:"+ el.getClass().toString());
			 return 1;
		 }
		 return 0;
	 }

  	
	 private int isRequirement(Element el) {
    	NamedElement ne = null;
    	if(el instanceof NamedElement ) {
    		ne = (NamedElement)el;
    	} 
    	if(StereotypesHelper.hasStereotypeOrDerived(el,"Requirement")) {
    		return 1;
    	}
    	if(el.getHumanType().equals("Requirement")) {
    		return 1;
    	}
    	return 0;
    }

     private int isValueProperty(Element el) {
    	if(el.getHumanType().equals("Value Property")) {
    		return 1;
    	}
    	return 0;
    }

    private  int isBoundValueProperty(Element el) {
    	if(el.getHumanType().equals("Value Property")) {
    		if(isValuePropertyBoundToConstraintParameterViaBindingConnector((Property)el)) { 
    			logThis("Bound Property: " + ((Property)el).getName());
    			return 1;
    		}
    	}
    	return 0;
	}

    private  int isValueType(Element el) {
    	if(el.getHumanType().equals("Value Type")) {
    		return 1;
    	}
    	return 0;
	}
    
    
    /**

     * @param valueProperty is the given value property.
     * @return true if the given value property is bound to at least one constraint parameter
     * via a binding connector.
       */
    public boolean isValuePropertyBoundToConstraintParameterViaBindingConnector(Property valueProperty) {
    	// The value properyy must not be null
    	assert valueProperty != null;
    	// Get all connector ends which are connected to the value property
    	List<ConnectorEnd> ends = valueProperty.getEnd();

    	for(ConnectorEnd end : ends) {
    		// Find the binding connector which is connected to the given value property
    		Connector connector = end.get_connectorOfEnd();

    		if(StereotypesHelper.hasStereotype(connector, SysMLConstants.BINDING_CONNECTOR_STEREOTYPE)){
    			// Find the element which are connected to the opposite end of binding connector.
    			int oppositEndIndex = connector.getEnd().get(0).getRole().equals(valueProperty) ? 1 : 0;
    			ConnectableElement oppositRole = connector.getEnd().get(oppositEndIndex).getRole();

    			// Check whether that the element at the opposite end is constraint parameter or not
    			// It must be applied with <<ConstrainParameter>> stereotype.
    			if(StereotypesHelper.hasStereotype(oppositRole, SysMLConstants.CONSTRAINT_PARAMETER_STEREOTYPE))
    				 { // The given value property is bound to a constraint parameter, return true. return true; }
    				return true;
    			}
    		}
    		// The given value property is not bound to a constraint parameter then return false.
    	}
    	return false;
    }
    
    
    /**
     * Check The given argument.
     * @param arg
     * @param propertyName
     */
    private boolean checkArgument(String arg, String propertyName) {
	    String start = propertyName + "=";
	    if (arg.startsWith(start)) {
	       String s = arg.substring(start.length());
	       System.setProperty(propertyName, s);
	       return true;
	    }
	    return false;
    }

    private  boolean isNamedElement(Object element) { 
    	if (element instanceof 
    			com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) { 
    		return true;
    	}
    	return false;
    }


	public  Object getTheProfileToBeProbed() {
		return theProfileToBeProbed;
	}


	public  void setTheProfileToBeProbed(String theProfileToBeProbed) {
		Utilities.theProfileToBeProbed = theProfileToBeProbed;
	}

	public String metricsReportText() {
		String rc = new String("");
		if(isOOSEM)  {
			Iterator<String> ii = null;
			if(metricsStereoHash.size() != 0) { 
				for(ii = metricsStereoHash.keySet().iterator(); ii.hasNext(); ) { 
					String stereoName = ii.next();
					Integer theMetric = metricsStereoHash.get(stereoName);				
					if( theMetric != null) { 
						rc += " " + stereoName+" "+theMetric.toString();
					}  
				}
			}
		} 
		return rc;
	}

 }
