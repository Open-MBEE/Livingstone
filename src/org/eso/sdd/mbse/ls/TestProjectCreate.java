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
import org.apache.log4j.helpers.NullEnumeration;

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


public class TestProjectCreate {

	private static Application application;
	private static Logger logger = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
    	 logger = null;
    	logger = Logger.getLogger("org.eso.sdd.mbse.ls");
		String theProjectName = "pippo";
    	String ext = ".mdzip";
    	Project retProject,depProject = null;
    	ProjectsManager projectsManager = null;
    	ProjectDescriptor projectDescriptorDep,projectDescriptorRet,projectDescriptorSysML  = null;
    	
    	// This request is enabled, because WARN >= INFO.
    	try {
    		if(logger.getAllAppenders() instanceof NullEnumeration) { 
    			logger.addAppender(new FileAppender(new PatternLayout(), "TestCreation.log"));
    		}
    	} catch (IOException ee) {
    		ee.printStackTrace();
    	}
		
    	startApplication();
    	projectsManager = Application.getInstance().getProjectsManager();
     	
    	
    	File file = new File(theProjectName + ext);
    	logger.info("File path " + file.getPath());
    	logger.info("File URI " + file.toURI());
    	   	
		logger.info("\nCreating Project "+theProjectName+"..." );
    	retProject = projectsManager.createProject();
    	projectDescriptorRet = ProjectDescriptorsFactory.createLocalProjectDescriptor(retProject, file);

    	projectsManager.saveProject(projectDescriptorRet,true);

    	//projectsManager.addCreatedProject(retProject);   		
    	//logger.info("The name of the  created project is " + retProject.getName());
    	
    	File thefile = new File("SysML Profile.mdzip");
    	projectDescriptorSysML = 	ProjectDescriptorsFactory.createProjectDescriptor(thefile.toURI());
    	// use module
    	if(projectDescriptorSysML != null ) { 
    		projectsManager.useModule(retProject, projectDescriptorSysML);
    	} else {
    		logger.error(theProjectName + ext+" does not manage to user SysML Profile");
    	}
	    	
    	retProject = projectsManager.getProjectByName(theProjectName);
    	if(retProject == null) { 
    		logger.info("I cannot find this project " + theProjectName);
    	}
    	
    	
    	depProject = projectsManager.createProject();
    	file = new File("pluto.mdzip");
    	projectDescriptorDep =	ProjectDescriptorsFactory.createLocalProjectDescriptor(depProject, file);
    	projectsManager.saveProject(projectDescriptorDep,true);
    	
//
//   
    	projectsManager.useModule(depProject, projectDescriptorRet);
    	    	
	}
	
	private static void startApplication() { 
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
