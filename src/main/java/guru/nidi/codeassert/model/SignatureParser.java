/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4
 */
class SignatureParser {
    private static final char EOF = (char) -1;
    private static final String NOT_IDENT = ".;[/<>:";
    private static final String BASE_TYPES = "BCDFIJSZ";

    private final String s;
    private char c;
    private int pos;
    private Set<String> packages = new HashSet<>();

    private SignatureParser(String s) {
        this.s = s;
        pos = 0;
        read();
    }

    public static SignatureParser parseClassSignature(String signature) {
        final SignatureParser parser = new SignatureParser(signature);
        parser.classSignature();
        return parser;
    }


    public static SignatureParser parseFieldSignature(String signature) {
        final SignatureParser parser = new SignatureParser(signature);
        parser.fieldTypeSignature(false);
        return parser;
    }

    public static SignatureParser parseMethodSignature(String signature) {
        final SignatureParser parser = new SignatureParser(signature);
        parser.methodTypeSignature();
        return parser;
    }

    public Collection<String> getPackages() {
        return packages;
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
        if (is('L')) {
            classTypeSignature();
        } else if (is('T')) {
            typeVariableSignature();
        } else if (is('[')) {
            arrayTypeSignature();
        } else if (!opt) {
            throw new RuntimeException("FieldTypeSignature expected [" + s + "]:" + pos);
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
        String id = s.toString();
        if (is('<')) {
            typeArguments();
        }
        if (is('.')) {
            while (is('.')) {
                classTypeSignatureSuffix();
            }
        }
        final int pos = id.lastIndexOf('.');
        packages.add(pos < 0 ? id : id.substring(0, pos));
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
        if (is('L')) {
            classTypeSignature();
        } else if (is('T')) {
            typeVariableSignature();
        } else {
            throw new RuntimeException("ClassType or TypeVariable signature expected [" + s + "]:" + pos);
        }
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
        StringBuilder s = new StringBuilder();
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
            throw new RuntimeException("'" + ch + "' expected in '" + s + "':" + pos);
        }
        return read();
    }
}
