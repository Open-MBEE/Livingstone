package org.eso.sdd.mbse.ls;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.uml2.impl.ElementsFactory;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.ci.persistence.decomposition.ProjectAttachmentConfiguration;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.uml.DiagramTypeConstants;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
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
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.InterfaceRealization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.VisibilityKindEnum;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Signal;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdports.Port;
import com.nomagic.uml2.ext.magicdraw.deployments.mdartifacts.Artifact;
import com.nomagic.uml2.ext.magicdraw.deployments.mdnodes.ExecutionEnvironment;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.awt.Rectangle;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.NullEnumeration;

import sun.util.calendar.BaseCalendar.Date;


import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.teamwork.application.VersionData;

import com.nomagic.magicreport.helper.FileUtils; 
import com.nomagic.magicdraw.dependency.*;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;


import org.eclipse.uml2.uml.VisibilityKind;
import org.eso.sdd.mbse.templates.Utilities;

public class TestGeneratedCMFDeliverables {
	private static Application application;
	private static Logger logger = null;
	private Project retProject;
	private Profile theTelInsProfile;
	private Profile theComodoProfile;
	// tel/ins stereotypes
	private Stereotype theTelSt = null;
	private Stereotype theInsSt = null;
	private Stereotype thePPSt = null;
	// cmdo stereotypes
	private Stereotype theCmdoModuleSt = null;
	private Stereotype theCmdoComponentSt = null;
	private Stereotype theCmdoComponentImplSt = null;	
	private Stereotype theCmdoInterfaceSt = null;		
	private Stereotype theCmdoContainerSt = null;
	private Stereotype theCmdoTopicSt = null;
	private Stereotype theCmdoCommandSt = null;	
	private Utilities ut = null;
	private ProjectsManager projectsManager;
	private ProjectDescriptor projectDescriptorRet;
	private Profile theSysMLProfile;
	private Stereotype theIBSt;
	

	
	
	public TestGeneratedCMFDeliverables() {
		logger = Logger.getLogger("org.eso.sdd.mbse.ls.test");
		// This request is enabled, because WARN >= INFO.
		if(logger.getAllAppenders() instanceof NullEnumeration) { 
			//logger.addAppender(new FileAppender(new PatternLayout(), "TestCreation.log"));
			ConsoleAppender ca = new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
			logger.addAppender(ca);

		}

	}
	
	private void initializeTelIns() {
		// TODO Auto-generated method stub
		String theProjectName = "MyTelescope";
		String ext = ".mdzip";
		Project depProject = null;
		projectsManager = null;
		ProjectDescriptor projectDescriptorDep,projectDescriptorSysML  = null;
		String theDependee = "TelescopeInstrumentProfile";

		projectsManager = Application.getInstance().getProjectsManager();


		File file = new File(theProjectName + ext);
		logger.info("Deleting previous file:"+file.toString());
		file.delete();
		logger.info("File path " + file.getPath());
		logger.info("File URI " + file.toURI());

		logger.info("\nCreating Project "+theProjectName+"..." );
		retProject = projectsManager.createProject();
		projectDescriptorRet = ProjectDescriptorsFactory.createLocalProjectDescriptor(retProject, file);

		projectsManager.saveProject(projectDescriptorRet,true);

		//projectsManager.addCreatedProject(retProject);   		
		logger.info("The name of the  created project is " + retProject.getName());

		File thefile = new File("SysML Profile.mdzip");
		projectDescriptorSysML = 	ProjectDescriptorsFactory.createProjectDescriptor(thefile.toURI());
		// use module
		if(projectDescriptorSysML != null ) { 
			projectsManager.useModule(retProject, projectDescriptorSysML);
		} else {
			logger.error(theProjectName + ext+" does not manage to use SysML Profile");
		}

		retProject = projectsManager.getProjectByName(theProjectName);
		if(retProject == null) { 
			logger.info("I cannot find this project " + theProjectName);
			return;
		}


		//depProject = projectsManager.createProject();
		file = new File(theDependee + ext);
		projectDescriptorDep =	ProjectDescriptorsFactory.createLocalProjectDescriptor(depProject, file);
		if(projectDescriptorDep == null) {
			logger.error("Could not create a project descriptor facotry for "+theDependee);
			return;
		}
		// projectsManager.saveProject(projectDescriptorDep,true);

		//
		//  
		projectsManager.useModule(retProject, projectDescriptorDep);
		theTelInsProfile = StereotypesHelper.getProfile(retProject, theDependee);
		if(theTelInsProfile == null) {
			logger.error("Cannot locate "+theDependee+ " in the newly created project.");
		}
		
		theSysMLProfile = StereotypesHelper.getProfile(retProject, "SysMLProfile");
		if(theSysMLProfile == null) {
			logger.info("Cannot locate SysMLProfile in the newly created project.");
		}

		// saving main project.
		projectsManager.saveProject(projectDescriptorRet,true);
		logger.info("Project " + retProject.getName() + " saved.");
	}

	private void initializeCOMODO() {
		// TODO Auto-generated method stub
		String theProjectName = "MyComodoApplication";
		String ext = ".mdzip";
		Project depProject = null;
		projectsManager = null;
		ProjectDescriptor projectDescriptorDep,projectDescriptorSysML  = null;
		String theDependee = "Comodo Profile";

		projectsManager = Application.getInstance().getProjectsManager();


		File file = new File(theProjectName + ext);
		logger.info("Deleting previous file:"+file.toString());
		file.delete();
		logger.info("File path " + file.getPath());
		logger.info("File URI " + file.toURI());

		logger.info("\nCreating Project "+theProjectName+"..." );
		retProject = projectsManager.createProject();
		projectDescriptorRet = ProjectDescriptorsFactory.createLocalProjectDescriptor(retProject, file);

		projectsManager.saveProject(projectDescriptorRet,true);

		//projectsManager.addCreatedProject(retProject);   		
		logger.info("The name of the  created project is " + retProject.getName());


		retProject = projectsManager.getProjectByName(theProjectName);
		if(retProject == null) { 
			logger.info("I cannot find this project " + theProjectName);
			return;
		}


		//depProject = projectsManager.createProject();
		file = new File(theDependee + ext);
		projectDescriptorDep =	ProjectDescriptorsFactory.createLocalProjectDescriptor(depProject, file);
		if(projectDescriptorDep == null) {
			logger.error("Could not create a project descriptor facotry for "+theDependee);
			return;
		}
		// projectsManager.saveProject(projectDescriptorDep,true);

		//
		//  
		projectsManager.useModule(retProject, projectDescriptorDep);
		theComodoProfile = StereotypesHelper.getProfile(retProject, theDependee);
		if(theComodoProfile == null) {
			logger.error("Cannot locate "+theDependee+ " in the newly created project.");
		}
		
		// saving main project.
		projectsManager.saveProject(projectDescriptorRet,true);
		logger.info("Project " + retProject.getName() + " saved.");
	}

	private void createTelInsElements() {
		Port p1, p2 = null;
		Class ib1, ib2 = null;
		ElementsFactory factory = retProject.getElementsFactory();
		Diagram theBDD = null;
		Package valSuite = null;
		Rectangle newBounds = null;
		ut = new Utilities();

		Package tPackage = factory.createPackageInstance();
		tPackage.setName("TestPackage");
		Package telPackage = factory.createPackageInstance();
		telPackage.setName("Telescope");
		Package insPackage = factory.createPackageInstance();
		insPackage.setName("Instrument");

		
		Class theIns = factory.createClassInstance();
		theIns.setName("CRIRES");
		Class theTel = factory.createClassInstance();
		theTel.setName("UT1");
		logger.info("Created Telescope and Instrument");						
		
		theTelSt = StereotypesHelper.getStereotype(retProject, "Telescope", theTelInsProfile);
		if(theTelSt == null) { 
			logger.error("The telescope stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(theTel, theTelSt);		
			logger.info("Added respective stereotypes");									
		}

		theInsSt = StereotypesHelper.getStereotype(retProject, "Instrument", theTelInsProfile);		
		if(theInsSt == null) { 
			logger.error("The instrument stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(theIns, theInsSt);
			logger.info("Added respective stereotypes");						
		}


		Stereotype theValidationSuiteStereo = StereotypesHelper.getStereotype(retProject,"validationSuite");
		if(theValidationSuiteStereo == null ) { 
			logger.error("Empty stereotype for validationSuite");
		}
			
		// ProxyPort stereotype
		thePPSt = StereotypesHelper.getStereotype(retProject, "ProxyPort", theSysMLProfile);
		if(thePPSt == null) { 
			logger.error("The Proxy Port stereotype does not exist. Bailing out.");
			return;
		} 

		// interface block stereotype
		theIBSt = StereotypesHelper.getStereotype(retProject, "InterfaceBlock", theSysMLProfile);
		if(theIBSt == null) { 
			logger.error("The InterfaceBlock stereotype does not exist. Bailing out.");
			return;
		} 

		PackageImport pi = factory.createPackageImportInstance();
		pi.setImportedPackage(theTelInsProfile);
		
		
		p1 = factory.createPortInstance();
		p2 = factory.createPortInstance();
		p1.setName("p1");
		p2.setName("p2");		
		StereotypesHelper.addStereotype(p1, thePPSt);
		StereotypesHelper.addStereotype(p2, thePPSt);
		
		ib1 = factory.createClassInstance();
		ib2 = factory.createClassInstance();
		ib1.setName("ib1");
		ib2.setName("ib2");
		
		StereotypesHelper.addStereotype(ib1, theIBSt);
		StereotypesHelper.addStereotype(ib2, theIBSt);
		
		p1.setType(ib1);
		p2.setType(ib2);		

		if (!SessionManager.getInstance().isSessionCreated()) {
			SessionManager.getInstance().createSession("LivingstoneTestingDeliverables");
			pi.setOwner(valSuite);
			valSuite = ut.addPackage(tPackage, "Verification");
			StereotypesHelper.addStereotype(valSuite, theValidationSuiteStereo);
			try {

				ModelElementsManager.getInstance().addElement(tPackage,retProject.getModel());
				logger.info("Added package to root element");				
				
				ModelElementsManager.getInstance().addElement(telPackage,tPackage);
				logger.info("Added Telescope package to Test Package");				

				ModelElementsManager.getInstance().addElement(insPackage,tPackage);
				logger.info("Added Instrument package to Test Package");				
				
				
				ModelElementsManager.getInstance().addElement(theTel,telPackage);
				logger.info("Added telescope to package");								
				ModelElementsManager.getInstance().addElement(theIns,insPackage);				
				logger.info("Added instrument to package");								
				ModelElementsManager.getInstance().addElement(valSuite,tPackage);
				logger.info("Added validation suite  to package");								
				ModelElementsManager.getInstance().addElement(pi,valSuite);				
				logger.info("Added Package Import to validation suite");
				
				//ModelElementsManager.getInstance().addElement(p1,theIns);				
				logger.info("Added port p1 to instrument");

				//ModelElementsManager.getInstance().addElement(p2,theIns);				
				logger.info("Added port p2 to instrument");

				ModelElementsManager.getInstance().addElement(ib1,insPackage);				
				logger.info("Added ib1 to package");

				ModelElementsManager.getInstance().addElement(ib2,insPackage);				
				logger.info("Added ib2 to package");

				
			} catch (ReadOnlyElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			theBDD = ut.createDiagram(tPackage, "Test Overview", ut.DT_BDD);

			
			newBounds = new Rectangle(300, 300, 80, 50);
			ut.addElementToDiagram(theIns, theBDD, newBounds);
			newBounds = new Rectangle(300, 150, 80, 50);
			ut.addElementToDiagram(theTel, theBDD, newBounds);
			
			retProject.getDiagram(theBDD).open();
   			SessionManager.getInstance().closeSession();
		} else {
			logger.error("A modification session is already running.");
		}
		logger.info("Saving...");
		projectsManager.saveProject(projectDescriptorRet, true);
		
   		
	}

	private void createCOMODOElements() {
		Property p1, p2 = null;
		ElementsFactory factory = retProject.getElementsFactory();
		Diagram theCD = null;
		Package valSuite = null;
		Rectangle newBounds = null;
		ut = new Utilities();

		theCmdoModuleSt = StereotypesHelper.getStereotype(retProject, "cmdoModule", theComodoProfile);
		theCmdoComponentSt = StereotypesHelper.getStereotype(retProject, "cmdoComponent", theComodoProfile);
		theCmdoCommandSt = StereotypesHelper.getStereotype(retProject, "cmdoCommand", theComodoProfile);
		theCmdoComponentImplSt = StereotypesHelper.getStereotype(retProject, "cmdoComponentImpl", theComodoProfile);
		theCmdoInterfaceSt = StereotypesHelper.getStereotype(retProject, "cmdoInterface", theComodoProfile);
		theCmdoTopicSt = StereotypesHelper.getStereotype(retProject, "cmdoTopic", theComodoProfile);				
		theCmdoContainerSt = StereotypesHelper.getStereotype(retProject, "cmdoContainer", theComodoProfile);	

		Package tPackage = factory.createPackageInstance();
		tPackage.setName("lsvAltAzController");
		Class theComp = factory.createClassInstance();
		theComp.setName("AltAzController");
		
		Artifact theCompImpl = factory.createArtifactInstance();
		theCompImpl.setName("AltAzControllerImpl");
		logger.info("Created Module and Component");						
		Interface theCompIF = factory.createInterfaceInstance();
		theCompIF.setName("AltAzControllerIF");
		logger.info("Created Interface");		
		
		ExecutionEnvironment theExecEnv = factory.createExecutionEnvironmentInstance();
		theExecEnv.setName("theContainer");
		
		Signal s1 = factory.createSignalInstance();
		s1.setName("s1");
		Signal s2 = factory.createSignalInstance();
		s2.setName("s2");		
		
		
		if(theCmdoModuleSt == null) { 
			logger.error("The theCmdoModuleSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(tPackage, theCmdoModuleSt);		
			logger.info("Added respective stereotypes");									
		}


		if(theCmdoComponentSt == null) { 
			logger.error("The theCmdoComponentSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(theComp, theCmdoComponentSt);
			logger.info("Added respective stereotypes");						
		}

		if(theCmdoComponentImplSt == null) { 
			logger.error("The theCmdoComponentImplSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(theCompImpl, theCmdoComponentImplSt);
			logger.info("Added respective stereotypes");						
		}

		if(theCmdoInterfaceSt == null) { 
			logger.error("The theCmdoInterfaceSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(theCompIF, theCmdoInterfaceSt);
			logger.info("Added respective stereotypes");						
		}

		if(theCmdoContainerSt == null) { 
			logger.error("The theCmdoContainerSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(theExecEnv, theCmdoContainerSt);
			logger.info("Added respective stereotypes");						
		}

		if(theCmdoCommandSt == null) { 
			logger.error("The theCmdoCommandSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(s1, theCmdoCommandSt);
			logger.info("Added respective stereotypes");						
		}

		if(theCmdoTopicSt == null) { 
			logger.error("The theCmdoTopicSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(s2, theCmdoTopicSt);
			logger.info("Added respective stereotypes");						
		}

		
		Stereotype theValidationSuiteStereo = StereotypesHelper.getStereotype(retProject,"validationSuite");
		if(theValidationSuiteStereo == null ) { 
			logger.error("Empty stereotype for validationSuite");
		}
			

		PackageImport pi = factory.createPackageImportInstance();
		pi.setImportedPackage(theComodoProfile);

		
		p1 = factory.createPropertyInstance();
		p2 = factory.createPropertyInstance();
		
		p1.setName("g");
		p1.setVisibility( VisibilityKindEnum.PUBLIC ); 
		p2.setName("sv1");		
		p2.setVisibility( VisibilityKindEnum.PUBLIC ); 	

		/*
		if(theCmdoSubscribeSt == null) { 
			logger.error("The theCmdoSubscribeSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(p1, theCmdoSubscribeSt);
		}
		if(theCmdoSubscribeSt == null) { 
			logger.error("The theCmdoSubscribeSt stereotype does not exist");
		} else {
			StereotypesHelper.addStereotype(p2,theCmdoSubscribeSt );
		}
*/	

		if (!SessionManager.getInstance().isSessionCreated()) {
			SessionManager.getInstance().createSession("LivingstoneTestingDeliverables");
			pi.setOwner(valSuite);
			valSuite = ut.addPackage(tPackage, "Verification");
			StereotypesHelper.addStereotype(valSuite, theValidationSuiteStereo);
			try {
				ModelElementsManager.getInstance().addElement(tPackage,retProject.getModel());
				logger.info("Added package to root element");				
				ModelElementsManager.getInstance().addElement(theComp,tPackage);
				logger.info("Added component to package");								

				ModelElementsManager.getInstance().addElement(s1,tPackage);
				logger.info("Added command to package");								

				ModelElementsManager.getInstance().addElement(s2,tPackage);
				logger.info("Added topic to package");								

				InterfaceRealization ir = factory.createInterfaceRealizationInstance();

				ModelElementsManager.getInstance().addElement(ir,theComp);
				logger.info("Added InterfaceRealization to component");								
				ir.setContract(theCompIF);		
				
				ModelElementsManager.getInstance().addElement(theCompImpl,tPackage);				
				logger.info("Added implementation to package");								
				ModelElementsManager.getInstance().addElement(theExecEnv,tPackage);				
				logger.info("Added execution environment to package");								

				ModelElementsManager.getInstance().addElement(valSuite,retProject.getModel());
				logger.info("Added validation suite  to root element");								
				ModelElementsManager.getInstance().addElement(pi,valSuite);				
				logger.info("Added Package Import to validation suite");
				
				ModelElementsManager.getInstance().addElement(p1,theCompIF);				
				logger.info("Added property p1 to interface");

				ModelElementsManager.getInstance().addElement(p2,theCompIF);				
				logger.info("Added property p2 to interface");
				ModelElementsManager.getInstance().addElement(theCompIF,tPackage);
				logger.info("Added interface to module");								


				
			} catch (ReadOnlyElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			theCD = ut.createDiagram(tPackage, "Test Overview", DiagramTypeConstants.UML_CLASS_DIAGRAM);

			newBounds = new Rectangle(300, 300, 80, 50);
			ut.addElementToDiagram(theComp, theCD, newBounds);
			newBounds = new Rectangle(300, 150, 80, 50);
			ut.addElementToDiagram(theCompImpl, theCD, newBounds);

			newBounds = new Rectangle(300, 10, 80, 50);
			ut.addElementToDiagram(theCompIF, theCD, newBounds);

			newBounds = new Rectangle(400, 150, 80, 50);
			ut.addElementToDiagram(theExecEnv, theCD, newBounds);

			retProject.getDiagram(theCD).open();
   			SessionManager.getInstance().closeSession();
		} else {
			logger.error("A modification session is already running.");
		}
		logger.info("Saving...");
		projectsManager.saveProject(projectDescriptorRet, true);
		
   		
	}
/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		TestGeneratedCMFDeliverables tgcd = new TestGeneratedCMFDeliverables();
		tgcd.startApplication();
		if(false) {
			tgcd.initializeTelIns();
			tgcd.createTelInsElements();
		}
		if(true) {
			tgcd.initializeCOMODO();
			tgcd.createCOMODOElements();
		}
		
		
		/*
		 *  This assumes that the plug-in directory contains links to the 
		 *  binary validation rules which have been generated.
		 * 
		 *  0) start a new MagicDraw instance
		 *  1) delete file with new project
		 *  1) create a new project
		 *  2) make it dependent on modules located on the filesystem
		 *  3) add a test package
		 *  4) add a Block, stereotype it with Telescope
		 *  5) add a Block, stereotype it with Instrument
		 *  6) create a BDD
		 *  7) create a validationSuite
		 *  8) do a package import from the validation suite to the TelescopeInstrumentProfile
		 *  9) add the telescope and the instrument to the BDD
		 *  10) open the BDD.
		 * 
		 *  The user takes over from that moment.
		 * 
		 * 
		 * 
		 */
	}
	
	private void startApplication() { 
		application = Application.getInstance();
       	try  {
     		application.start(true, false, false, new String[0], null);
    				logger.info("Application Started");
 
    		//application.shutdown();
    	} catch (ApplicationExitedException e)  {
    		logger.info( "RED ALERT! COULD NOT START THE APPLICATION");
    		e.printStackTrace();
    		StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			logger.info(sw.toString());
    	}
	}
	


}
