package org.eso.sdd.mbse.ls.validation;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.JOptionPane;
import com.nomagic.magicdraw.core.project.ProjectsManager;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.NullEnumeration;
import org.eso.sdd.mbse.ls.Constants;
import org.eso.sdd.mbse.ls.Engine;
import org.eso.sdd.mbse.ls.Utilities;

class CMFAction1 extends MDAction { 
    private JFileChooser fc = null;

    public CMFAction1(String id, String name) { 
    	super(id,name,null,null);

    }

    /**
     * shows message
     **/

    public void actionPerformed(ActionEvent e) {
    	Model theModel = null;
    	Utilities theUt = new Utilities();
    	Engine theEngine = null;
    	Logger logger = null;
    	Project theProject = null;
    	File destFile,pFile,dirFile = null;
		ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
		theProject = Application.getInstance().getProject();


		if(theProject == null) {
			logger.error("Application seems to have no project loaded. Aborting");
			return;
		}
		
    	

    	if(theUt.dependsOn(theProject, Constants.cmfName)) {
    		logger.info("Loaded project depends on "+ Constants.cmfName );
    	} else {  
    		logger.info("Loaded project does not depend on " + Constants.cmfName);    		
    		return;
    	}

		theEngine = new Engine();
		theEngine.run(theProject);
     }
}


