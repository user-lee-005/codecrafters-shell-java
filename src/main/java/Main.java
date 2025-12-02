import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        while(true) {
         System.out.print("$ ");
         Scanner scanner = new Scanner(System.in);
         String command = scanner.nextLine();
         if(Objects.equals(command, "exit")) {
             break;
         }
         System.out.println(command + ": command not found");
        }
    }
}
