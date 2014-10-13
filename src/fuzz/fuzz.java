package fuzz;

public class fuzz {

	/**
	 * This program will scan a given web page and all of it's connecting pages for it's
	 * 	inputs. After, it will either list all inputs and input types to the terminal or
	 * 	will test inputs with provided attack vectors.
	 * 
	 * @param args - needs at least two arguments. First is [discover | test] depending on
	 * 					what functionality is desired. The second is a valid URL path to
	 * 					connect to.
	 */
	public static void main(String[] args) {
		
		boolean areTesting = false;
		boolean haveCommonWords = false;
		boolean haveVectors = false;
		boolean haveSensitive = false;
		boolean randomTest = false;
		String startingPagename = args[1];
		String commonWordsFilename = new String("");
		String sensitiveFilename = "";
		String customAuthorize = "";
		String vectorsFilename = "";
		int slowTime = 500;
		Fuzzer fuzzer;

		//check to ensure the first argument is either discover or test
		areTesting = testingCheck(args[0]);
		
		//Attempt to parse the parameters
		
		for(int i = 2; i < args.length; i++) {
			//parses the current argument
			if (args[i].toLowerCase().contains("--common-words=")) {
				commonWordsFilename=args[i].substring(15);
				haveCommonWords = true;
				
				//if no filename provided to common-words argument, throw error
				if (commonWordsFilename.equals("")) {
					manMessage("no filename provided");
					System.exit(1);
				}	
			} else if(args[i].toLowerCase().contains("--custom-auth=")) {
				customAuthorize=args[i].substring(14);
				
				//if no value for the custom authorization is given, throw error
				if (customAuthorize.equals("")) {
					manMessage("no value provided for argument \"--custom-auth\"");
					System.exit(1);
				}
			} else if(areTesting && args[i].toLowerCase().contains("--random=")) {
				
				//if the argument contains "true", set the randomTest flag to true.
				//otherwise, leave randomTest flag false
				
				if (args[i].toLowerCase().contains("true")) {
					randomTest = true;
				}
			} else if(areTesting && args[i].toLowerCase().contains("--vectors=")) {
				vectorsFilename = args[i].substring(10);
				haveVectors = true;
				//if no value is given for the vectors filename, throw error
				if(vectorsFilename.equals("")) {
					manMessage("no value provided for argument \"--vectors\"");
					System.exit(1);
				}
			} else if(areTesting && args[i].toLowerCase().contains("--slow=")) {
				if (!args[i].substring(7).equals("")) {
					try {
						slowTime = Integer.parseInt(args[i].substring(7));
						if (slowTime < 0) {
							manMessage("must use a non-negative number for argument \"--slow\"");
							System.exit(1);
						}
					}catch(NumberFormatException e) {
						manMessage("invalid argument for argument \"--slow\"");
						System.exit(1);
					}
				} else {
					manMessage("no value provided for argument \"--slow\"");
					System.exit(1);
				}
			} else if (areTesting && args[i].toLowerCase().contains("--sensitive=")) {
				if (!args[i].substring(12).equals("")) {
					sensitiveFilename = args[i].substring(12);
					haveSensitive = true;
				} else {
					manMessage("no value for argument \"--sensitive\"");
					System.exit(1);
				}
			} else {
				manMessage("invalid option " + args[i]);
				System.exit(1);
			}
		}
		
		//check to ensure that the common words file exists
		if(!areTesting && !haveCommonWords) {
			manMessage("argument --common-words=file required");
			System.exit(1);
		}
		
		//if testing, ensure vectors argument was given
		if(areTesting && !haveVectors) {
			manMessage("argument --vectors=file required");
			System.exit(1);
		}
		
		//if testing, ensure sensitive file argument was given
		if(areTesting && !haveSensitive) {
			manMessage("argument --sensitive=file required");
			System.exit(1);
		}
		//initialize the Fuzzer object here with String : commonWordsFilename
		//after initializing, use the logIn method and then begin fuzzing I think
		fuzzer = new Fuzzer(commonWordsFilename);
	//	fuzzer.fuzz(startingPagename, customAuthorize);
		System.out.println("Command line works");
	}
	
	/*----------------------------------------------------------------------------
	 * Method to print out the man page message in the case of invalid parameters,
	 * 	whether it be of wrong type or too few.
	 * 
	 * @param error  Custom error message for each unique situation
	 -----------------------------------------------------------------------------*/
	
	private static void manMessage(String error) {
		System.err.println("Error: " + error);
		System.err.println("fuzz [discover | test] url OPTIONS\n");
		System.err.println("COMMANDS:");
		System.err.println("\tdiscover - Output a comprehensive, " +
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
				"page guessing and input guessing. Required.\n");
		System.err.println("	Test options:");
		System.err.println("		--vectors=file - Newline-delimited file of " +
				"common exploits to vulnerabilities. Required.");
		System.err.println("		--sensitive=file - Newline-delimited file data " +
				"that should never be leaked. Required.");
		System.err.println("		--random=[true|false] - When off, try each input to each " +
				"page systematically.\n\t\t\t When on, choose a random page, then a random input " +
				"field and test all vectors. Default: false");
		System.err.println("		--slow=500 - Number of milliseconds considered when a response " +
				"is considered \"slow\". Default is 500 milliseconds");
	}
	
	/*-------------------------------------------------------------------------
	 * Method to check the first parameter for the correct command
	 * 
	 * @param command  The string containing the first argument from when the
	 * 					program is called
	 * 
	 * @return testCheck boolean value determining if the fuzzer is just discovering
	 * 						or also testing
	 --------------------------------------------------------------------------*/
	private static boolean testingCheck(String command) {
		boolean testCheck = false;
		//check for either "discover" or "test". If neither, return error
		//and man-page messages then terminate the program
		if(command.toLowerCase().equals("discover")) {
			testCheck = false; //we aren't testing
		}else if(command.toLowerCase().equals("test")) {
			testCheck = true; //we are testing
		}else {
			manMessage("first argument must be \"discover\" or \"test\"");
			System.exit(1);
		}
		return testCheck;
	}
}
