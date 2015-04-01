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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import sun.util.calendar.BaseCalendar.Date;


import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.teamwork.application.VersionData;

import com.nomagic.magicdraw.magicreport.GenerateTask;
import com.nomagic.magicdraw.magicreport.helper.TemplateHelper;
import com.nomagic.magicdraw.magicreport.ui.bean.PackageSelectionBean;
import com.nomagic.magicdraw.magicreport.ui.bean.ReportBean;
import com.nomagic.magicdraw.magicreport.ui.bean.ReportPropertyBean;
import com.nomagic.magicdraw.magicreport.ui.bean.TemplateBean;
import com.nomagic.magicdraw.metrics.MetricResult;
import com.nomagic.magicdraw.metrics.MetricsInformation;
import com.nomagic.magicdraw.metrics.MetricsManager;
import com.nomagic.magicdraw.metrics.MetricsResults;
import com.nomagic.magicdraw.metrics.MetricsSuite;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicreport.helper.FileUtils; 
import com.nomagic.magicdraw.dependency.*;

 

public class Livingstone { 
    private static String server = "teamwork.hq.eso.org";
    private static String user   =   "sqa-ops";
    private static String password = "patch22";
    private static boolean Debug = true;
    private static HashMap<String,String> theProjectLabels = new HashMap();

    
	private static String pLab = "MBSE Livingstone ";
   	
	final static String lineSeparator = System.getProperty ( "line.separator" );  
	final static String nl = "\\n";

	private static ProjectsManager projectsManager;
	private Application application;
	
	// input projects
	private Project theInputOntology = null;
	private Project theVLTOntology = null;
	
	
	// output projects
	private Project theValidationRules = null;
	private Project theVLTProfile      = null;
	//private String interfaceOntFileURL = "COWLInstrument.mdzip";
	public String ontologyFileURL = "Comodo Ontology";
	//public String ontologyFileURL = "TelescopeInstrumentOntology";
			
	private static ProjectDescriptor projectDescriptor;
	private static Project theProject;
	public static Project generatedProfileProject;

	private int recLevel = 0;
	
	public static Logger  logger = null;
	private Utilities theUt = null;
	private Engine theEngine  = null;
	private boolean silent = true;
	public boolean remoteLoad = true;
	private static String ontologyNameProperty = "ONTOLOGY_NAME";
	private static String remoteSwitchProperty = "REMOTE";

	
    private static boolean checkArgument(String arg, String propertyName) {
	    String start = propertyName + "=";
	    if (arg.startsWith(start)) {
	       String s = arg.substring(start.length());
	       System.setProperty(propertyName, s);
	       return true;
	    }
	    return false;
    }

    
    public Livingstone() { 
    	theUt = new Utilities();
    	theEngine = new Engine();
    	application = Application.getInstance();
		projectsManager = Application.getInstance().getProjectsManager();
		// initialize logger
    	logger = Logger.getLogger("org.eso.sdd.mbse.ls");
    	// This request is enabled, because WARN >= INFO.
    	logger.setLevel(Level.DEBUG);
    	try {
    		logger.addAppender(new FileAppender(new PatternLayout(), "MBSE.log"));
    	} catch (IOException e) {
    		e.printStackTrace(); 
    	}

		if(projectsManager == null) { 
			logger.info(pLab + "Empty projectsManager instance: bailing out");
		}

    }
	
    public static void  main(String[] args) {
      	ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));

    	Livingstone theApp = new Livingstone();
      	for (Iterator<String> it = argsList.iterator(); it.hasNext();)	    {
    		String arg = it.next();
    		if (checkArgument(arg, ontologyNameProperty)) { 
    			it.remove();
    		}
    		if (checkArgument(arg, remoteSwitchProperty)) { 
    			it.remove();
    		}
    	}
		
    	if(System.getProperty(ontologyNameProperty) != null) { 
    		theApp.ontologyFileURL = System.getProperty(ontologyNameProperty);
    		System.out.println("Input Ontology set to: " + theApp.ontologyFileURL);    			    		
    	} else {
    		System.out.println("Ontology Name property missing");    			    		
    	}
    	
    	if(System.getProperty(remoteSwitchProperty) != null) { 
    		if(System.getProperty(remoteSwitchProperty).equals("true")) { 
    			theApp.remoteLoad = 	true;
    		} else {
    			theApp.remoteLoad = 	false;    			    		
    		}
    		System.out.println("Remote Access set to: " + theApp.remoteLoad);    			    		
    	} else {
    		System.out.println("Remote Access property missing");    			    		
    	}



    	theApp.run(argsList);
    	System.out.println("Main method end");
    	
    }

    public void run(ArrayList<String> argsList) {
    	logger.setLevel(Level.DEBUG);
    	startApplication();
    	java.util.Date date= new java.util.Date();
   	 	logger.info(new Timestamp(date.getTime()));
    	logger.info("");
    	//theInterfaceOntology = openFileSystemProject(interfaceOntFileURL);
    	
    	if(remoteLoad) { 
    		teamworkServerLogin();
    		theInputOntology = loadProjectFromTWS(ontologyFileURL);

    		
    	} else {
    		theInputOntology = loadProjectFromFileSystem(ontologyFileURL);
    	}
    	if(theInputOntology != null) { 
    		if(verifyGivenOntologyProject())  { 
    			theEngine.run(theProject);
    		}
    	} else {
    		logger.error(pLab + " The interface ontology project is null!");
    	}
    	logger.info("Generation of profiles completed.");
    	logger.removeAllAppenders();
    	logger.shutdown();
     	if(silent) { 
     		closeApp();
     	}
    }
    
    
    /*
     * this generated the Profile Project which will hold
     * the stereotypes and their constraints
     * 
     */
    

    // put in here all assumptions on the ontology project
    private boolean verifyGivenOntologyProject() { 
    	if(theUt.dependsOn(theInputOntology, Constants.cmfName)) {
    		logger.info("Loaded project depends on "+ Constants.cmfName );
    		return true;
    	} 
    	logger.info("Loaded project does not depend on " + Constants.cmfName);    		
    	return false;
    }
    
	private Project openFileSystemProject(String theFileURL) {
		File projectFile = new File(theFileURL);
		if(!projectFile.exists()) { 
			logger.error(" File "+theFileURL + " does not exist!");
			return null;
		}
		projectDescriptor = ProjectDescriptorsFactory.createProjectDescriptor(projectFile.toURI());
		if(projectDescriptor == null) { 
			logger.error(pLab + " Could not create valid project descriptor for "+theFileURL);
			return null;
		}
		logger.info(pLab + "loading project "+theFileURL);    			
		projectsManager.loadProject(projectDescriptor, true);
		logger.info(pLab + "Project "+theFileURL+" loaded.");    			
		theProject = projectsManager.getActiveProject();
		if(theProject == null ) { 
			logger.error(" No active project for the application???");
		}
		return theProject;
	}



	
	private static void analyzeDependencies() { 
		Iterator<ElementLocationDependency> i = null;
		Iterator ii = null;
		Map map = null;
		MetricsSuite ms = null;
		
		
		// atempts to use available Metrics infrastructure failed, see:
		//  	MDUMLCS-10345
		DependencyCheckResult results = DependencyCheckerHelper.checkDependencies(theProject);
		if(!results.hasDependencies()) { 
			return;
		}
		Collection<ElementLocationDependency> myDeps = results.getDependencies();
		for(i = myDeps.iterator(); i.hasNext(); ) {
			ElementLocationDependency eld = null;
			eld = i.next();
			ElementLocation el = eld.getElementLocation();
			ElementLocation del = eld.getDependsOnLocation();
			
			//
			//el.getAttachedProject().getProjectID();
			//el.getAttachedProject().getProjectState().toString();
			
			
			if(!el.isProject()) {
				// element is not coming from the project but from a module
				continue;
			}
			logger.info("DEPENDENCY " + el.toString() + " -> " + del.toString());
		}
	}

	private static Project loadProjectFromTWS(String projectName) { 
			try { 
				logger.info(pLab + "getting remote project descriptor for " +projectName );
				System.out.flush();
				String[] branches = new String[0];
				String qProjectName = 	TeamworkUtils.generateProjectQualifiedName(projectName, branches);
				projectDescriptor = TeamworkUtils.getRemoteProjectDescriptorByQualifiedName(qProjectName);
				if(projectDescriptor != null) { 
					logger.info(pLab + "project description "+projectDescriptor.getRepresentationString());	
				} else { 
					logger.info(pLab + "empty project descriptor for " + projectName + ", bailing out.");	
					return null;
				}
			} catch(java.rmi.RemoteException re) { 
				logger.info(pLab + "Remote Exception when trying to load the project " + projectName + "\n" + re.toString());
				System.out.flush();
				return null;
			}


			logger.info(pLab + "loading project "+projectName+"...");
			System.out.flush();
			// LOAD PROJECT
			// (for some reason, the progress bar is shown....)
			projectsManager.loadProject(projectDescriptor, true);
			logger.info(pLab + "Project "+projectName+" loaded.");    			
			System.out.flush();
			theProject = Application.getInstance().getProject();
			return theProject;
	}

	private static Project loadProjectFromFileSystem(String projectName) {
		if(! projectName.endsWith(".mdzip")) { 
			projectName = projectName + ".mdzip";
		}
		File prjFile = new File(projectName);
		if(!prjFile.exists()) {
			logger.error("Cannot find file: " + projectName);
			return null;
		}
		projectDescriptor = ProjectDescriptorsFactory.createLocalProjectDescriptor(theProject,prjFile);
		if(projectDescriptor != null) { 
			logger.info(pLab + "project description "+projectDescriptor.getRepresentationString());	
		} else { 	
			logger.info(pLab + "empty project descriptor for " + projectName + ", bailing out.");	
			return null;	
		}	

		logger.info(pLab + "loading project "+projectName+"...");
		System.out.flush();
		// LOAD PROJECT
		// (for some reason, the progress bar is shown....)
		projectsManager.loadProject(projectDescriptor, true);
		logger.info(pLab + "Project "+projectName+" loaded.");    			
		System.out.flush();
		theProject = Application.getInstance().getProject();
		return theProject;
}

	
	private void startApplication() { 
       	try  {
       		if(silent) { 
       			application.start(false, true, false, new String[0], null);
       		} else { 
       			application.start(true, false, false, new String[0], null);	
       		}
    		if (Debug) { 
    				logger.info("Application Started");
    		}
 
    		//application.shutdown();
    	} catch (ApplicationExitedException e)  {
    		logger.info(pLab + "RED ALERT! COULD NOT START THE APPLICATION");
    		e.printStackTrace();
    		StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

			logger.info(sw.toString());
    	}
	}
	


	private static boolean teamworkServerLogin() {
		if (!user.equals(TeamworkUtils.getLoggedUserName())) {
			if (!TeamworkUtils.login(server, -1, user, password))   {
				// login failed
				logger.info(pLab + "LOGIN to TWS with user "+user + " FAILED");
				return false;
			} else {
				logger.info(pLab + "Logged in as user "+user );    			
				System.out.flush();    		
			}
			projectsManager = Application.getInstance().getProjectsManager();  
			if(projectsManager == null) { 
				logger.info(pLab + "Empty projectsManager instance: bailing out");
				return false;
			}
		}
		return true;
	}

	private static void teamworkServerLogout() { 
  		if(!TeamworkUtils.logout()) { 
  			logger.info(pLab + "Failed to log out from TWS!");
  			return;
  		}
  		logger.info(pLab + " logged out");			      		
	}
	
	

	private  void closeApp() { 
    	try { 
    		application.shutdown();
    		logger.info("APPLICATION SHUT DOWN " );
    	} catch(com.nomagic.runtime.ApplicationExitedException aee) {
    		logger.info("APPLICATION EXIT EXCEPTION " + aee.toString());
    	}
    }

  
}