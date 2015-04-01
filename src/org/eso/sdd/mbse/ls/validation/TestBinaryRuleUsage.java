package org.eso.sdd.mbse.ls.validation;

import org.eso.sdd.mbse.ls.validation.VRH4;
import org.eso.sdd.mbse.ls.validation.VRH1;
import java.lang.reflect.Method;

import org.eso.sdd.mbse.ls.validation.JunctionValidationRuleTemplate;

public class TestBinaryRuleUsage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VRH1 vrh1 = new VRH1();
		VRH4 vrh4 = new VRH4();
		Object[] vrHolders = { vrh1,  vrh4 };
		
		//JunctionValidationRuleTemplate jvrt = new JunctionValidationRuleTemplate("pippo","pluto","paperino");
 
		String theValue = "empty";
		for(Object jvrt: vrHolders) { 
			Class<?> clazz = jvrt.getClass();
			System.out.println("\nCLASS: "+ clazz.getName());
			System.out.println("Declared methods: "+clazz.getDeclaredMethods().length);
			for(Method m: clazz.getDeclaredMethods()) { 
				System.out.println("Method: "+m.getName());
			}
		}
		System.out.println("This is the stereoOneName value which I got:"+vrh1.stereoOneName );
		System.out.println("This is the profileName value which I got:"+vrh1.profileName );
	}

}
