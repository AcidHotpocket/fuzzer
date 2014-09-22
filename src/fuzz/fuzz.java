package fuzz;

import java.net.*;
import java.io.*;

public class fuzz {

	/**
	 * This program will scan a given web page and all of it's connecting pages for it's
	 * 	inputs. After, it will either list all inputs and input types to the terminal or
	 * 	will test inputs with provided attack vectors.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		boolean areTesting = false;
		URL startingPage;
		BufferedReader reader;
		
		/*-------------------------------------------------------------------
		 * Checks to see if the minimum number of parameters are applied and if
		 * the first parameter is either "discover" or "test". If either of these
		 * checks fail, an error is thrown and the man page is printed before 
		 * terminating the program. 
		 -----------------------------------------------------------------------*/
		
		if(args.length < 2) {
			System.err.println("Error: too few parameters");
			manMessage();
			System.exit(1);
		}else if(args[0].equals("discover")) {
			System.out.println("We discovering!");
			areTesting = false;
		}else if(args[0].equals("test")) {
			System.out.println("We testing!");
			areTesting = true;
		}else {
			System.err.println("Error: first argument must be \"discover\" or \"test\"");
			manMessage();
			System.exit(1);
		}

		// attempt to take the second argument and turn it into a URL object.
		
		try {
			startingPage = new URL(args[1]);
			System.out.println("It's a url all right!");
		}catch(MalformedURLException e) {
			System.err.println("Error: Invalid URL");
			manMessage();
			System.exit(1);
		} 
		
		//Attempt to parse the rest of the parameters. Will attempt to get different
		//parameters depending on which command was used.
		
		if((args.length >= 3) && !areTesting) {
			System.out.println("there are more parameters for discovering!");
			
			for(int i = 2; i < args.length; i++) {
				if (args[i].contains("--common-words")) {
					System.out.println("we getting common words!");
				} else if(args[i].contains("--custom-auth")) {
					System.out.println("using the hardcoded authentication!");
				} else {
					System.err.println("Error: invalid option " + args[i]);
					manMessage();
					System.exit(1);
				}
			}
		}else if((args.length >= 3) && areTesting) {
			System.out.println("there are more parameters for testing!");
		}
	}
	
	/*----------------------------------------------------------------------------
	 * Method to print out the man page message in the case of invalid parameters,
	 * 	whether it be of wrong type or too few.
	 -----------------------------------------------------------------------------*/
	
	private static void manMessage() {
		System.err.println("fuzz [discover | test] url OPTIONS\n");
		System.err.println("COMMANDS:");
		System.err.println("	discover - Output a comprehensive, " +
				"human-readable list of all discovered inputs " +
				"to the system.");
		System.err.println("	test - Discover all inputs, then attempt a " +
				"list of exploit vectors on those inputs\n");
		System.err.println("OPTIONS:");
		System.err.println("	--custom-auth=string - Signal that the fuzzer should use " +
				"hard-coded authentication for a specific application\n");
		System.err.println("	Discover options:");
		System.err.println("		--common-words=file - Newline-delimited " +
				"file of common words to be used in " +
				"page guessing and input guessing.\n");
		System.err.println("	Test options:");
		System.err.println("		--vectors=file - Newline-delimited file of " +
				"common exploits to vulnerabilities.");
		System.err.println("		--sensitive=file - Newline-delimited file data " +
				"that should never be leaked.");
		System.err.println("		--random=[true|false] - When off, try each input to each " +
				"page systematically. When on, choose a random page, then a random input " +
				"field and test all vectors. Default: false");
		System.err.println("		--slow=500 - Number of milliseconds considered when a response " +
				"is considered \"slow\". Default is 500 milliseconds");
		
	}

}
