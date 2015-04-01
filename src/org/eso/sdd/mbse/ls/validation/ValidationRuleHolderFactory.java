package org.eso.sdd.mbse.ls.validation;


import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;

import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;

import org.apache.bcel.generic.Type;
import org.apache.bcel.classfile.Attribute; 
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Logger;

import org.eso.sdd.mbse.ls.validation.JunctionValidationRuleTemplate;
import org.apache.log4j.Level;
import java.util.logging.*;

public class ValidationRuleHolderFactory {

	private String packageName;
	private String junctionTemplateName    = "JunctionValidationRuleTemplate";
	private String blockTemplateName       = "BlockValidationRuleTemplate";	
	private String pluginClassTemplateName = "ValidationRuleEmptyPlugin"; 
	private Logger logger = null;
	
	private List<String> myFiles = null;
	
	/**
	 * @param args
	 */

	public ValidationRuleHolderFactory(String pn, Logger l) { 
		packageName = pn;
    	logger = l;
    	logger.setLevel(Level.DEBUG);
    	myFiles = new ArrayList<String>();
	}
	
	public String getPackageName() { 
		return packageName;
	}
	
	
	/*
	 * add the content of the myFiles list to the named jar/zipfile
	 */
	public  void addFilesToJar(String jarfileName) { 
		byte[] buffer = new byte[1024];
 
    	try{
    		String baseName =null;
    		File theJarfile = new File(jarfileName);
    		if(theJarfile.exists()) { 
    			logger.warn("Attention: jarfile: " +jarfileName+ " already exists. Will be deleted.");
    			theJarfile.delete();
    		} else {
    			theJarfile.getParentFile().mkdirs();
    			theJarfile.createNewFile();
    		}
			FileOutputStream fos = new FileOutputStream(new File(jarfileName));
			ZipOutputStream  zos = new ZipOutputStream(fos);
    		for(String fn: myFiles) { 
    			ZipEntry ze = null;
    			if(fn.lastIndexOf(File.separatorChar) != -1) { 
    				baseName = fn.substring(fn.lastIndexOf(File.separatorChar)+1, fn.length());
    			} else {
    				baseName = fn;
    				
    				//logger.error("File: "+fn+ " does not contain "+ File.separatorChar + " bailing out.");
    				//continue;
    			}

    			logger.info("File to be added: " + fn + " ("+ baseName+")");
    			if(! new File(fn).exists()) { 
    				logger.error("Attention: the file "+fn+" does not seem to exist");
    				continue;
    			}
    			// cutting out bin/ or xxx/ slash
    			if(fn.contains(File.separatorChar + "")) { 
    				ze= new ZipEntry(fn.substring(4, fn.length()).replace(File.separatorChar,'/' )  ) ;
    			} else {
    				ze= new ZipEntry(fn  ) ;
    			}

    			zos.putNextEntry(ze);
    			FileInputStream in = new FileInputStream(fn);

    			int len;
    			while ((len = in.read(buffer)) > 0) {
    				zos.write(buffer, 0, len);
    			}

    			in.close();
    			zos.closeEntry();
    		}
			zos.close();
    		logger.info("Finished adding files to jarfile "+jarfileName+".");
 
    	}catch(IOException ex){
    	   ex.printStackTrace();
    	}
    }


	private  void replaceStringInConstantPool(ConstantPoolGen cpgen,
			String toBeReplaced, String replacement) {
		int utf8StringPointer = cpgen.lookupUtf8(toBeReplaced);
		if(utf8StringPointer != -1)
		{
		        cpgen.setConstant(utf8StringPointer, new ConstantUtf8(replacement));
		        if(cpgen.lookupString(toBeReplaced) != -1 ) { 
		        	cpgen.setConstant(cpgen.lookupString(toBeReplaced), new ConstantString(utf8StringPointer));
		        }
		        //System.out.println("Found Moose in Constant Pool ("+toBeReplaced+")");
				logger.debug("Replaced "+toBeReplaced+" with " + replacement);		        
		} else {
			logger.error("Did not find a moose! (String "+ toBeReplaced + " not found in ConstantPoolGen");
		}
	}

	/*
	 * @args: sName = stereotype name
	 * 	      pName = part name
	 * 
	 */
	private void manipulateInstructionList(ConstantPoolGen cgen, InstructionList iList, String pName, String sName,String rn) { 	
		InstructionHandle myLDC = null;
		InstructionHandle myII = null;
		INVOKEINTERFACE ii = null;
		
		InstructionHandle[] iHandles = iList.getInstructionHandles();
		for(int f = 0; f < iHandles.length; f++) {
			if( iHandles[f].getInstruction() instanceof LDC) {
				myLDC = iHandles[f];
				Instruction ins = myLDC.getInstruction();
				String symbolRep = ins.toString(cgen.getConstantPool());
				//logger.debug("found LDC:" + symbolRep + ":"+ins.toString());
				if(symbolRep.contains("INNER_ELEMENT_NAME")) { 
					LDC ldcIns = new LDC(cgen.addString(pName));
					myLDC.setInstruction(ldcIns);
					logger.debug("\tFound LDC: INNER_ELEMENT_NAME ("+pName+")");
				}
				if(symbolRep.contains("INNER_ELEMENT_TYPE")) { 
					LDC ldcIns = new LDC(cgen.addString(sName));
					myLDC.setInstruction(ldcIns);
					logger.debug("\tFound LDC: INNER_ELEMENT_TYPE ("+sName+")");
				}
				if(symbolRep.contains("BRULE_NAME")) { 
					LDC ldcIns = new LDC(cgen.addString(rn));
					myLDC.setInstruction(ldcIns);
					logger.debug("\tFound LDC: BRULE_NAME ("+rn+")");					
				}
			}
			
//			if( iHandles[f].getInstruction() instanceof INVOKEINTERFACE ){ 
//				myII = iHandles[f];
//				ii = (INVOKEINTERFACE) myII.getInstruction();
//				logger.info("\t" + ii.toString(cgen.getConstantPool()));
//				logger.info("\tFound an INVOKE INTERFACE with method: "+ii.getMethodName(cgen));
//				
//			}
			
		}
	}



	public  void replaceMethod(ClassGen cg, String oldMethodName, String newMethodName) {
		MethodGen mg = null;
		for(Method m1: cg.getMethods()) { 
			if(m1.getName().equals(oldMethodName)) { 
				logger.debug("Found method:" + oldMethodName);
				mg = new MethodGen(m1, cg.getClassName(), cg.getConstantPool());
				mg.setName(newMethodName);
				cg.replaceMethod(m1,mg.getMethod());
			}
		}
	}

	/*
	 * ATTENTION: this method is no longer called in operation, only by the main method.
	 * It can be likely deleted.
	 */
	public void createJunctionValidationRuleHolder(String genClassName, String profileName, String stOneNew, String stTwoNew) { 
		ClassGen myClassGen = null;
		JavaClass jcjvrt = null;
		File outputFile = null;
		Class<?> clazz = null;
		String fqResourceName = packageName+"."+genClassName;
		String fqTemplateName = this.getClass().getPackage().getName() +"."+junctionTemplateName ;
		String outputFileName = "bin/"+fqResourceName.replace('.', '/')+".class";

		try {
			clazz = Class.forName(fqTemplateName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Could not find any class for: "+ fqTemplateName);
			return;
		}
		
		jcjvrt = Repository.lookupClass(clazz.getCanonicalName());

		//outputFileName = "xxx/"+fqTemplateName.replace('.', '/')+".class";
		outputFileName = "bin/"+fqResourceName.replace('.', '/')+".class";
		logger.info("Forcedly setting the classname to:" + fqResourceName);
		jcjvrt.setClassName(fqResourceName);

		myClassGen = new ClassGen(jcjvrt);
		ConstantPoolGen cpgen = new ConstantPoolGen(jcjvrt.getConstantPool());

		replaceStringInConstantPool(cpgen,"PROFILE_NAME",profileName);
		replaceStringInConstantPool(cpgen,"RULE_NAME"   ,genClassName);
		
		replaceStringInConstantPool(cpgen, "STEREOONENAME",stOneNew);
		replaceStringInConstantPool(cpgen, "STEREOTWONAME",stTwoNew);

		
		replaceStringInConstantPool(cpgen,fqTemplateName.replace('.', '/'),fqResourceName.replace('.', '/'));
		myClassGen.setConstantPool(cpgen);
		myClassGen.setClassName(fqResourceName);
		replaceMethod(myClassGen,junctionTemplateName, genClassName);

		try { 
			outputFile = new File(outputFileName);
			myClassGen.getJavaClass().dump(outputFile);
			//jcjvrt.dump(outputFile);
			logger.debug("Dumped java class to file:" + outputFile.toString());
			myFiles.add(outputFile.toString());		
			logger.debug("Scheduled: "+outputFile.toString() + " for adding to jarfile.");
		} catch(IOException ioe) { 
			System.err.println(ioe.toString());
		}

	}
	
	/*
	 * The hashtable supplied indicates the part types and the their stereotypes
	 * For each pair a new method  needs to be instantiated from the template method
	 * 
	 * 
	 */

	public void createBlockValidationRuleHolder(String genClassName, String mClass, int cnumb, String profileName, 
			List<String> vHash, List<String> wHash) { 
		ClassGen myClassGen = null;
		String fqPath = "";
		JavaClass jcjvrt = null;
		File outputFile = null;
		Class<?> templateClazz,argumentClazz = null;
		String ruleName = "";
		int cn = cnumb;
		
		String methodToBeClonedName1 = "hasInnerElementTemplate";
		String methodToBeClonedName2 = "hasInnerRelationshipTemplate"; // MZA + mClass;
		String fqNewClassName = packageName+"."+genClassName;
		String fqTemplateName = this.getClass().getPackage().getName() +"."+blockTemplateName ;
		String outputFileName = "bin/"+fqNewClassName.replace('.', '/')+".class";

		if(vHash == null && wHash == null) {
			logger.error("Supplied empty hashtable for part/type pairs and for relationships. Nothing to do.");
			return;
		}
		
		try {
			templateClazz = Class.forName(fqTemplateName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Could not find any class for: "+ fqTemplateName);
			return;
		}
		
		jcjvrt = Repository.lookupClass(templateClazz.getCanonicalName());
		// 
		//adding the unmodified template class to the jarfile, for reference and comparison
		if(false && ! myFiles.contains("MasterTemplate.class")) { 
			myClassGen = new ClassGen(jcjvrt);		
			try { 
				outputFile = new File("MasterTemplate.class");
				myClassGen.getJavaClass().dump(outputFile);
				//jcjvrt.dump(outputFile);
				logger.info("Dumped java class  to file:" + outputFile.toString());
				myFiles.add(outputFile.toString());		
				logger.info("Scheduled: "+outputFile.toString() + " for adding to jarfile.");
			} catch(IOException ioe) { 
				System.err.println(ioe.toString());
			}
		}
		// TEST END
		
		
		outputFileName = "bin/"+fqNewClassName.replace('.', '/')+".class";
		jcjvrt.setClassName(fqNewClassName);

		myClassGen = new ClassGen(jcjvrt);
		ConstantPoolGen cpgen = new ConstantPoolGen(jcjvrt.getConstantPool());
		
		replaceStringInConstantPool(cpgen,"PROFILE_NAME",profileName);
		replaceStringInConstantPool(cpgen,"RULE_NAME"   ,genClassName);
		
		try {
			 fqPath = "";
			if(mClass.equals("Node")) { 
				fqPath = "com.nomagic.uml2.ext.magicdraw.deployments.mdnodes.Node";
			} else if(mClass.equals("Interface")) { 
				fqPath = "com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface";
			} else if (mClass.equals("Artifact")) { 
				fqPath = "com.nomagic.uml2.ext.magicdraw.deployments.mdartifacts.Artifact";
			} else  {
				fqPath = "com.nomagic.uml2.ext.magicdraw.classes.mdkernel."+mClass;
			}
			argumentClazz = Class.forName(fqPath);			
			logger.debug("Found class for: "+argumentClazz.getCanonicalName());
		} catch(ClassNotFoundException cnfe) { 
			logger.error("Cannot find the class!!!");
			logger.error(fqPath + " Class");
			
		}

				
		// Properties
		if(vHash != null) { 
			cn = utility1(genClassName, vHash, myClassGen, cn, methodToBeClonedName1, cpgen, mClass,argumentClazz);
		}	

		// Relationships
		if(wHash != null) { 
			cn = utility1(genClassName, wHash, myClassGen, cn, methodToBeClonedName2, cpgen, mClass,argumentClazz);
		}	
		
		
		replaceStringInConstantPool(cpgen,fqTemplateName.replace('.', '/'),fqNewClassName.replace('.', '/'));
		myClassGen.setConstantPool(cpgen);
		myClassGen.setClassName(fqNewClassName);
		replaceMethod(myClassGen,blockTemplateName, genClassName);
		
		logger.debug("Verification " + genClassName + ": CP's length is "+myClassGen.getConstantPool().getConstantPool().getLength());

		for(Method m: myClassGen.getMethods()) { 
			logger.debug("\t"+m.getName() + " "+		m.getSignature() + " = "+ m.getConstantPool().getConstantPool().length);
			//listConstantPool(m.getConstantPool());
		}
		
		try { 
			outputFile = new File(outputFileName);
			myClassGen.getJavaClass().dump(outputFile);
			//jcjvrt.dump(outputFile);
			logger.info("Dumped java class ["+mClass+"] to file:" + outputFile.toString() + "("+outputFile.length() + " bytes)");
			myFiles.add(outputFile.toString());		
			logger.info("Scheduled: "+outputFile.toString() + " for adding to jarfile.");
		} catch(IOException ioe) { 
			System.err.println(ioe.toString());
		}

	}
	
	
	/*
	 *  @param: tHash is the hash table binding the stereotype name to the relationships
	 */
	//

	private int utility1(String genClassName, List<String> tHash, ClassGen myClassGen, int cn,
			String methodToBeClonedName, ConstantPoolGen cpgen,  String mClass, Class<?> cl) {
		String ruleName;
		String replacePart = "";
		replacePart = "Template"; 
		
		// here cloning the methods:
		Method methodToBeCloned = null;
		
		for(Method m: myClassGen.getMethods()) { 
			if(m.getName().equals(methodToBeClonedName)) { 
				methodToBeCloned = m;
			}
		}

		if(methodToBeCloned == null ) {
			logger.error("Could not find: "+ methodToBeClonedName+" in " + genClassName + ", bailing out.");
			return 0;
		}
		for(Iterator<String> it = tHash.iterator(); it.hasNext(); ) {
			boolean duplicate = false;
			String pair = it.next();
			if(pair.lastIndexOf(':') == -1) {
				logger.warn("\tThe "+pair+" does not contain any : . Won't be cloning the method.");
				continue;
			}
			String part = pair.substring(0, pair.lastIndexOf(':'));
			String value = pair.substring( pair.lastIndexOf(':')+1, pair.length());
			
			ruleName = "BVR"+cn++;
			String newMethodName = methodToBeClonedName.replace(replacePart, "")+"_" + part+"_"+ value;

			logger.debug(genClassName + ":Would clone method: "+methodToBeClonedName + " ==> "+ newMethodName);
			for(Method m: myClassGen.getMethods()) { 
				if(m.getName().equals(newMethodName)) { 
					duplicate = true;
				}
			}
			if(duplicate) {
				logger.error("ERROR: request to create method which already exists: " + newMethodName + ". Ignoring it");
				continue;
			}
			
			
			MethodGen mgen = new MethodGen(methodToBeCloned, methodToBeClonedName, cpgen);			
			mgen.setName(newMethodName);
			InstructionList il = mgen.getInstructionList();
			
			manipulateInstructionList(cpgen, il,part,value,ruleName);
			il.setPositions();
			
			// the following part failed for the moment because of a "ClassFormatError" at runtime.
			// although it would have been a much more promising way.
			// maybe this depends on BCEL 6.0 which is not officially out yet, as of today
			
			if(cl != null) { 
				Type theType = Type.getType("L"+cl.getName().replace('.', '/')+";");
						
				if(theType != null ) { 
					mgen.setArgumentType(0, theType);
					for(LocalVariableGen lvg : mgen.getLocalVariables()) { 
						if(lvg.getName().equals("ib")) { 
							logger.debug("Found ib variable which has type: " + lvg.getType().getSignature());
							lvg.setType(theType);
						}
					}

				} else {
					logger.error("Could not find a type for: "+cl.getCanonicalName());
				}
			} else {
				logger.warn("Attention: the supplied Class information is empty.");
			}
			mgen.setInstructionList(il);
			mgen.setMaxStack();
			mgen.setMaxLocals();
			mgen.removeLineNumbers();			
			// we could use also replace method if we were sure that there would be not other methods to clone
			myClassGen.addMethod(mgen.getMethod());
		}
		return cn;
	}

	public void createValidationRulePluginClass(String genClassName) { 
		ClassGen myClassGen = null;
		JavaClass jcjvrt = null;
		File outputFile = null;
		Class<?> clazz = null;
		
		String fqResourceName = packageName+"."+genClassName;
		String fqTemplateName = this.getClass().getPackage().getName() +"."+pluginClassTemplateName ;
		String outputFileName = "bin/"+fqResourceName.replace('.', '/')+".class";

		try {
			clazz = Class.forName(fqTemplateName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Could not find any class for: "+ fqTemplateName);
			return;
		}
		
		jcjvrt = Repository.lookupClass(clazz.getCanonicalName());

		logger.debug("Forcedly setting the classname to:" + fqResourceName);
		jcjvrt.setClassName(fqResourceName);

		myClassGen = new ClassGen(jcjvrt);
		ConstantPoolGen cpgen = new ConstantPoolGen(jcjvrt.getConstantPool());

		
		replaceStringInConstantPool(cpgen,fqTemplateName.replace('.', '/'),fqResourceName.replace('.', '/'));
		myClassGen.setConstantPool(cpgen);
		myClassGen.setClassName(fqResourceName);
		replaceMethod(myClassGen,pluginClassTemplateName, genClassName);

		try { 
			outputFile = new File(outputFileName);
			myClassGen.getJavaClass().dump(outputFile);
			//jcjvrt.dump(outputFile);
			logger.debug("Dumped java class to file:" + outputFile.toString());
			myFiles.add(outputFile.toString());		
			logger.debug("Scheduled: "+outputFile.toString() + " for adding to jarfile.");
		} catch(IOException ioe) { 
			System.err.println(ioe.toString());
		}
	}

	public void addFileToList(String fileName) { 
		myFiles.add(fileName);		
	}
	
	public static void main(String[] args) {
		String packageName = "org.eso.sdd.mbse.ls.validation";
		String profileName = "TelescopeInstrumentProfile";
		String fqTestClassName = packageName+".TestBinaryRuleUsage";
		String jarFileName = "ppp/GeneratedRules.jar";
		String[] junClasses = {"VRH1","VRH2","VRH3"};
		String[] bloClasses = {"VRH4","VRH5","VRH6"};		
		List <String> v = new ArrayList<String>();
		List <String> w = new ArrayList<String>();
		
		v.add("Tizio:typeTizio");
		v.add("Caio:typeCaio");
		v.add("Sempronio:typeSempronio");		
		
		w.add("manifestation:caioImpl");				
		
		ConsoleAppender ca = new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
		Logger logger =	Logger.getLogger("org.eso.sdd.mbse.ls.validation.ValidationRuleHolderFactory.TEST");
		logger.addAppender(ca);

		ValidationRuleHolderFactory vrhf = new ValidationRuleHolderFactory(packageName,logger);

		for(String gcn: junClasses) { 
			String path = "bin\\"+packageName.replace('.', File.separatorChar)+File.separatorChar+gcn+".class";
			vrhf.createJunctionValidationRuleHolder(gcn, profileName, "Tel2InsMechIF", "Ins2TelMechIF");
		}

		for(String gcn: bloClasses) { 
			String path = "bin\\"+packageName.replace('.', File.separatorChar)+File.separatorChar+gcn+".class";
			vrhf.createBlockValidationRuleHolder(gcn,"Class",4,profileName , v,w);
		}

		vrhf.addFileToList("bin"+File.separatorChar+fqTestClassName.replace('.', File.separatorChar)+".class");
		vrhf.addFilesToJar(jarFileName);
	}

}
