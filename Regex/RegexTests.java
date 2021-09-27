package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Tests declarations for steps 1 & 2
 * are provided, you must add your own for step 3.
 *
 * To run tests, either click the run icon on the left margin or execute the
 * gradle test task, which can be done by clicking the Gradle tab in the right
 * sidebar and navigating to Tasks > verification > test Regex(double click to run).
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests.
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("2 letter domain domain", "thelegend27@gmail.co", true),
                Arguments.of("No type of email", "thelegend27@.com", true),
                Arguments.of("Allowed Symbol in name", "the-legend_2.7@yahoo.com", true),
                Arguments.of("Different emails", "thelegend2.7@hotmail.com", true), //no
                Arguments.of("Caps in names", "MYEMail@yahoo.com", true),
                Arguments.of("Caps in type of mail", "thelegend27@GMAIL.com", true),
                Arguments.of("Numbers in type of mail", "thelegend27@gmail2.com", true),
                Arguments.of("Allowed Symbol in type of mail", "thelegend27@yah-oo.com", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),
                Arguments.of("Just the domain", "@gmail.com", false),
                Arguments.of("More than 2-3 letters on the domain", "thelegend27@gmail.comm", false),
                Arguments.of("No domain", "thelegend27@gmail.", false),
                Arguments.of("Missing @", "thelegend27gmail.com", false),
                Arguments.of("Numeric domain", "thelegend27@gmail.123", false),
                Arguments.of("Symbol in Domain", "thelegend27@gmail.c@m", false),
                Arguments.of("All Caps Domain", "thelegend27@gmail.COM", false),
                Arguments.of("One Character Domain", "thelegend27@gmail.c", false),
                Arguments.of("Invalid Symbol in Domain", "thelegend27@ya_hoo.com", false),
                Arguments.of("Invalid Symbol before domain", "thelegend27$yahoo$com", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testFileNamesRegex(String test, String input, boolean success) {
        //this one is different as we're also testing the file name capture
        Matcher matcher = test(input, Regex.FILE_NAMES, success);
        if (success) {
            Assertions.assertEquals(input.substring(0, input.indexOf(".")), matcher.group("name"));
        }
    }

    public static Stream<Arguments> testFileNamesRegex() {
        return Stream.of(
                Arguments.of("Java File", "Regex.tar.java", true),
                Arguments.of("Java Class", "RegexTests.class", true),
                Arguments.of("Combine Extensions", "Regex.java.class", true),
                Arguments.of("Same Extensions", "Regex.java.java", true),
                Arguments.of("Multiple extensions", "Regex.tar.co.se.lol.java", true),
                Arguments.of("Symbols in name", "Reg_=ex.tar.java", true),
                Arguments.of("Symbols in extension", "Regex.ta^@r.java", true),
                Arguments.of("Alphanumeric extension", "Regex.ta3r.java", true),
                Arguments.of("Alphanumeric name", "Reg3ex.tar.java", true),
                Arguments.of("Caps extension", "Regex.TAR.java", true),
                Arguments.of("Caps Name", "REGEX.tar.java", true),
                Arguments.of("No dot with extension", "Regexjava", false),
                Arguments.of("Directory", "directory", false),
                Arguments.of("Python File", "scrippy.py", false),
                Arguments.of("More than one dot", "Regex..tar.java", false),
                Arguments.of("No name", ".java", false),
                Arguments.of("Caps Extension java or class", "Regex.tar.CLASS", false),
                Arguments.of("Something after java or class", "Regex.tar.javaa", false),
                Arguments.of("Just spaces after dot", "Regex. .java", false),
                Arguments.of("Just spaces name", " .tar.java", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                Arguments.of("14 Characters", "thishas14chars", true),
                Arguments.of("10 Characters", "i<3pancakes!", true),
                Arguments.of("Symbols", ")(*(&^%@%@^@", true),
                Arguments.of("Just Spaces", "            ", true),
                Arguments.of("All caps", "THISISLONG", true),
                Arguments.of("Just Numbers", "12345678910111", true),
                Arguments.of("Spaces in the string", "This is more ok!", true),
                Arguments.of("20 Characters", "mynameismiguelandthi", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("No Characters", "", false),
                Arguments.of("More than 20 characters", "6chars55555555555555555", false),
                Arguments.of("15 Characters", "i<3pancakes!!", false),
                Arguments.of("Just numbers less than 10", "123456789", false),
                Arguments.of("Just symbols more than 20", "***#*******%***************&****@****", false),
                Arguments.of("Just a space", " ", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Empty List", "[]", true),
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Multiple Digit Numbers", "[645,2222,6789]", true),
                Arguments.of("Multiple and single digit Numbers", "[4,2222,3]", true),
                Arguments.of("Space After Commas Space", "[1, 2, 3]", true),
                Arguments.of("Just large digit", "[1234]", true),
                Arguments.of("Space and not spaces after Commas", "[1,2, 3]", true),
                Arguments.of("Large List", "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20]", true),
                Arguments.of("Symbols", "[&, @, #]", false),
                Arguments.of("Spaces between digits", "[4,2 2 2 2,3]", false),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Trailing Comma", "[1,2,3,]", false),
                Arguments.of("Missing Bracket", "[1,2,3", false),
                Arguments.of("Multiple Brackets", "[1,2,3]]", false),
                Arguments.of("Ending in Space", "[1, 2, ]", false),
                Arguments.of("Dots instead of Commas", "[1.2.3]", false),
                Arguments.of("Just Space", "[ ]", false),
                Arguments.of("Just Comma", "[,]", false),
                Arguments.of("Just Comma and space", "[, ]", false),
                Arguments.of("Brackets in the middle", "[1,2],3", false),
                Arguments.of("Letters", "[a,b,c]", false),
                Arguments.of("Negative Digit Numbers", "[-5,-2,-9]", false),
                Arguments.of("Decimal Numbers", "[1.5, 2.0, 3.3]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIdentifier(String test, String input, boolean success) {
        test(input, Regex.IDENTIFIER, success);
    }

    public static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("Normal String", "getName", true),
                Arguments.of("Allowed Symbols", "<=>", true),
                Arguments.of("Allowed Symbols 2", "is-empty?", true),
                Arguments.of("Allowed Numbers", "hello123", true),
                Arguments.of("More than one dot", "..", true),
                Arguments.of("Upper case and numbers", "HELLOworld123", true),
                Arguments.of("Starting with a dot", ".hello", true),
                Arguments.of("Starting with symbols", "!+hello", true),
                Arguments.of("Begin with number", "42=life", false),
                Arguments.of("Just a dot", ".", false),
                Arguments.of("Invalid Symbols", "why,are,there,commas,", false),
                Arguments.of("Invalid Symbols 2", "$\\;@+()#[]", false),
                Arguments.of("Start with invalid symbol", "$Money", false),
                Arguments.of("Spaces", "Hello World", false),
                Arguments.of("Just a Space", " ", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumber(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumber() {
        return Stream.of(
                Arguments.of("Just a digit", "1", true),
                Arguments.of("Start with -", "-1", true),
                Arguments.of("Start with +", "+1", true),
                Arguments.of("Multiple Digits", "42069", true),
                Arguments.of("Negative and decimals", "-1.0", true),
                Arguments.of("Positive and decimals", "+01.00", true),
                Arguments.of("Starting with zero decimal", "007.000", true),
                Arguments.of("Start with a different symbol", "*15", false),
                Arguments.of("Incomplete Decimal", "1.", false),
                Arguments.of("Incomplete Decimal 2", ".5", false),
                Arguments.of("More than one decimal point", "1..05", false),
                Arguments.of("More than one decimal point 2", "1.0.5", false),
                Arguments.of("Multiple symbols", "++105", false),
                Arguments.of("Multiple symbols 2", "-10.5-2", false),
                Arguments.of("End with a symbol", "300+", false),
                Arguments.of("Letters", "two", false),
                Arguments.of("Letters and numbers", "2.three", false),
                Arguments.of("Multiple symbols 3", "@.^&", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testString(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("Empty string", "\"\"", true),
                Arguments.of("Letters", "\"abc\"", true),
                Arguments.of("Alphanumeric", "\"abc123\"", true),
                Arguments.of("Uppercase", "\"ABCD\"", true),
                Arguments.of("Valid Escape Sequence", "\"Hello,\\nWorld!\"", true),
                Arguments.of("Numbers and symbols", "\"123&%$%\"", true),
                Arguments.of("Spaces", "\"Hello     World\"", true),
                Arguments.of("More Escape Sequences", "\"\\b\\'\\\\n\\t\"", true),
                Arguments.of("Missing Quotation", "\"unterminated", false),
                Arguments.of("Invalid Escape", "\"invalid\\escape\"", false),
                Arguments.of("Invalid Escape Caps", "\"hello\\Nworld\"", false),
                Arguments.of("No Quotations", "Hello World", false),
                Arguments.of("Multiple Quotations", "\"\"Hello World\"\"", false),
                Arguments.of("Multiple Quotations 2", "\"Hello\",World!\"", false),
                Arguments.of("Single Quotation No Escape", "\"Hello\',World!\"", false),
                Arguments.of("Outside of Quotations", "Hello \" World!\"", false)
        );
    }

    /**
     * Asserts that the input matches the given pattern and returns the matcher
     * for additional assertions.
     */
    private static Matcher test(String input, Pattern pattern, boolean success) {
        Matcher matcher = pattern.matcher(input);
        Assertions.assertEquals(success, matcher.matches());
        return matcher;
    }


}
