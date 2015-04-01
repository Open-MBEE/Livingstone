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
 *    $Id: AccessingTWS.java 611 2012-09-12 09:07:23Z karo-se2 $
 *
*/


package org.eso.sdd.mbse.ls;


import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.uml2.impl.ElementsFactory;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.ci.persistence.decomposition.ProjectAttachmentConfiguration;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
// added by me.
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.modules.AutoLoadKind;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.runtime.ApplicationExitedException;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Abstraction;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eso.sdd.mbse.ls.validation.ValidationRuleHolderFactory;

import sun.util.calendar.BaseCalendar.Date;


import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.teamwork.application.VersionData;

import com.nomagic.magicdraw.metrics.MetricResult;
import com.nomagic.magicdraw.metrics.MetricsInformation;
import com.nomagic.magicdraw.metrics.MetricsManager;
import com.nomagic.magicdraw.metrics.MetricsSuite;
import com.nomagic.magicreport.helper.FileUtils; 
import com.nomagic.magicdraw.dependency.*;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.impl.OpaqueExpressionImpl;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.OpaqueBehavior;
 

public class Engine { 
    private static boolean Debug = true;
    private static HashMap<String,String> theProjectLabels = new HashMap();

	private Stereotype CMFConSte = null;
	private Stereotype CMFRelSte = null;
	private Stereotype CMFDomSte = null;
	private Stereotype CMFRanSte = null;
	private Stereotype CMFOntSte = null;
	
	private static ProjectDescriptor projectDescriptor;
	private static Project theOntoProject;

	
	private static Logger  logger = null;
	private static Logger  cLogger = null;
	private Utilities theUt = null;

	private Profile theCMFProfile;
	
	// data model relevant data structures
	private HashMap<String,String>  stereoToProfile      = new HashMap<String,String>(); // stereo to ontology mapping
	private HashMap<String,Element> stNameToElement      = new HashMap<String,Element>();
	private HashMap<String,String>  inheritsFrom         = new HashMap<String,String>(); // not used
	private HashMap<Package,String> packageToProfileProjectName = null; // the list of all packages which correspond to ontologies
	private HashMap<String,Vector<String>> dependsUpon   = new HashMap<String, Vector<String>>();
	
	private HashMap<String,List<String>> compositionConstraintHash = new HashMap<String,List<String>>();
	private HashMap<String,List<String>> relationshipConstraintHash = new HashMap<String,List<String>>();

	
	private Stereotype theStereoStereo;
	private Profile theSysMLProfile = null;
	private Profile theUMLProfile = null;
	
	private int constraintNumber = 0; // counter to be increased for each generated constraint.
	private int vrhNumber = 0;        // counter to be increased for each generated validation rule holder
	
	private Stereotype theValidationRuleStereo = null;
	private Vector<Stereotype> relCMFSteColl = null; // CMF stereotypes which apply to dependencies
	private String  ontologyBeingInspected = null; // the name of the currently inspected ontology
	private Vector<ProfileInfo> generatedProfiles = null; // 

	private ProjectsManager projectsManager = null;

	private String validationPackageName = "org.eso.sdd.mbse.ls.validation";
	private ValidationRuleHolderFactory vrhf = null;
	
	
	class ProfileInfo implements Comparable<ProfileInfo> { 
		Project p;
		String name;
		String fileName;
		Package pack;
		
		public ProfileInfo(String n) {
			name = n;
			fileName = n + ".mdzip";
		}

		public int compareTo(ProfileInfo pi) { 
			return getName().compareTo(pi.getName());
		}
		
		Project getProject() { 
			return p;
		}
		
		void setProject(Project t) {
			p = t;
		}
		
		String getName() {
			return name;
		}
		
		void setName(String n) {
			name = n;
		}
	}
    
    public Engine() {
    	String logFileName = "LivingstoneEngine.log";
    	String cLogFileName = "ValidationRuleList.txt";
    	theUt = new Utilities();
    	logger = Logger.getLogger("org.eso.sdd.mbse.ls.engine");
    	cLogger = Logger.getLogger("org.eso.sdd.mbse.ls.Constraints");
    	// This request is enabled, because WARN >= INFO.
    	try {
    		new File(logFileName).delete();
    		new File(logFileName).createNewFile();
    		logger.addAppender(new FileAppender(new PatternLayout(), logFileName));

    		new File(cLogFileName).delete();
    		new File(cLogFileName).createNewFile();
    		cLogger.addAppender(new FileAppender(new PatternLayout(), cLogFileName));
    		
    		
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	
    	 projectsManager = Application.getInstance().getProjectsManager();

    	packageToProfileProjectName = new HashMap<Package,String>();
    }
	
    public void run(Project theProject) { 
    	logger.info("********************");
    	logger.info("***** PHASE ONE ****");
    	logger.info("********************");
    	inspectOntologyProject(theProject);
    	logger.info("********************");
    	logger.info("***** PHASE TWO ****");
    	logger.info("********************");
    	createNeededProfiles();
    	logger.info("********************");
    	logger.info("***** PHASE THREE ****");
    	logger.info("********************");
		createAllStereotypes();
		logger.info("");
		logger.info("Generation completed.");
		logger.info("");

		
		logger.removeAllAppenders();
    	cLogger.removeAllAppenders();
    	cLogger.shutdown();
    	logger.shutdown();
		
    }

	

     
    public void inspectOntologyProject(Project theProject) {
    	String theCMFProfileName = Constants.cmfName;
    	Profile theProfile = StereotypesHelper.getProfile(theProject, theCMFProfileName);
    	theOntoProject = theProject;
    	// retrieve SysML Profile for later usage

    	
    	if(theProfile == null) { 
    		logger.error("calling method verification ineffective: profile " + theCMFProfileName + " is empty");
    		return;
    	}
    	//
    	theCMFProfile = theProfile;
    	
    	CMFConSte = StereotypesHelper.getStereotype(theProject, Constants.CMFCon, theProfile );
    	if(CMFConSte == null) { 
    		logger.error("Stereotype CMFConSte is empty");
    		return;
    	}

    	CMFRelSte = StereotypesHelper.getStereotype(theProject, Constants.CMFRel, theProfile );
    	if(CMFRelSte == null) { 
    		logger.error("Stereotype CMFRelSte is empty");
    		return;
    	}

    	CMFRanSte = StereotypesHelper.getStereotype(theProject, Constants.CMFRan, theProfile );
    	if(CMFRanSte == null) { 
    		logger.error("Stereotype CMFRanSte is empty");
    		return;
    	}

    	CMFDomSte = StereotypesHelper.getStereotype(theProject, Constants.CMFDom, theProfile );
    	if(CMFDomSte == null) { 
    		logger.error("Stereotype CMFDomSte is empty");
    		return;
    	}

    	CMFOntSte = StereotypesHelper.getStereotype(theProject, Constants.CMFOnt, theProfile );
    	if(CMFOntSte == null) { 
    		logger.error("Stereotype CMFOntSte is empty");
    		return;
    	}

    	retrieveSysMLProfile();
    	assessProjectModulesDependencies(theProject);

    	for(Package thePackage: packageToProfileProjectName.keySet()    ) { 
    		logger.info("");
    		logger.info("** SEEKING ALL CMF.CONCEPTS IN "+thePackage.getName());
    		ontologyBeingInspected = thePackage.getName();
    		logger.info("");    				
    		recurse(thePackage);
    	} 
    }

	public void assessProjectModulesDependencies(Project theProject) { 
		if(theProject.getName() == null) {
			logger.error("assessProjectModulesDependencies: Project does not have a name! ERROR");
			return;
		}
		
		for(IAttachedProject theModule: ProjectUtilities.getAllAttachedProjects(theProject)) {
			logger.debug("Checking module: " + theModule.getName());
			if(theModule.getName().contains("Ontolog")) { // notice that we need to catch plural and singular!
				addDependency(theProject.getName(),theModule.getName());
				addDependency(o2p(theProject.getName()),o2p(theModule.getName()));				

				final Collection<Package> sharedPackages =
						ProjectUtilities.getSharedPackages(theModule);
				for(Package thePackage: sharedPackages) { 
					if(isCMFOntology(thePackage)) { 
						logger.info("Adding ontology " + thePackage.getName() + " ["+theModule.getName() + "],to "+theProject.getName());
						packageToProfileProjectName.put(thePackage,o2p(theModule.getName()));
					} else {
						logger.info(thePackage.getName() + " is not Ontology. Trying with lower level.");
						for(Element el:thePackage.getOwnedElement()) { 
							if(el instanceof Package && isCMFOntology((Package)el)) {
								Package thePack2 = (Package)el;
								logger.info("\tAdding ontology " + thePack2.getName() + " ["+theModule.getName() + "],to "+theProject.getName());
								packageToProfileProjectName.put(thePack2, o2p(theModule.getName()));
							}
						}
					}
					//System.out.println("\t==> "+thePackage.getName());
				}
			}
		}
		System.out.flush();
		// now we search for all top-level Packages which are shared in this very same project
		for(Element theEle: theProject.getModel().getOwnedElement()) { 
			if(theEle instanceof Package && ProjectUtilities.getAttachedProject(theEle) == null) { 
				Package thePack = (Package)theEle;
				if(isCMFOntology(thePack)) {
					logger.info("Adding ontology (Package) " + thePack.getName() + " [ (Project) "+theProject.getName() + "],to (Project) "+theProject.getName());
					packageToProfileProjectName.put(thePack, o2p(thePack.getName()));
				} else {
					logger.info(thePack.getName() + " is not Ontology. Skipping.");
				}
			}
		}
		

		addDependency("FoundationalProfile","UMLSysMLProfile");
		addDependency("FoundationalOntology","UMLSysMLOntology");
		// verification
		// @TODO: this kind of analysis should be used recursively throughout all the modules to determine
		// the dependency chain and hence the order of profile generation. At the moment this is only hard-coded
		logger.info("\n\nDependency verification\n");
		for(String pName:  dependsUpon.keySet() ) { 
			logger.info(pName+":");
			for(Iterator<String> jj = dependsUpon.get(pName).iterator(); jj.hasNext(); ) { 
				logger.info("\t"+jj.next());
			}
			logger.info("");
		}
		
	}

	private void  recurse(Element el) {
    	if(el instanceof NamedElement) {
    		//logger.info(" (" + ((NamedElement)el).getName()+")");
    	} else {
    		//logger.info("");
    	}
    	// make computations here

    	processElement(el);
    	if(el.hasOwnedElement()) { 
    		
    		for(Iterator<Element> it = el.getOwnedElement().iterator(); it.hasNext(); ) { 
    			Element ownedElement = it.next();
    			recurse(ownedElement);
    		}
    	} else {
    		//logger.info("*** " + el.getHumanName() + " TERMINATION CONDITION");
    		//termination condition
    	}
     }
	
	private void processElement(Element el) {
		String ancestryInfo = "";
    	if(isCMFConcept(el) ) {
    		Element ancestor = traverseToAncestor(el);
    		
    		if(ancestor != el) { 
    			ancestryInfo = " ( "+ ancestor.getHumanName() + " )";
    		}
    		logger.info("");
    		logger.info(el.getHumanName() + " is a CMF Concept" + ancestryInfo);
    		if(el instanceof NamedElement) { 
    			NamedElement ne = (NamedElement)el;

    			stereoToProfile.put(ne.getName(),ontologyBeingInspected);
    			stNameToElement.put(ne.getName(),el);

    			listDependencies(el);
    			listGeneralizations(el);
    		} else {
    			logger.error("CMF Concept is not a named element. FAILURE. ");
    			return;
    		}
    	} 
    }


    
    public void createAllStereotypes() {
    	logger.info("####");
    	logger.info("#### CREATING ALL STEREOTYPES ######");
    	logger.info("####");
    	
    	for( ProfileInfo gpInfo : generatedProfiles ) { 
    		// Generated Profile Info
    		String plugInDirectory = validationPackageName+".validationrules."+gpInfo.getName();
    		vrhf = new ValidationRuleHolderFactory(plugInDirectory, logger);    	

    		logger.info("");
    		logger.info(" **** WORKING ON PROFILE "+ gpInfo.getName());
    		logger.info("");
    		cLogger.info("# "+gpInfo.getName());
    		theValidationRuleStereo = StereotypesHelper.getStereotype(gpInfo.getProject(), "validationRule");
    		
    		if(theValidationRuleStereo == null) { 
    			logger.error("CANNOT FIND A STEREOTYPE FOR VALIDATION RULE");
    		}
    		// before creating any stereotype the module dependencies need to be refreshed.
    		//  projectsManager.r
    		refreshModuleUsage(gpInfo.getProject());
    		
    		
    		if (!SessionManager.getInstance().isSessionCreated()) {
    			SessionManager.getInstance().createSession("LivingstoneCreatingStereotypes-"+gpInfo.getName());

    			for ( String theStereoName: stereoToProfile.keySet()) { 
    				// check if the stereotype needs to be created in this profile, or in another one.
    				if(o2p(stereoToProfile.get(theStereoName)).equals(gpInfo.getName() )) {
						createStereotype(gpInfo.getProject(), theStereoName);
						logger.info("");
    				} 
    			}
    			SessionManager.getInstance().closeSession();
    			projectsManager.saveProject(ProjectDescriptorsFactory.getDescriptorForProject(gpInfo.getProject()),true);
    		} else {
    			logger.error("Modification session already running");
    		}
    		vrhf.createValidationRulePluginClass("VRPlugin");
    		vrhf.addFilesToJar(plugInDirectory+File.separatorChar+gpInfo.getName()+"ValidationRules.jar");
    		createPluginXMLFile(plugInDirectory,gpInfo.getName()+"ValidationRules.jar","VRPlugin");
    		
    		
    	}
    }
    
	private void createPluginXMLFile(String plugInDirectory, String jarFileName, String className) {
		// TODO Auto-generated method stub
		String pluginXmlName = "plugin.xml";
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<plugin\nid=\""+plugInDirectory+"\"\n" +
				"name=\""+plugInDirectory+".ValidationSuite\" version=\"1.0\"	provider-name=\"ESO DOE SDD\"\n" +
				"class=\""+plugInDirectory+"."+className+"\">\n" +
				"<requires>\n			   <api version=\"1.0\"/>\n	</requires>\n<runtime>\n<library name=\""+jarFileName+"\"/>" + 
				"</runtime>\n</plugin>\n";

		FileWriter fw = null;
		if(! new File(plugInDirectory).exists()) {
			logger.error("Directory: " + plugInDirectory+ " does not exist.");
			return;
		}
		File theXMLFile = new File(plugInDirectory+File.separatorChar+pluginXmlName);
		if(theXMLFile.exists()) { 
			logger.warn("The file: " + pluginXmlName + " already exists.");
			return;
		}
		
		try {
			fw = new FileWriter(theXMLFile);
			fw.write(content);

			fw.close();
		} catch(IOException ioe) { 
			ioe.printStackTrace();
			logger.error("Could not write to "+pluginXmlName);
			return;
		}
		
		
		
	}





	private void listGeneralizations(Element el) { 
   		 for(Relationship rel:el.get_relationshipOfRelatedElement() ) {
   			 if(rel.getHumanType().equals("Generalization")) {
   				 Generalization theGen = (Generalization)rel;
   				 NamedElement theOtherEnd = null;
   				 // we not know at which end of the generalization we are:
   				 if(theGen.getGeneral().equals(el)) { 
   					 theOtherEnd = theGen.getSpecific();
   				 } else {
   					 theOtherEnd = theGen.getGeneral();
   	   				 inheritsFrom.put(((NamedElement)el).getName(), theOtherEnd.getName());
   				 }
   				 //System.out.println("\t\t"+rel.getHumanName() + " " + theOtherEnd.getName());
   			 }
   		 }
	}
	
	private void listDependencies(Element el) { 
		NamedElement ne = (NamedElement)el;
		
		if(ne.hasSupplierDependency()) { 
			for(Dependency theDep: ne.getSupplierDependency()) {
				for(NamedElement client:theDep.getClient()) { 
					if(StereotypesHelper.hasStereotypeOrDerived(client, CMFRelSte)  ) { 
						String ancestorName = null;
						String pAncestorName = null;
						Element clientAncestor = traverseToAncestor(client);
						if(clientAncestor instanceof NamedElement) { 
							ancestorName = ((NamedElement)clientAncestor).getName();
						} else {
							ancestorName = clientAncestor.getHumanName();
						}
						NamedElement theOtherEnd = getCMFRelationOtherEnd(client,ne);
						pAncestorName = processMCName(ancestorName);
						logger.info("\t "+ theDep.getHumanName() + " <-> " 
								+ client.getName() 	+ " (" + ancestorName + ") " 
								+ ((theOtherEnd!=null)?"<->" + theOtherEnd.getName():"" ));
						// we just need to handle one case
						if(isCMFDomain(theDep)) { 
							logger.info("==> "+ ne.getName()+ " has one: "+ pAncestorName +
										", of type: "+((theOtherEnd!=null)?theOtherEnd.getName():"" ) );
							if(theOtherEnd != null) { 
								String pName = null;
								Object familyOfClientAncestor = discoverFamilyOfElement(clientAncestor);
								
								if (isUMLRootConcept_Element(familyOfClientAncestor) || 
										pAncestorName.startsWith("A_")) {
								//if(isCMFConcept(clientAncestor)) {
									logger.info("\t"+ancestorName + " is a UML Root Element");
									pName = extractPropertyNameFromUMLSpec(pAncestorName);
									logger.info("Would log constraint info (ATTR) for: "+ pName + " ( " +ne.getName() + ")");									
									logInfoForAttributeConstraint(ne.getName(),pName,theOtherEnd.getName());							
								} else {
									logger.info("\t"+ancestorName + " is not a UML Root Element");
									if (isUMLRootConcept_Relationship(familyOfClientAncestor)) {
										//if(isCMFRelation(clientAncestor)) {
										logger.info("\t"+ancestorName + " is a UML Root Relation");
										if(ancestorName.startsWith("UML"))  {
											pName = extractPropertyNameFromUMLSpec(pAncestorName);
											logger.info("Would log constraint info (REL) for: "+ pName + " ( " +ne.getName() + ")");
											logInfoForRelationshipConstraint(ne.getName(),pName,theOtherEnd.getName());																
										} else {
											logger.info("Will not log constraint for "+ancestorName+ " because it is not UML");
										}
									} else {
										logger.info("\t"+ancestorName + " and is not a UML Root Relation");									
										logger.info("\tFIX YOUR ONOTOLOGY: "+ancestorName + " is neither Element nor Relationship!!" );												
									}
								}
							} else {
								logger.error("Empty other end for " + ne.getName());
							}
						}
					} else {
						//System.out.println("Discarding dependency: " + theDep.getHumanName());
					}
				}
			} // end loop over supplier dependencies
		}
	}

	// A_type_typedElement
	private String extractPropertyNameFromUMLSpec(String theSpec) {
		String retVal = theSpec;
		if(theSpec.matches(".*_.*_.*")) {
			int st = theSpec.indexOf('_')+1;
			int en = theSpec.lastIndexOf('_');
			retVal = theSpec.substring(st, en).replace("owned","");
		} else {
			logger.info("No match for "+theSpec);
		}
		return retVal;
	}
	
	/* 
	 * for an element stereotyped with 'stereoName', it shall "contain" an element of type
	 * 	'subPart', which is stereotyped by 'partType' 
	 */
	
	private void logInfoForAttributeConstraint(String stereoName, String subPart, String partType) {
		// TODO Auto-generated method stub
		List<String> v = null;
		String msg = stereoName+" "+subPart+":"+partType;
		logger.setLevel(Level.INFO);
		logger.debug("logging information for constraint: " +msg );
		if(compositionConstraintHash.containsKey(stereoName)) { 
			v = compositionConstraintHash.get(stereoName);
			v.add(subPart+":"+partType);
			logger.debug("** Added for "+msg);
		} else {
			v = new ArrayList<String>();
			v.add(subPart+":"+partType);
			logger.debug("** Initialized/Added for "+msg);
			compositionConstraintHash.put(stereoName, v);
		}
	}

	/* 
	 * for an element stereotyped with 'stereoName', it shall "contain" an relationship of type
	 * 	'relation', which has a target which is stereotyped by 'partType' 
	 */
	
	private void logInfoForRelationshipConstraint(String stereoName, String relation, String partType) {
		List<String> v = null;
		String msg = stereoName+" "+relation+":"+partType;
		logger.setLevel(Level.INFO);
		logger.debug("logging information for constraint: " +msg );
		if(relationshipConstraintHash.containsKey(stereoName)) { 
			v = relationshipConstraintHash.get(stereoName);
			v.add(relation+":"+partType);
			logger.debug("** Added for "+msg);
		} else {
			v = new ArrayList<String>();
			v.add(relation+":"+partType);
			logger.debug("** Initialized/Added for "+msg);
			relationshipConstraintHash.put(stereoName, v);
		}
	}

	


	private NamedElement getCMFRelationOtherEnd(NamedElement theRelation, NamedElement theOrigin) {
		NamedElement retNE = null;
		if(theRelation.hasClientDependency()) { 
			for(Dependency theDep: theRelation.getClientDependency() ) {
				//if(StereotypesHelper.hasStereotype(theDep,relCMFSteColl));
				for(NamedElement theSupplier: theDep.getSupplier()) {  
					if( theSupplier != theOrigin ) {
						 retNE = theSupplier;
					}
				}
			}
		} else {
			logger.error("The Relation "+theRelation.getName() + " has no cient dependencies???");
		}
		return retNE;
	}
	
	private List<NamedElement> findCMFRangedElement(NamedElement theOrigin) {
		Vector<NamedElement> theColl = new Vector<NamedElement>();
		if(theOrigin.hasClientDependency()) { 
			for(Dependency theDep: theOrigin.getClientDependency() ) {
				//System.out.println("FCMFRE: " + theDep.getHumanName());
				if(StereotypesHelper.hasStereotypeOrDerived(theDep, CMFRanSte)  ) { 
					for(NamedElement one:theDep.getSupplier() ) { 
						theColl.add(one);
					}		
				}
			}	
		} else {
			System.out.println("FCMFRE: " + theOrigin.getHumanName() + " has not client dependencies");			
		}
		return theColl;
	}
	
	
	/*
	 *
	 * 	 1  create a Validation Rule Holder via BCEL
		 2 loop through all logged stereotype information
		 3 create constraints with appropriate methods

	     This is supposed to be called only once per each stereotype

	 */
	
	private void setupValidationForStereotype(Project createInProject, Stereotype theStereo) {
		String stereoName = theStereo.getName();
		String metaClass = getMetaClassFromStereotype(theStereo).elementAt(0).getName();
		List<String> v = null;
		List<String> w = null;		
		
		// composition rules (Block Template)
		
		if(compositionConstraintHash.containsKey(stereoName)  || 
				relationshipConstraintHash.containsKey(stereoName)  ) {
			String vrhName = "VRH"+vrhNumber++;
			// we create a Validation Rule Holder
			
			
			v = compositionConstraintHash.get(stereoName);
			w = relationshipConstraintHash.get(stereoName);
			vrhf.createBlockValidationRuleHolder(vrhName,metaClass, constraintNumber, theStereo.getProfile().getName(),v,w);
			
			if( v != null && ! v.isEmpty()) {
				logger.info(stereoName + " has " + v.size() + " composition keys.");
				for(Iterator<String> it = v.iterator(); it.hasNext(); ) {
					String pair = it.next();
					if(pair.lastIndexOf(':') == -1) {
						logger.error("BAD FORMAT (missing colon):  " + pair);
						continue;
					}
					String s = pair.substring(0, pair.lastIndexOf(':'));
					if(s.contains(" ")) { 
						logger.warn("Removing space from: " + s + " FIX YOUR ONTOLOGY!!");
						s = s.replace(" ", "");
					}
					String type = pair.substring( pair.lastIndexOf(':')+1, pair.length());

					String vrName = "BVR" + constraintNumber++;
					String methodName = "hasInnerElement_" +s+"_"+type;
					String errorMessage = " the element is missing a: "+s+" whose type is stereotyped by:" + type;
					logger.info("Adding Constraint "+vrName+ " => Class:"+vrhName+ " Method: "+methodName + " to stereo "+theStereo.getName());
					addValidationRuleToStereotype(createInProject,theStereo,vrhf.getPackageName(), vrName, vrhName,methodName, errorMessage);
					cLogger.info(vrName+","+vrhName+","+theStereo.getName()+","+methodName);
				}
			}
			if( w != null && ! w.isEmpty()) {
				logger.info(stereoName + " has " + w.size() + " relationship keys.");
				for(Iterator<String> it = w.iterator(); it.hasNext(); ) {
					String pair = it.next();
					String s = pair.substring(0, pair.lastIndexOf(':'));
					String type = pair.substring( pair.lastIndexOf(':')+1, pair.length());

					String vrName = "BVR" + constraintNumber++;
					String methodName = "hasInnerRelationship_" +s+"_"+type;
					String errorMessage = " the element is missing a relationship: "+s+" whose target is stereotyped by:" + type;
					logger.info("Adding Constraint "+vrName+ " => Class:"+vrhName+ " Method: "+methodName + " to stereo "+theStereo.getName());
					addValidationRuleToStereotype(createInProject,theStereo,vrhf.getPackageName(), vrName, vrhName,methodName, errorMessage);
					cLogger.info(vrName+","+vrhName+","+theStereo.getName()+","+methodName);
				}
			}

		} else {
			logger.info("No composition constraint nor relationship constraint for stereotype: "+theStereo.getName()+ ". no validation will be created.");
		}
		
		// association rules (Junction Template)

		
		
	}
	
	private void addValidationRuleToStereotype(Project theGeneratedprofile, Stereotype theStereo, String thePackage, String theVRName,  String theClass, String theMethod, String errorMessage) { 
		String language = "Binary";
		String body = thePackage+"."+theClass+"."+theMethod;
		List<String> languages = null;
		List<String> bodies = null;

		Constraint theConstr = null;
		ElementsFactory factory = theGeneratedprofile.getElementsFactory();

		// determining the constraint, setting names and stereotypes
		theConstr = factory.createConstraintInstance();
		theConstr.setName(theVRName);

		OpaqueExpression theOEI = factory.createOpaqueExpressionInstance();
		
		// set specification to opaque expression
		theConstr.setSpecification(theOEI);

		// set language
		languages = theOEI.getLanguage();
		languages.clear();
		languages.add(language);

		// set body
		bodies = theOEI.getBody();		
		bodies.clear();
		bodies.add(body);
		
		//logger.info("FOUND OE:" + theOEI.getClass().getName());
		StereotypesHelper.addStereotype(theConstr, theValidationRuleStereo);
		StereotypesHelper.setStereotypePropertyValue(theConstr, 
					theValidationRuleStereo, "abbreviation", theConstr.getName());
		StereotypesHelper.setStereotypePropertyValue(theConstr, 
					theValidationRuleStereo, "errorMessage", errorMessage);
		
		List<Element> theCEs = theConstr.getConstrainedElement();
		theCEs.clear();
		theCEs.add(theStereo);
			// adding the constraint to the stereotype
	
		//logger.info("Would try to add "+theConstr.getName()+ " to " + theStereo.getName());
		try { 
			ModelElementsManager.getInstance().addElement(theConstr,theStereo);
			//ModelElementsManager.getInstance().addElement(theOEI   ,theConstr);
		} catch(Exception e) {
			logger.error(e.toString());
		}
	}

	private String processMCName(String mcName) {
		String retVal = mcName;
		if(mcName.startsWith("SysML ")) { 
			retVal = retVal.replace("SysML ","");
		}
		if(mcName.startsWith("UML ")) { 
			retVal = retVal.replace("UML ","");
		}
		return retVal;
	}
	
	private String getNextConstraintName() {
		return "E-TI-" + String.valueOf(constraintNumber++);
	}




	private void createStereotype(Project createInProject, String conceptName) { 
		//theProfile.
		Package theDestPack = null;
		String metaClassName = null;
		String metaClassTargetName = null;
		String theProfileName = null;
		String stereoName = null;
		Class metaClass = null;
		ElementsFactory factory = null;
		Vector<Class> theMClasses = new Vector<Class>();
		
		Vector<Stereotype> theGenStereotypes = new Vector<Stereotype>();
		Vector<Element> theGenerals = null;
		
		Stereotype theGenStereo = null;
		Element cmfConcept = null; 
		Profile theTargetProfile = null;
		
		
		stereoName = processMCName(conceptName);
		if(! stNameToElement.containsKey(conceptName)) { 
			logger.error("No Element stored for \""+ conceptName + "\" in association hash. Bailing out.\n");
			return;
		}
		
		
		if(! isPendingStereotype(conceptName) ) { 
			logger.error("Asked to create stereotype ("+conceptName+") for which no Profile was logged. Bailing out.\n");
			return;
		}
		theProfileName = o2p(stereoToProfile.get(conceptName));
		
		// check if for this requested stereotype name a metaclass exists
		if(StereotypesHelper.getMetaClassByName(createInProject, stereoName) != null   ) { 
			logger.info("\""+stereoName +"\" is a metaclass, no need to create a stereotype for it.");
			return;
		}
		if( isSysMLStereotype( stereoName ) ) { 
			logger.info("Stereotype \""+ conceptName + "\" appears to be SysML, skipping");
			return;
		}
		
		if( isUMLStereotype( stereoName ) ) { 
			logger.info("Stereotype \""+ conceptName + "\" appears to be UML, skipping");
			return;
		}
		
		logger.info("\nCreating \""+ stereoName + "\" stereotype in package "+ theProfileName );

		// from the given project, we select the top-level package which is also a Profile and
		// has the corresponding name. This will be the Profile we add the Stereotype to (theTargetProfile).
		
		for(Iterator<Element> ii = createInProject.getModel().getOwnedElement().iterator(); ii.hasNext(); ) { 
			Element ele = ii.next();
			if(ele instanceof Profile) { 
				theTargetProfile = (Profile)ele;
				if(theTargetProfile.getName().equals(theProfileName)) { 
					break;
				}
				//System.out.println("Found a profile:" + theGreekProfile.getName());
			}
		}
		if(theTargetProfile == null) { 
			logger.error("Could not find any suitable profile to add stereotype \""+stereoName + "\". Bailing out\n");
			return;
		}
		
		// check if the stereotype happens to exist already (recursion termination condition)
		if(StereotypesHelper.getStereotype(createInProject, conceptName,theTargetProfile) != null) { 
			logger.info("The stereotype \""+conceptName+"\" appears to have been created already");
			return;
		}

		factory = createInProject.getElementsFactory();
		
		// all info available, now apply decision algorithm
		// get first level non abstract generalizations
		theGenerals = getFirstLevelGeneralizations(stNameToElement.get(conceptName));	
		
		

		if(theGenerals.size() == 0) {
			logger.info("No generalization availabel for this stereotype. Bailing out.");
			if (StereotypesHelper.getMetaClassByName(createInProject, stereoName) == null)
				logger.info("Cannot create stereotype \""+ stereoName +"\", it has no generalization, and no metaclass exists for it. Bailing out.\n");
			return;
		}
		
		logger.info("stereotype \""+stereoName + "\" has "+theGenerals.size()+" generalization(s).\n");

		//  theMCClasses vector is filled up recursively
		for(Element theEl: theGenerals) { 
			if(theEl instanceof NamedElement) { 
				delegateStereoCreation(createInProject, theTargetProfile, theEl, theMClasses, theGenStereotypes);
			} else {
				logger.error("Cannot create stereotype \""+ stereoName +"\" it specializes a single element which is not a Named Element\n");
				return;
			}		
		}
			


		String theClassList = "[";
		for(Class clazz: theMClasses) { 
			theClassList += clazz.getName()+ " ";
		}
		theClassList += "]";
		logger.info("Got "+theMClasses.size()+ " metaclasses for stereotype \""+stereoName+"\"" + theClassList);

		
		if(theMClasses.size() == 0 ) { 
			logger.error("No metaclasses available for " + stereoName + ".Bailing out.\n");
			return;
		}
		
		// we chose the first one only to determine the package which will act as container
		// for this stereotype
	
		
		metaClass = theMClasses.elementAt(0);
		
		
		if(metaClass == null) {
			// failure
			logger.info("*** Cannot create stereotype \""+ stereoName+ "\" : empty metaclass! (" +metaClassTargetName+")\n" );
			return;
		} else {
			metaClassName = metaClass.getName();
			theDestPack = providePackageToHostStereotype(metaClassName,  theTargetProfile, factory);
		}			
		

		//Stereotype theNewStereo = factory.createStereotypeInstance();
		Stereotype theNewStereo = StereotypesHelper.createStereotype(theTargetProfile,
				stereoName, theMClasses);   // Arrays.asList(metaClass));


		// here we make the newly created stereotype specialize the generic one identified by recursion
		for(Stereotype tgs: theGenStereotypes) { 
			if(tgs != null) { 
				addGeneralization(theNewStereo, tgs,createInProject);
			} else {
				logger.error("Empty Generalization stereotype returned in Vector by delegateStereoCreation");
			}
		}
		
		theNewStereo.setAbstract( isAbstract(stNameToElement.get(conceptName)) );	

		
		try {
			ModelElementsManager.getInstance().addElement(theNewStereo,theDestPack);
		} catch (ReadOnlyElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Created stereotype \""+stereoName+"\".");
		//logger.info("\tBase Class of created Stereotype: "+StereotypesHelper.getBaseClasses(theNewStereo).iterator().next().getName());
		setupValidationForStereotype(createInProject, theNewStereo);
		
	}
	

	private Package providePackageToHostStereotype(String mcName, Profile thePro, ElementsFactory factory) { 
		Package theDestPack = null;
		for( Element el :thePro.getOwnedElement() ) { 
			if((el instanceof Package) && ((NamedElement)el).getName().equals(mcName+" Stereotypes")) { 
				theDestPack = (Package)el;
			}
		}	

		if(theDestPack == null) { 
			theDestPack = factory.createPackageInstance();
			theDestPack.setName(mcName+" Stereotypes");
			try {
				ModelElementsManager.getInstance().addElement(theDestPack,thePro);
			} catch (ReadOnlyElementException e) {
				e.printStackTrace();
			}
		}
		return theDestPack;
	}


	/* @params: theEl is the element which has been identified as a generalization of a cmf.concept
	 * This method may return a non null Stereotype, but it may also be null.
	 * the MetaClasses (theMClasses) will be used to construct the stereotype one level up in the recursion.
	 * 
	 * 
	 */

	private void delegateStereoCreation(Project createInProject,
			Profile theTargetProfile, Element theEl, Vector<Class> tMetaC, Vector<Stereotype> tgStereo) {
		Class metaClass;
		Stereotype theGenStereo;
		String theGenName;
		theGenName   = processMCName(((NamedElement)theEl).getName());
		theGenStereo = StereotypesHelper.getStereotype(createInProject, theGenName);
		
		if(theGenStereo != null) {
			// if the stereotype exists then we go only one level down in the recursion
			tMetaC.addAll(getMetaClassFromStereotype(theGenStereo));
			logger.info("\tseems to have a stereotype for its \""+theGenName + "\" generalization ("
					+ tMetaC.size()+")");					

			for(Iterator<Class> ii =  tMetaC.iterator(); ii.hasNext(); ) { 
				metaClass = ii.next();
				logger.info("\t\t"+metaClass.getName());
			}
		} else {
			logger.info("\tbut there exists no model stereotype for \""+theGenName + "\" generalization");
			// proceed to recursion if applicable, the desired stereotype must be in the list of stereos to be
			// created for this profile.
			
			if(StereotypesHelper.getMetaClassByName(createInProject, theGenName) != null) {			
				logger.info("\tbut a meta class exists for \""+theGenName+"\"");
				tMetaC.add(StereotypesHelper.getMetaClassByName(createInProject, theGenName));
			} else {
				if(isPendingStereotype(theGenName) ) {				
					logger.info("\tand it appears that \""+theGenName+"\" is pending creation ");
					if( o2p(stereoToProfile.get(theGenName)).equals(theTargetProfile.getName() ) ) { 
						logger.info("\tin this profile,so I'll create it.");
						
						createStereotype(createInProject,theGenName);
						
						theGenStereo = StereotypesHelper.getStereotype(createInProject, theGenName,theTargetProfile);
						// code repetition, should be factored out
						tMetaC = getMetaClassFromStereotype(theGenStereo);
						logger.info("\tNow, seems to have a stereotype for its \""+theGenName + "\" generalization ("
								+ tMetaC.size()+")");					

						for(Iterator<Class> ii =  tMetaC.iterator(); ii.hasNext(); ) { 
							metaClass = ii.next();
							logger.info("\t\t"+metaClass.getName());
						}
					} else {
						logger.info("\tbut not in this profile ("+stereoToProfile.get(theGenName)+"), too bad.");							
					}
				} else {
					logger.info("\tbut \""+theGenName+"\" is not pending creation");
				}
			}
		}
		if(theGenStereo != null) { 
			tgStereo.add(theGenStereo);
		}
	}
	
	
	/*
	 * convenience method to add a generalization between two stereotypes
	 * 
	 */
	private void addGeneralization(Stereotype theSpecialized, Stereotype theGeneral, Project theProject) {
		// assumes session already open.
		ElementsFactory factory = theProject.getElementsFactory();
		Generalization g = factory.createGeneralizationInstance();
		
		g.setGeneral(theGeneral);
		g.setSpecific(theSpecialized);
		try { 
			ModelElementsManager.getInstance().addElement(g,theSpecialized);
		} catch (ReadOnlyElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}




	private Vector<Class> getMetaClassFromStereotype(Stereotype s) { 
		Vector<Class> tmc = new Vector<Class>();
		for(Class theClass: StereotypesHelper.getBaseClasses(s)) { 
			tmc.add(theClass);
		}
		return tmc; 
	}
	
	private void retrieveSysMLProfile() {
    	theSysMLProfile = StereotypesHelper.getProfile(theOntoProject, "SysML");
    	if(theSysMLProfile == null) { 
    		logger.error("Cannot find the SysML Profile in project "+theOntoProject.getName());
    	}

    	theUMLProfile = StereotypesHelper.getProfile(theOntoProject, "UML Standard Profile");
    	if(theUMLProfile == null) { 
    		logger.error("Cannot find the UML Profile in project "+theOntoProject.getName());
    	}
    }

    	
	
	/*
	 * this recursive method navigates up following more general elements but 
	 * ignoring abstract classifiers.
	 * Notice that if more than one generalization exists, the first one is returned.
	 */
	
	
	private Element traverseToAncestor(Element el) {
		NamedElement ne = null;
		if(el instanceof NamedElement) { 
			ne = (NamedElement)el;
		} else {
			logger.error("traverseToAncestor: element " + el.getHumanName() + " is not a named element");
			return el;
		}
		if(isPendingStereotype(ne.getName())) {
			logger.info("\nAncestor search returned pending stereotype for: " + ne.getName());
			return el;
		}
		// should the element not have any relationship we return the element itself (= recursion end)
		
		if(el.has_relationshipOfRelatedElement()) {
			for(Relationship rel:el.get_relationshipOfRelatedElement() ) {
	   			 if(rel.getHumanType().equals("Generalization")) {
	   				 Generalization theGen = (Generalization)rel;
	   				 NamedElement theOtherEnd = null;
	   				 if(theGen.getSpecific().equals(el)) { 
	   					 theOtherEnd = theGen.getGeneral();
	   					 if(theOtherEnd instanceof Classifier && ((Classifier)theOtherEnd).isAbstract()) { 
	   							 continue; 
	   					 }
	   					 // recurse if the generalization is not abstract
	   					 return traverseToAncestor(theOtherEnd);
	   				 } // else this element is at the general side, then we do not care.
	   			 }
			} 
		} 
		
		return el;
	}
	
	private Element traverseFromElementToRootConcept (Element el) {
		if(el.has_relationshipOfRelatedElement()) {
			for(Relationship rel:el.get_relationshipOfRelatedElement() ) {
	   			 if(rel.getHumanType().equals("Generalization")) {
	   				 Generalization theGen = (Generalization)rel;
	   				 NamedElement theOtherEnd = null;
	   				 if(theGen.getSpecific().equals(el)) { 
	   					 theOtherEnd = theGen.getGeneral();
	   					 //if(theOtherEnd instanceof Classifier && ((Classifier)theOtherEnd).isAbstract()) { 
	   					//		 continue; 
	   					 //}
	   					 // recurse if the generalization is not abstract
	   					 return traverseFromElementToRootConcept(theOtherEnd);
	   				 }
	   			 }
			} 
		} 
		return el;
	}
	
	
	// return all first level generalizations which are not abstract
	private Vector<Element> getFirstLevelGeneralizations(Element el) {
		Vector<Element> theGenerals = new Vector<Element>();
		if(el.has_relationshipOfRelatedElement()) {
			for(Relationship rel:el.get_relationshipOfRelatedElement() ) {
	   			 if(rel.getHumanType().equals("Generalization")) {
	   				 Generalization theGen = (Generalization)rel;
	   				 NamedElement theOtherEnd = null;
	   				 // we not know at which end of the generalization we are:
	   				 if(theGen.getSpecific().equals(el)) { 
	   					 theOtherEnd = theGen.getGeneral();
	   					 theGenerals.add(theOtherEnd);
	   				 }
	   			 }
			} 
		} 
		return theGenerals;
	}
	
	/*
	 * this merely adds an entry to a vector listing the expected module dependencies
	 * for a project.
	 */
	
	private void addDependency(String theProject, String theModule) {
		if(dependsUpon.containsKey(theProject)) { 
			if(! dependsUpon.get(theProject).contains(theModule)) {   // .add(theModule);
				dependsUpon.get(theProject).add(theModule);
				logger.info("Added module dependency ("+theModule+") to hash "+theProject);
			}
		} else {
			Vector<String> theDependees = new Vector<String>();
			theDependees.add(theModule);
			dependsUpon.put(theProject,theDependees);
			logger.info("Added module dependency ("+theModule+") to hash "+theProject);
		}
	}
	
	private boolean isPendingStereotype(String stereoName) { 
		//if(toBeCreated.contains())
		if(stereoToProfile.containsKey(stereoName)) { 
			return true;
		}
		return false;
	}
	
	
	/*
	 *  this method takes care of filling a hashtable for the
	 *  MagicDraw projects which will have to be generated,
	 *  then hands over the generation to createProfileProject() method
	 * 
	 * for the moment I can just as well hard-code the fact that the 
	 * ontology depends on the SysMLUMLOntology and on the 
	 * Foundational ontology.
	 * 
	 */
	
	
	public void createNeededProfiles() {
		ProfileInfo theInterface = null;
		List<ProfileInfo> gpFound = new Vector<ProfileInfo>();
		HashSet<String> llop = new HashSet<String>();
		generatedProfiles = new Vector<ProfileInfo>();
		logger.info("");
		logger.info("\t*** CREATING PROFILE PROJECTS ***");
		logger.info("");
		
		// UMLSysML Ontology first
		for( Package thePackage: packageToProfileProjectName.keySet() ) {
			ProfileInfo pInfo = new ProfileInfo(o2p(thePackage.getName()));
			String theProjectName = packageToProfileProjectName.get(thePackage);
			if(theProjectName.contains("SysML")) { 
				pInfo.setProject(createProfileProject(theProjectName,pInfo.getName()));
				generatedProfiles.add(pInfo);
				llop.add(theProjectName);
				break;
			}
		}
	
		// Foundational after
		for(Package thePackage: packageToProfileProjectName.keySet()  ) {
			ProfileInfo pInfo = new ProfileInfo(o2p(thePackage.getName()));
			String theProjectName = packageToProfileProjectName.get(thePackage);
			if(theProjectName.contains("Foundational")) { 
				pInfo.setProject(createProfileProject(theProjectName,pInfo.getName()));
				gpFound.add(pInfo);
				llop.add(theProjectName);				
				if(pInfo.getName().startsWith("Interface")) { 
					theInterface = pInfo;
				}
			}
		}

		// forcedly putting the InterfaceProfile before the others in the same Profile Project
		// since other profiles in the same project need it.
		if(theInterface != null) { 
			gpFound.remove(theInterface);
			gpFound.add(0,theInterface);
		}
		
		generatedProfiles.addAll(gpFound);

		
		// anything else afterwards
		for(Package thePackage: packageToProfileProjectName.keySet() ) {
			ProfileInfo pInfo = new ProfileInfo(o2p(thePackage.getName()));
			String theProjectName = packageToProfileProjectName.get(thePackage);
			if(! theProjectName.contains("Foundational") && ! theProjectName.contains("SysML")) { 
				pInfo.setProject(createProfileProject(theProjectName,pInfo.getName()));
				llop.add(theProjectName);				
				generatedProfiles.add(pInfo);
				// @TODO: this should be done automatically and not hard-coded
			}
		}
		logger.info("Number of profiles to be generated: " + generatedProfiles.size()  + 
					" number of Projects containing them:"+ llop.size());
	}
	
	
	/*
	 * 
	 * constructs a project corresponding to the profile, unless such a project already exists.
	 * On project creation, the necessary Project Descriptors for the dependencies (the module)
	 * are added and the project saved.
	 * 
	 * The profile (i.e. a shared package) is then added to the project, together with a ValidationSuite package.
	 * 
	 */
	
    public Project createProfileProject(String theProjectName, String theProfileName) {
    	String ext = ".mdzip";
    	Project retProject = null;
    	Stereotype theValidationSuiteStereo = null;
    	
    	File file = new File(theProjectName + ext );
    	ProjectDescriptor pdMain, pdAux  = null;	

    	retProject = projectsManager.getProjectByName(theProjectName);
    	logger.info("");
    	logger.info("\nSeeking Project "+theProjectName+"..." );
    	if( retProject == null) {
        	File theSysMLFile = new File("SysML Profile.mdzip");
    		logger.info("\nCreating Project "+theProjectName+"..." );
        	retProject = projectsManager.createProject();
        	pdMain = 	ProjectDescriptorsFactory.createLocalProjectDescriptor(retProject, file);
        	
        	pdAux = ProjectDescriptorsFactory.createProjectDescriptor(theSysMLFile.toURI());
        	// use module
        	if(pdAux != null ) { 
        		projectsManager.useModule(retProject, pdAux);
        	} else {
        		logger.error(theProjectName+" does not manage to use SysML Profile");
        	}
    		projectsManager.saveProject(pdMain, true);

        	//if(dependsUpon.containsKey(theProjectName.replace("Profile", "Ontology"))) {         		    		
        	if(dependsUpon.containsKey(o2p(theProjectName))) { 
        		for(String modName: dependsUpon.get(o2p(theProjectName))) { 
        			modName = o2p(modName);
        			Project theMod = projectsManager.getProjectByName(modName);
        			if(theMod != null) { 
        				pdAux = ProjectDescriptorsFactory.getDescriptorForProject(theMod);
        				
        	        	if(pdAux != null ) {
        	        		logger.info(pdAux.getRepresentationString());
        	        		projectsManager.useModule(retProject, pdAux);
        	        		logger.info("\tAdded " + modName + " as module to " + theProjectName);
        	        		logger.info("");
        	        	} else {
        	        		logger.error(theProjectName + " : cannot find project descriptor for " + modName);
        	        	}

        			} else {
        				logger.error("ERROR: Cannot add project " +  modName + " to "+ theProjectName + " cause I cannot find it");
        			}
        		}
        	} else {
        		logger.info( theProjectName + " does not have dependencies");
        	}
        	
    	} else {
    		logger.info("\nProject "+theProjectName+" seems to exist already" );    	
    		pdMain = ProjectDescriptorsFactory.getDescriptorForProject(retProject);
    	}
    	
		if (!SessionManager.getInstance().isSessionCreated()) {
			Package theValidationSuite = null;
			
			SessionManager.getInstance().createSession("LivingstonePreparing-Stereotypes");
			ElementsFactory factory = retProject.getElementsFactory();
			Profile theProfile = factory.createProfileInstance();
			theProfile.setName(theProfileName);
			
			theValidationSuiteStereo =  StereotypesHelper.getStereotype(retProject,"validationSuite");
			theValidationSuite = factory.createPackageInstance();
			theValidationSuite.setName("Validation Suite");

			if(theValidationSuiteStereo == null ) { 
				logger.error("ERROR: cannot get validationSuite stereotype");
			} else { 
				StereotypesHelper.addStereotype(theValidationSuite, theValidationSuiteStereo);
			}
			
			try {
				ModelElementsManager.getInstance().addElement(theProfile,retProject.getModel());

				ModelElementsManager.getInstance().addElement(theValidationSuite, theProfile);
			} catch (ReadOnlyElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SessionManager.getInstance().closeSession();
			// share package
			Collection<Package> sharedPackages =
					ProjectUtilities.getSharedPackages( ProjectUtilities.getProjectFor(theProfile) );
			sharedPackages.add(theProfile);
			projectsManager.sharePackage(retProject, (List<Package>) sharedPackages, "some description");
			logger.info("Made profile "+ theProfile.getName() + " shared in "+retProject.getName());
			
		} else {
			logger.error("Modification session already running");
		}
		// saving the project
		projectsManager.saveProject(pdMain, true);
		logger.info("");
		
		return retProject;
    }
    
    private void refreshModuleUsage(Project retProject) {
    	ProjectDescriptor pdAux = null;
    	String theProjectName = retProject.getName(); // .replace("Profile","Ontology");
    	logger.info("Reloading dependencies for Project " + retProject.getName());
       	if(dependsUpon.containsKey(theProjectName)) { 
    		for(String modName: dependsUpon.get(theProjectName)) { 
    			modName = o2p(modName);
    			Project theMod = projectsManager.getProjectByName(modName);
    			if(theMod != null) { 
    				pdAux = ProjectDescriptorsFactory.getDescriptorForProject(theMod);

    	        	if(pdAux != null ) {
        				if(projectsManager.findAttachedProject(retProject, pdAux) != null) { 
        					projectsManager.reloadModule(retProject, pdAux);
        					logger.info("Reloaded " + modName + " as module to " + theProjectName);
        				} else {
        					logger.error("Attempted to reload the module "+modName+" in project "+theProjectName + " where it is not used.");
        				}
    	        	} else {
    	        		logger.error(theProjectName + " : cannot find project descriptor for " + modName);
    	        	}

    			} else {
    				logger.error("Cannot reload project " +  modName + " to "+ theProjectName + " cause I cannot find it");
    			}
    		}
    	} else {
    		logger.info(theProjectName + " Project does not have any dependencies listed");
    		logger.info("");
    	}
    	
    }

    /*
     *  IDENTIFICATION METHODS
     */
    	
    	// SYSML Profiles handling
        
        private boolean isSysMLStereotype(String stereoName) {
        	if(theSysMLProfile == null) { 
        		//logger.info("There is no SysMLProfile reference loaded. Something is very wrong.");
        	}
        	if(StereotypesHelper.getStereotype(theOntoProject, stereoName, theSysMLProfile) != null) { 
        		return true;
        	}
        	return false;
        }
        
        private boolean isUMLStereotype(String stereoName) {
        	if(theUMLProfile == null) { 
        		logger.error("There is no UMLProfile reference loaded. Something is very wrong.");
        	}
        	if(StereotypesHelper.getStereotype(theOntoProject, stereoName, theUMLProfile) != null) { 
        		return true;
        	}
        	return false;
        }
        
        private boolean isAbstract(Element el) { 
        	return (el instanceof Classifier && ((Classifier)el).isAbstract());
        }
        

        private boolean isCMFRange(Element el) {
    		return hasCMFStereotype(el,CMFRanSte);
    	}

    	private boolean isCMFDomain(Element el) {
    		return hasCMFStereotype(el,CMFDomSte);
    	}

    	private boolean isCMFRelation(Element el) {
    		return hasCMFStereotype(el,CMFRelSte);
    	}

    	private boolean isCMFConcept(Element el) {
    		return hasCMFStereotype(el,CMFConSte);
    	}

    	private boolean isCMFOntology(Package p) {
    		return hasCMFStereotype(p,CMFOntSte);
    	}

    	
    	private boolean isUMLRootConcept_Element (Object family) { 
    		return ("UML Element".equals(family));
    	}
    	
    	private boolean isUMLRootConcept_Relationship (Object family) {
    		return ("UML Relationship".equals(family));
    	}
    	
    	private boolean hasCMFStereotype(Element el, Stereotype arg1) { 
    		if(StereotypesHelper.hasStereotype(el, arg1)) { 
    			return true;
    		}
    		return false;
    	}
    	
    	
    	private Object discoverFamilyOfElementOBSOLETE (Element el) {
    		if (el instanceof NamedElement)
    			return ((NamedElement)el).getName();
    		else return "";
    	}

    	private Object discoverFamilyOfElement (Element el) {
    		// for example: el is "InterfaceRealization"
    		NamedElement family = (NamedElement) traverseFromElementToRootConcept(el);
    		return family.getName();
    	}


    
    /*
     *  Convenience Method
     *  Ontology to Profile mapping 
     */
    private String o2p(String o) {
    	if(o.contains("Ontology")) {
    		return o.replace("Ontology", "Profile");
    	}
    	if(o.contains("Ontologies")) {
    		return o.replace("Ontologies", "Profiles");
    	}
    	return o;
    }
    
}
    
