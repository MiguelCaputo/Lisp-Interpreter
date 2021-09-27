package plc.interpreter;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException}.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers, they're not necessary but their use will make the implementation a
 * lot easier. Regex isn't the most performant way to go but it gets the job
 * done, and the focus here is on the concept.
 */
public final class Lexer {

    final CharStream chars;

    Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Lexes the input and returns the list of tokens.
     */
    static List<Token> lex(String input) throws ParseException {
        return new Lexer(input).lex();
    }

    /**
     * Repeatedly lexes the next token using {@link #lexToken()} until the end
     * of the input is reached, returning the list of tokens lexed. This should
     * also handle skipping whitespace.
     */
    List<Token> lex() throws ParseException {
        List<Token> list = new ArrayList<>();
        while (chars.has(0)) {
            if (!peek("[ \n\r\t]")) {
                list.add(lexToken());
                if(peek("[ \n\r\t]")) {
                    chars.advance();
                }
            }
            else
                chars.advance();
        }
        return list;
    }

    Token lexToken() throws ParseException {
        chars.reset();
        if (peek("[^A-Za-z0-9+\\-*\\/:\\.!_?<>= \n\r\t\"]")) {
            Token token = chars.emit(Token.Type.OPERATOR);
            chars.advance();
            return token;
        }
        else if (peek("[A-Za-z*\\/:!_?<>=]"))
            return lexIdentifier();
        else if (peek("\""))
            return lexString();
        else if (peek("[0-9]"))
            return lexNumber();
        else if (peek("[+\\-]")) {
            if (chars.has(1)) {
                if (peek("[+\\-]", "[0-9]"))
                    return lexNumber();
                else if (peek("[+\\-]","[A-Za-z*\\/:!_?+\\-<>\\.=]"))
                    return lexIdentifier();
            }
            return lexIdentifier();
        }
        else if(peek("[\\.]"))
        {
            if (chars.has(1) && !peek("\\.","[^A-Za-z0-9+\\-*\\/:\\.!_?<>=]"))
                return lexIdentifier();
            else {
                Token token = chars.emit(Token.Type.OPERATOR);
                chars.advance();
                return token;
            }
        }
        else
            throw new ParseException("Not a valid Token", chars.index);

    }

    Token lexNumber() {
        boolean isDot = false;
        if (match("[\\-+]") || peek("[0-9]")) {
            while (peek("[0-9]") || peek("[\\.]")) {
                if (peek("[\\.]")) {
                    if (isDot || !peek(".","[0-9]"))
                        return chars.emit(Token.Type.NUMBER);
                    match("[\\.]");
                    isDot = true;
                }
                else if (match("[0-9]")) {}
            }
            return chars.emit(Token.Type.NUMBER);
        }
        throw new ParseException("Not a valid Number", chars.index);
    }

    Token lexIdentifier() {
        if(match("[A-Za-z0-9+\\-*\\/:\\.!_?<>=]"))
        {
            while(match("[A-Za-z0-9+\\-*\\/\\.:!_?<>=]"))
            {
                continue;
            }
            return chars.emit(Token.Type.IDENTIFIER);
        }
        throw new ParseException("Not a valid Identifier", chars.index);
    }

    Token lexString() throws ParseException {
        int index = chars.index;
        if (!match("\""))
            throw new ParseException("Not a valid Token", index);
        try {
            while (!peek("[\"]")) {
                if (match(("[^\\\\\"']?"))) {
                } else if (match("\\\\", "[bnrt'\"\\\\]")) {
                } else {
                    throw new ParseException("Not a valid Token", index);
                }
            }
        }
        catch (ParseException e)
        {
            throw new ParseException("Not a valid String", index);
        }
        if (match("['\"']")) {
            return chars.emit(Token.Type.STRING);
        }
        throw new ParseException("Not a valid String", index);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    boolean peek(String... patterns) {
        if (chars.has(patterns.length - 1))
        {
            String test;
            for (int i = 0; i < patterns.length; i++)
            {
                test = Character.toString(chars.get(i));
                if (!test.matches(patterns[i]))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns true in the same way as peek, but also advances the CharStream to
     * if the characters matched.
     */
    boolean match(String... patterns) {
        if (chars.has(patterns.length - 1))
        {
            String test;
            for (int i = 0; i < patterns.length; i++)
            {
                test = Character.toString(chars.get(i));
                if (!test.matches(patterns[i])) {
                    return false;
                }
            }
            for (int i = 0; i < patterns.length; i++)
                chars.advance();
            return true;
        }
        return false;
    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    static final class CharStream {

        final String input;
        int index = 0;
        int length = 0;

        CharStream(String input) {
            this.input = input;
        }

        /**
         * Returns true if there is a character at index + offset.
         */
        boolean has(int offset) {
            if (input.length() - 1 < (index + offset))
                return false;
            return true;
        }

        /**
         * Gets the character at index + offset.
         */
        char get(int offset) {
            if(has(offset))
                return input.charAt(index + offset);
            throw new ParseException("Out of bounds", index);
        }

        /**
         * Advances to the next character, incrementing the current index and
         * length of the literal being built.
         */
        void advance() {
            index++;
            length++;
        }

        /**
         * Resets the length to zero, skipping any consumed characters.
         */
        void reset() {
            length = 0;
        }

        /**
         * Returns a token of the given type with the built literal and resets
         * the length to zero. The index of the token should be the
         * <em>starting</em> index.
         */
        Token emit(Token.Type type) {
            Token token;
            if (type.equals(Token.Type.OPERATOR))
                token = new Token(type, input.substring(index, index + 1), index);
            else
                token = new Token(type, input.substring(index - length, index), index - length);
            reset();
            return token;
        }
    }
}

