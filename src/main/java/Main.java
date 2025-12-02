import utils.StringUtils;

import java.util.Objects;
import java.util.Scanner;

public class Main {
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
            System.out.println(command.split("echo")[1].trim());
        } else {
            System.out.println(command + ": command not found");
        }
        return false;
    }
}
