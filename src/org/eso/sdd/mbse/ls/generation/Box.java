package org.eso.sdd.mbse.ls.generation;

import java.io.InputStream;


/**
 * Generic version of the Box class.
 * @param <T> the type of the value being boxed
 */
public class Box<T> {
    // T stands for "Type"
    private T t;

    public void set(T t) { this.t = t; }
    public T get() { return t; }



    public static void main(String argv[]) { 

    	Class theClass;
    	Box<Integer> integerBox = new Box<Integer>();
    	Integer alpha = new Integer(1);

    	integerBox.set(alpha);
    	System.out.println("Set integer into the box");

    	theClass = integerBox.getClass();

    	System.out.println(theClass.getPackage().getName());
    	InputStream is = theClass.getResourceAsStream('/' + theClass.getName().replace('.', '/')+".class");
    	
    	try {
    		theClass = Class.forName("org.eso.sdd.mbse.ls.Engine");
    		if(theClass != null) { 
    			int r = 0;
    			int bytesRead = 0;
    			//is = theClass.getClassLoader().getResourceAsStream("/org/eso/sdd/mbse/ls/Engine.class");
    			is = theClass.getClassLoader().getResourceAsStream(theClass.getName().replace('.', '/') + ".class");
    			System.out.println("Package is: " + theClass.getPackage().getName());
    			if(is == null) { 
    				System.err.println("The INPUT STREAM IS EMPTY!!!");
    				return;
    			}

    			byte[] buffer = new byte[8192];
				for( int i = 0 ; i < 8192 ; i++ ) { 
					buffer[i] = (byte)0;
				}
    			
				try { 
    				while((r=is.read(buffer)) >= 0 ) {
    					bytesRead += r;
    					System.out.println("Read " + r + " bytes");
    				}   
    				System.out.println("Finally Read " + bytesRead + " bytes");
    				for( int i = 0 ; i < 8192 ; i++ ) { 
    					//System.out.println(">" + buffer[i]);
    				}
    				System.out.println(r);    		
    			} catch(Exception e) { 
    				System.err.println("Could not read the buffer, go to Hell!");    					
    			}
    		}
    	} catch(Exception e ) {
    		System.err.println("Could not find the class, go to Hell!");
    	}

    }


}