package plc.interpreter;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;

public final class Interpreter {

    /**
     * The VOID constant represents a value that has no useful information. It
     * is used as the return value for functions which only perform side
     * effects, such as print, similar to Java.
     */
    public static final Object VOID = new Function<List<Ast>, Object>() {

        @Override
        public Object apply(List<Ast> args) {
            return VOID;
        }

    };

    public final PrintWriter out;
    public Scope scope;

    public Interpreter(PrintWriter out, Scope scope) {
        this.out = out;
        this.scope = scope;
        init();
    }

    /**
     * Delegates evaluation to the method for the specific instance of AST. This
     * is another approach to implementing the visitor pattern.
     */
    public Object eval(Ast ast) {
        if (ast instanceof Ast.Term) {
            return eval((Ast.Term) ast);
        } else if (ast instanceof Ast.Identifier) {
            return eval((Ast.Identifier) ast);
        } else if (ast instanceof Ast.NumberLiteral) {
            return eval((Ast.NumberLiteral) ast);
        } else if (ast instanceof Ast.StringLiteral) {
            return eval((Ast.StringLiteral) ast);
        } else {
            throw new AssertionError(ast.getClass());
        }
    }

    /**
     * Evaluations the Term ast, which returns the value resulting by calling
     * the function stored under the term's name in the current scope. You will
     * need to check that the type of the value is a {@link Function}, and cast
     * to the type {@code Function<List<Ast>, Object>}.
     */
    private Object eval(Ast.Term ast) {
        Ast.Identifier identifier = new Ast.Identifier(ast.getName());
        Function<List<Ast>, Object> fun = requireType(Function.class,eval(identifier));
        return fun.apply(ast.getArgs());
    }

    /**
     * Evaluates the Identifier ast, which returns the value stored under the
     * identifier's name in the current scope.
     */
    private Object eval(Ast.Identifier ast) {
        return scope.lookup(ast.getName());
    }

    /**
     * Evaluates the NumberLiteral ast, which returns the stored number value.
     */
    private BigDecimal eval(Ast.NumberLiteral ast) {
        return ast.getValue();
    }

    /**
     * Evaluates the StringLiteral ast, which returns the stored string value.
     */
    private String eval(Ast.StringLiteral ast) { return ast.getValue(); }

    /**
     * Initializes the given scope with fields and functions in the standard
     * library.
     */
    private void init() {
        scope.define("print", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            evaluated.forEach(out::print);
            out.println();
            return VOID;
        });
        scope.define("+", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ZERO;
            for (Object obj : evaluated) {
                result = result.add(requireType(BigDecimal.class, obj));
            }
            return result;
        });
        scope.define("-", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream()
                    .map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            if (evaluated.size() == 1)
                return evaluated.get(0).negate();
            else if (evaluated.size() == 0)
                throw new EvalException("Error: No arguments for subtraction");
            else {
                BigDecimal number = evaluated.get(0);
                for (int i = 1; i < evaluated.size(); i++) {
                        number = number.subtract(evaluated.get(i));
                }
                return number;
            }
        });
        scope.define("*", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream()
                    .map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            BigDecimal result = BigDecimal.ONE;
            for (BigDecimal num : evaluated) {
                result = result.multiply(num);
            }
            return result;
        });
        scope.define("/", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream()
                    .map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            BigDecimal result = BigDecimal.ZERO;
            BigDecimal div = BigDecimal.ONE;
            if (evaluated.size() == 1)
                return div.divide(evaluated.get(0),RoundingMode.HALF_EVEN);
            else if (evaluated.size() == 0)
                throw new EvalException("Error: No arguments for Division");
            else {
                result = result.add(evaluated.get(0));
                for (int i = 1; i < evaluated.size(); i++) {
                    div = div.multiply(evaluated.get(i));
                }
                try {
                    result = result.divide(div,  RoundingMode.HALF_EVEN);
                }
                catch (ArithmeticException e) { throw new EvalException("Error: Cannot divide by zero"); }
            }
            return result;
        });
        scope.define("true", true);
        scope.define("false", false);
        scope.define("equals?", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2)
                throw new EvalException("Error: Two arguments required");
            return (Objects.deepEquals(eval(args.get(0)),eval(args.get(1))));
        });
        scope.define("not", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 1)
                throw new EvalException("Error: Single boolean is required");
            if (Objects.deepEquals(requireType(Boolean.class, eval(args.get(0))), true))
                return false;
            return true;
        });
        scope.define("and", (Function<List<Ast>, Object>) args -> {
            for (Ast arg: args) {
                if (Objects.deepEquals(requireType(Boolean.class, eval(arg)), false))
                    return false;
            }
            return true;
        });
        scope.define("or", (Function<List<Ast>, Object>) args -> {
            for (Ast arg: args) {
                if (Objects.deepEquals(requireType(Boolean.class, eval(arg)), true))
                    return true;
            }
            return false;
        });
        scope.define("<", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream()
                    .map(a -> requireType(Comparable.class, eval(a)))
                    .collect(Collectors.toList());
            boolean x = true;
            if (evaluated.size() == 0)
                return x;
            try {
                for (int i = 0; i < evaluated.size() - 1; i++) {
                    if (evaluated.get(i).compareTo(evaluated.get(i+1)) >= 0)
                        x = false;
                }
            }
            catch (ClassCastException w) { throw new EvalException("Error: Arguments not comparable"); }
            return x;
        });
        scope.define("<=", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream()
                    .map(a -> requireType(Comparable.class, eval(a)))
                    .collect(Collectors.toList());
            boolean x = true;
            if (evaluated.size() == 0)
                return x;
            try {
                for (int i = 0; i < evaluated.size() - 1; i++) {
                    if (evaluated.get(i).compareTo(evaluated.get(i+1)) > 0)
                        x = false;
                }
            }
            catch (ClassCastException w) { throw new EvalException("Error: Arguments not comparable"); }
            return x;
        });
        scope.define(">", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream()
                    .map(a -> requireType(Comparable.class, eval(a)))
                    .collect(Collectors.toList());
            boolean x = true;
            if (evaluated.size() == 0)
                return x;
            try {
                for (int i = 0; i < evaluated.size() - 1; i++) {
                    if (evaluated.get(i).compareTo(evaluated.get(i+1)) <= 0)
                        x = false;
                }
            }
            catch (ClassCastException w) { throw new EvalException("Error: Arguments not comparable"); }
            return x;
        });
        scope.define(">=", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream()
                    .map(a -> requireType(Comparable.class, eval(a)))
                    .collect(Collectors.toList());
            boolean x = true;
            if (evaluated.size() == 0)
                return x;
            try {
                for (int i = 0; i < evaluated.size() - 1; i++) {
                    if (evaluated.get(i).compareTo(evaluated.get(i+1)) < 0)
                        x = false;
                }
            }
            catch (ClassCastException w) { throw new EvalException("Error: Arguments not comparable"); }
            return x;
        });
        scope.define("list", (Function<List<Ast>, Object>) args -> {
            LinkedList<Object> list = new LinkedList<>();
            for (Ast arg : args) {
                list.add(eval(arg));
            }
            return list;
        });
        scope.define("range", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream()
                    .map(a -> requireType(BigDecimal.class, eval(a)))
                    .collect(Collectors.toList());
            LinkedList<Object> list = new LinkedList<>();
            BigDecimal start,end;
            if (evaluated.size() != 2)
                throw new EvalException("Error: Two arguments required");
            start = evaluated.get(0);
            end = evaluated.get(1);
            if (start.compareTo(end) == 0 )
                return list;
            else if(start.stripTrailingZeros().scale() > 0 || end.stripTrailingZeros().scale() > 0)
                throw new EvalException("Error: Arguments are not integers");
            else if (start.compareTo(end) == 1)
                throw new EvalException("Error: First argument > Second argument");
            for (int i = start.intValueExact(); i < end.intValueExact(); i++) {
                list.add(new BigDecimal(i));
            }
            return list;
        });
        scope.define("define", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2)
                throw new EvalException("Error: Expecting two arguments");
            // Define variable
            if (args.get(0) instanceof Ast.Identifier)
                scope.define(((Ast.Identifier) args.get(0)).getName(), eval(args.get(1)));
            else if (args.get(0) instanceof Ast.Term)
            {
                String name = ((Ast.Term) args.get(0)).getName();
                List<String> parameters = ((Ast.Term) args.get(0)).getArgs().stream()
                        .map(a -> requireType(Ast.Identifier.class, a).getName())
                        .collect(Collectors.toList());
                Scope parent = scope;
                scope.define(name, (Function<List<Ast>, Object>) arguments -> {
                    List<Object> evaluated = arguments.stream().map(this::eval).collect(Collectors.toList());
                    if (parameters.size() != evaluated.size())
                        throw new EvalException("Invalid number of arguments");
                    Scope current = scope;
                    scope = new Scope(parent);
                    for (int i = 0; i < parameters.size(); i++)
                    {
                        scope.define(parameters.get(i), evaluated.get(i));
                    }
                    Object result = eval(args.get(1));
                    scope = current;
                    return result;
                });
            }
            else
                throw new EvalException("Invalid first argument");
            return VOID;
        });
        scope.define("set!", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2)
                throw new EvalException("Error: Expecting two arguments");
            Ast.Identifier ast = requireType(Ast.Identifier.class, args.get(0));
            scope.set(ast.getName(), eval(args.get(1)));
            return VOID;
        });
        scope.define("do", (Function<List<Ast>, Object>) args -> {
            Object x = VOID;
            scope = new Scope(scope);
            for (Ast ast : args)
            {
                x = eval(ast);
            }
            scope = scope.getParent();
            return x;
        });
        scope.define("while", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2) {
                throw new EvalException( "Expected 2 arguments, received " + args.size() + "." );
            }
            while (requireType( Boolean.class, eval(args.get(0)))) {
                eval(args.get(1));
            }
            return VOID;
        });
        scope.define("for", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2) {
                throw new EvalException( "Expected 2 arguments, received " + args.size() + "." );
            }
            Ast.Term term = requireType(Ast.Term.class,args.get(0));
            if (term.getArgs().size() != 1)
                throw new EvalException( "Expected 1 arguments , received " + args.size() + "." );
            LinkedList<Object> list = requireType(LinkedList.class, eval(term.getArgs().get(0)));
            if (list.size() == 0)
                return VOID;
            scope = new Scope(scope);
            scope.define(term.getName(), "0");
            for (int i = 0; i < list.size(); i++) {
                scope.set(term.getName(), list.get(i));
                eval(args.get(1));
            }
            scope = scope.getParent();
            return VOID;
        });
    }


    /**
     * A helper function for type checking, taking in a type and an object and
     * throws an exception if the object does not have the required type.
     */
    private static <T> T requireType(Class<T> type, Object value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new EvalException("Expected " + value + " to have type " + type.getSimpleName() + ".");
        }
    }
}
