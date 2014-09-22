package fuzz;

import java.net.*;
import java.io.*;

public class fuzz {

	/**
	 * This program will scan a given web page and all of it's connecting pages for it's
	 * 	inputs. After, it will either list all inputs and input types to the terminal or
	 * 	will test inputs with provided attack vectors.
	 * @param args
	 */
	public static void main(String[] args) {
		
		boolean areTesting;
		URL startingPage;
		
		if(args.length < 2) {
			System.out.println("Error: too few parameters");
			manMessage();
			System.exit(1);
		}else if(args[0].equals("discover")) {
			System.out.println("We discovering!");
			areTesting = false;
		}else if(args[0].equals("test")) {
			System.out.println("We testing!");
			areTesting = true;
		}else {
			System.out.println("Error: first argument must be \"discover\" or \"test\"");
			manMessage();
			System.exit(1);
		}

		try {
			startingPage = new URL(args[1]);
		}catch(MalformedURLException e) {
			System.err.println("Error: Invalid URL");
			manMessage();
			System.exit(1);
		} 
		
	}
	
	/*----------------------------------------------------------------------------
	 * Method to print out the man page message in the case of invalid parameters,
	 * 	whether it be of wrong type or too few.
	 -----------------------------------------------------------------------------*/
	
	private static void manMessage() {
		System.out.println("fuzz [discover | test] url OPTIONS\n");
		System.out.println("COMMANDS:");
		System.out.println("	discover - Output a comprehensive, " +
				"human-readable list of all discovered inputs " +
				"to the system.");
		System.out.println("	test - Discover all inputs, then attempt a " +
				"list of exploit vectors on those inputs\n");
		System.out.println("OPTIONS:");
		System.out.println("	--custom-auth=string - Signal that the fuzzer should use " +
				"hard-coded authentication for a specific application\n");
		System.out.println("	Discover options:");
		System.out.println("		--common-words=file - Newline-delimited " +
				"file of common words to be used in " +
				"page guessing and input guessing.\n");
		System.out.println("	Test options:");
		System.out.println("		--vectors=file - Newline-delimited file of " +
				"common exploits to vulnerabilities.");
		System.out.println("		--sensitive=file - Newline-delimited file data " +
				"that should never be leaked.");
		System.out.println("		--random=[true|false] - When off, try each input to each " +
				"page systematically. When on, choose a random page, then a random input " +
				"field and test all vectors. Default: false");
		System.out.println("		--slow=500 - Number of milliseconds considered when a response " +
				"is considered \"slow\". Default is 500 milliseconds");
		
	}

}
