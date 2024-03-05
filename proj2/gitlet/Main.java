package gitlet;

import java.io.File;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author 2580368016
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */


    private static void incorrectOperands() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }

    private static void checkInit() {
        if (Repository.GITLET_DIR.exists())
            return;
        System.out.println("Not in an initialized Gitlet directory.");
        System.exit(0);
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.initRepository();
                break;
            case "add":
                checkInit();

                if (args.length != 2)
                    incorrectOperands();
                File fileToBeAdded = Utils.join(Repository.CWD, args[1]);
                if (!fileToBeAdded.exists()) {
                    System.out.println("File does not exist.");
                    System.exit(0);
                }

                Repository.add(args[1]);
                break;
            case "commit":
                checkInit();

                if (args.length != 2) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }

                Repository.commit(args[1]);
                break;
            case "log":
                checkInit();
                if (args.length > 1)
                    incorrectOperands();
                Repository.log();
                break;


            default:
                System.out.println("No command with that name exists.");
        }
    }
}
