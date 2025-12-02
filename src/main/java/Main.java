import utils.StringUtils;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static String[] allowedCommands = new String[] {"echo", "exit", "type"};
    public static void main(String[] args) throws Exception {
        while(true) {
             System.out.print("$ ");
             Scanner scanner = new Scanner(System.in);
             String command = scanner.nextLine();
             if (executeCommand(command)) break;
        }
    }

    private static boolean executeCommand(String command) {
        if(StringUtils.equals(command, "exit")) {
            return true;
        } else if(StringUtils.startsWith(command, "echo")) {
            System.out.println(getArgs(command, "echo"));
        } else if(StringUtils.startsWith(command, "type")) {
            String arg = getArgs(command, "type");
            if(Arrays.stream(allowedCommands).anyMatch(cmd -> StringUtils.equals(cmd, arg))) {
                System.out.println(arg + " is a shell builtin");
            } else {
                System.out.println(arg + ": not found");
            }
        }

        else {
            System.out.println(command + ": command not found");
        }
        return false;
    }

    private static String getArgs(String args, String command) {
        int commandLength = command.length();
        return args.substring(commandLength + 1).trim();
    }
}
