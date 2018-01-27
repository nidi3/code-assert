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

import guru.nidi.codeassert.config.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

import static guru.nidi.codeassert.model.SourceFileParser.State.*;

abstract class SourceFileParser {
    private static final Logger LOG = LoggerFactory.getLogger(SourceFileParser.class);
    static final String SLASH_SLASH = "//";
    static final String QUOTE = "\"";
    static final String TRIPLE_QUOTE = "\"\"\"";
    static final String SLASH_STAR = "/*";
    static final String STAR_SLASH = "*/";

    enum State {
        CODE, STRING, LINE_STRING, COMMENT, LINE_COMMENT
    }

    State state;
    String line;
    int pos;
    String token;
    int codeLines;
    int commentLines;
    int emptyLines;
    int totalLines;

    static CodeClass parse(CodeClass clazz, File file, Charset charset) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            final Language language = Language.byFilename(file.getName());
            if (language == null) {
                LOG.info("Unknown source file type {}. Ignoring it", file);
            } else {
                parse(clazz, language, in, charset);
            }
            return clazz;
        }
    }

    static CodeClass parse(CodeClass clazz, Language language, InputStream is, Charset charset) throws IOException {
        try (Reader in = new InputStreamReader(is, charset)) {
            return parse(clazz, language, in);
        }
    }

    static CodeClass parse(CodeClass clazz, Language language, Reader reader) throws IOException {
        try (BufferedReader in = new BufferedReader(reader)) {
            final SourceFileParser parser = parser(language);
            if (parser == null) {
                LOG.info("No parser for language {}. Ignoring it", language);
            } else {
                parser.parse(in);
                new CodeClassBuilder(clazz)
                        .addSourceSizes(parser.codeLines, parser.commentLines, parser.emptyLines, parser.totalLines);
            }
            return clazz;
        }
    }

    private static SourceFileParser parser(Language language) {
        switch (language) {
            case JAVA:
                return new JavaParser();
            case KOTLIN:
                return new KotlinParser();
            default:
                return null;
        }
    }

    void parse(BufferedReader in) throws IOException {
        state = CODE;
        while ((line = in.readLine()) != null) {
            pos = 0;
            totalLines++;
            if (line.trim().length() == 0) {
                emptyLines++;
            }
            parseLine();
            if (state == LINE_COMMENT || state == LINE_STRING) {
                state = CODE;
            }
        }
    }

    void parseLine() {
        boolean comment = state == COMMENT;
        boolean code = false;
        do {
            doParse();
            code |= (!comment && pos > 2)
                    || (state != COMMENT && state != LINE_COMMENT && (!comment || pos < line.length()));
            comment |= state == COMMENT || state == LINE_COMMENT;
        } while (pos > 0 && pos < line.length());
        if (comment) {
            commentLines++;
        }
        if (code) {
            codeLines++;
        }
    }

    protected boolean findToken(String... tokens) {
        int tp = -1;
        for (final String tok : tokens) {
            final int index = line.indexOf(tok, pos);
            if (index >= 0 && (tp < 0 || index < tp)) {
                token = tok;
                tp = index;
            }
        }
        if (tp < 0) {
            pos = -1;
            return false;
        }
        pos = tp + token.length();
        return true;
    }

    protected abstract void doParse();

    static class JavaParser extends SourceFileParser {
        public void doParse() {
            if (findToken(SLASH_SLASH, SLASH_STAR, STAR_SLASH, QUOTE)) {
                if (state == COMMENT || state == LINE_COMMENT) {
                    if (token.equals(STAR_SLASH)) {
                        state = CODE;
                    }
                } else if (state == LINE_STRING) {
                    if (token.equals(QUOTE) && (pos == 0 || line.charAt(pos - 2) != '\\')) {
                        state = CODE;
                    }
                } else if (token.equals(QUOTE)) {
                    state = LINE_STRING;
                } else if (token.equals(SLASH_STAR)) {
                    state = COMMENT;
                } else if (token.equals(SLASH_SLASH)) {
                    state = LINE_COMMENT;
                }
            }
        }
    }

    static class KotlinParser extends SourceFileParser {
        private int nesting;

        public void doParse() {
            if (findToken(SLASH_SLASH, SLASH_STAR, STAR_SLASH, TRIPLE_QUOTE, QUOTE)) {
                if (state == COMMENT || state == LINE_COMMENT) {
                    if (token.equals(STAR_SLASH)) {
                        nesting--;
                        if (nesting == 0) {
                            state = CODE;
                        }
                    } else if (token.equals(SLASH_STAR)) {
                        state = COMMENT;
                        nesting++;
                    }
                } else if (state == STRING) {
                    if (token.equals(TRIPLE_QUOTE)) {
                        state = CODE;
                    }
                } else if (token.equals(TRIPLE_QUOTE)) {
                    state = STRING;
                } else if (state == LINE_STRING) {
                    if (token.equals(QUOTE) && (pos == 0 || line.charAt(pos - 2) != '\\')) {
                        state = CODE;
                    }
                } else if (token.equals(QUOTE)) {
                    state = LINE_STRING;
                } else if (token.equals(SLASH_STAR)) {
                    state = COMMENT;
                    nesting++;
                } else if (token.equals(SLASH_SLASH)) {
                    state = LINE_COMMENT;
                }
            }
        }
    }
}
