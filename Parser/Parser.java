package plc.interpreter;

import javax.swing.event.ListDataEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Parser {

    private final TokenStream tokens;

    private Parser(String input) {
        tokens = new TokenStream(Lexer.lex(input));
    }


    public static Ast parse(String input) {
        return new Parser(input).parse();
    }

    private Ast parse() {
        List<Ast> trees = new ArrayList<>();
        while(tokens.has(0))
        {
            trees.add(parseAst());
        }
        return new Ast.Term("source",trees);
    }

    private Ast parseAst() {
        if(peek("(") || peek("["))
            return parseTerm();
        else if(peek(Token.Type.NUMBER))
            return parseNumberLit();
        else if(peek(Token.Type.STRING))
            return parseStringLit();
        else if(peek(Token.Type.IDENTIFIER))
            return parseIdentifier();
        throw new ParseException("Cannot Parse, not a valid Token", tokens.get(0).getIndex());
    }

    private Ast.Identifier parseIdentifier() {
        if(match(Token.Type.IDENTIFIER))
        {
            String name = tokens.get(-1).getLiteral();
            return new Ast.Identifier(name);
        }
        throw new ParseException("Not an Identifier", tokens.get(-1).getIndex());
    }

    private Ast.NumberLiteral parseNumberLit() {
        if(match(Token.Type.NUMBER))
        {
            BigDecimal number = new BigDecimal(tokens.get(-1).getLiteral());
            return new Ast.NumberLiteral(number);
        }
        throw new ParseException("Not an Number Literal", tokens.get(-1).getIndex());
    }

    private Ast.StringLiteral parseStringLit() {
        if(match(Token.Type.STRING))
        {
            String string = tokens.get(-1).getLiteral();
            string = string.substring(1 , string.length() - 1);
            string = string.replace("\\b","\b");
            string = string.replace("\\n","\n");
            string = string.replace("\\r","\r");
            string = string.replace("\\t","\t");
            string = string.replace("\\\"","\"");
            string = string.replace("\\'","\'");
            string = string.replace("\\\\","\\");
            return new Ast.StringLiteral(string);
        }
        throw new ParseException("Not an String Literal", tokens.get(-1).getIndex());
    }

    private Ast.Term parseTerm()
    {
        boolean isPar = false;
        List<Ast> args = new ArrayList<>();
        String name;
        if((match("(")) && peek(Token.Type.IDENTIFIER)) {
            isPar = true;
            name = parseIdentifier().getName();
        }
        else if ((match("[")) && peek(Token.Type.IDENTIFIER))
            name = parseIdentifier().getName();
        else
            throw new ParseException("Not a valid term, missing name", tokens.get(-1).getIndex());
        while(!peek(")") && !peek("]"))
        {
            if(peek(Token.Type.NUMBER))
                args.add(parseNumberLit());
            else if(peek(Token.Type.STRING))
                args.add(parseStringLit());
            else if(peek(Token.Type.IDENTIFIER))
                args.add(parseIdentifier());
            else if(peek("(") || peek("["))
                args.add(parseTerm());
            else
                throw new ParseException("Not a term, missing valid token", tokens.get(-1).getIndex());
        }
        if ((match(")") && isPar) || (match("]") && !isPar))
            return new Ast.Term(name, args);
        throw new ParseException("Not a term, missing closing parenthesis/bracket", tokens.get(-1).getIndex());
    }

    private boolean peek(Object... patterns) {
        if (tokens.has(patterns.length - 1))
        {
            for (int i = 0; i < patterns.length; i++)
            {
                if(!patterns[i].equals(tokens.get(i).getType()) && !patterns[i].equals(tokens.get(i).getLiteral()))
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        if (tokens.has(patterns.length - 1))
        {
            for (int i = 0; i < patterns.length; i++)
            {
                if(!patterns[i].equals(tokens.get(i).getType()) && !patterns[i].equals(tokens.get(i).getLiteral()))
                    return false;
            }
            for (int i = 0; i < patterns.length; i++)
                tokens.advance();
            return true;
        }
        return false;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            if (tokens.size() - 1 < (index + offset))
                return false;
            return true;
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            if(has(offset))
                return tokens.get(index + offset);
            throw new ParseException("Index out of bounds", index);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }
    }
}
