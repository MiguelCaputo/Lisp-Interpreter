package plc.interpreter;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public final class Repl {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Interpreter interpreter = new Interpreter(new PrintWriter(System.out, true), new Scope(null));

    public static void main(String[] array) {
        interpreter.scope.define("source", (Function<List<Ast>, Object>) args -> {
            args.stream()
                    .map(interpreter::eval)
                    .filter(r -> r != Interpreter.VOID)
                    .forEach(interpreter.out::println);
            return Interpreter.VOID;
        });
        while (true) {
            try {
                interpreter.eval(Parser.parse(scanner.nextLine()));
            } catch (ParseException | EvalException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected exception:");
                e.printStackTrace();
            }
        }
    }

}
