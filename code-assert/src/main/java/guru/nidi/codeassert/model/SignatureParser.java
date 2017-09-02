/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.model;

import guru.nidi.codeassert.AnalyzerException;

import java.util.*;

/**
 * Parse a java type signature.
 * see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4
 */
final class SignatureParser {
    public enum Source {
        CLASS, FIELD, METHOD
    }

    private static final char EOF = (char) -1;
    private static final String NOT_IDENT = ".;[/<>:";
    private static final String BASE_TYPES = "BCDFIJSZ";

    private final String s;
    private char c;
    private int pos;
    private final Set<String> classes = new HashSet<>();

    private SignatureParser(String s) {
        this.s = s;
        pos = 0;
        read();
    }

    public static SignatureParser parseSignature(Source source, String signature) {
        final SignatureParser parser = new SignatureParser(signature);
        switch (source) {
            case CLASS:
                parser.classSignature();
                break;
            case FIELD:
                parser.fieldTypeSignature(false);
                break;
            case METHOD:
                parser.methodTypeSignature();
                break;
            default:
                //nothing
        }
        return parser;
    }

    public Collection<String> getClasses() {
        return classes;
    }

    private void classSignature() {
        if (is('<')) {
            formalTypeParameters();
        }
        do {
            classTypeSignature();
        } while (!is(EOF));
    }

    private void formalTypeParameters() {
        read('<');
        do {
            formalTypeParameter();
        } while (!is('>'));
        read('>');
    }

    private void formalTypeParameter() {
        identifier();
        classBound();
        while (is(':')) {
            interfaceBound();
        }
    }

    private void classBound() {
        read(':');
        fieldTypeSignature(true);
    }

    private void interfaceBound() {
        read(':');
        fieldTypeSignature(false);
    }

    private void fieldTypeSignature(boolean opt) {
        if (!classTypeOrTypeVariableSignature()) {
            if (is('[')) {
                arrayTypeSignature();
            } else if (!opt) {
                throw new AnalyzerException("FieldTypeSignature expected [" + s + "]:" + pos);
            }
        }
    }

    private void classTypeSignature() {
        read('L');
        final StringBuilder s = new StringBuilder();
        s.append(classIdentifier());
        while (!is(';') && !is('<')) {
            if (is('$')) {
                read();
                classIdentifier();
            } else {
                s.append('.');
                read();
                s.append(classIdentifier());
            }
        }
        final String id = s.toString();
        if (is('<')) {
            typeArguments();
        }
        while (is('.')) {
            classTypeSignatureSuffix();
        }
        classes.add(id);
        read(';');
    }

    private void classTypeSignatureSuffix() {
        read('.');
        classIdentifier();
        if (is('<')) {
            typeArguments();
        }
    }

    private void typeArguments() {
        read('<');
        do {
            typeArgument();
        } while (!is('>'));
        read('>');
    }

    private void typeArgument() {
        if (is('*')) {
            read('*');
        } else {
            if (is('+')) {
                read('+');
            } else if (is('-')) {
                read('-');
            }
            fieldTypeSignature(false);
        }
    }

    private void arrayTypeSignature() {
        read('[');
        typeSignature();
    }

    private void typeSignature() {
        if (isBaseType()) {
            read();
        } else {
            fieldTypeSignature(false);
        }
    }

    private boolean isBaseType() {
        return BASE_TYPES.indexOf(c) >= 0;
    }

    private void typeVariableSignature() {
        read('T');
        identifier();
        read(';');
    }

    private void methodTypeSignature() {
        if (is('<')) {
            formalTypeParameters();
        }
        read('(');
        while (!is(')')) {
            typeSignature();
        }
        read(')');
        returnType();
        while (is('^')) {
            throwsSignature();
        }
    }

    private void throwsSignature() {
        read('^');
        if (!classTypeOrTypeVariableSignature()) {
            throw new AnalyzerException("ClassType or TypeVariable signature expected [" + s + "]:" + pos);
        }
    }

    private boolean classTypeOrTypeVariableSignature() {
        if (is('L')) {
            classTypeSignature();
            return true;
        }
        if (is('T')) {
            typeVariableSignature();
            return true;
        }
        return false;
    }

    private void returnType() {
        if (is('V')) {
            read();
        } else {
            typeSignature();
        }
    }

    private String classIdentifier() {
        return identifier(true);
    }

    private String identifier() {
        return identifier(false);
    }

    private String identifier(boolean clazz) {
        final StringBuilder s = new StringBuilder();
        do {
            s.append(c);
            read();
        } while (NOT_IDENT.indexOf(c) < 0 && (!clazz || c != '$'));
        return s.toString();
    }

    private boolean is(char ch) {
        return c == ch;
    }

    private char read() {
        return c = (pos == s.length() ? EOF : s.charAt(pos++));
    }

    private char read(char ch) {
        if (c != ch) {
            throw new AnalyzerException("'" + ch + "' expected in '" + s + "':" + pos);
        }
        return read();
    }
}
