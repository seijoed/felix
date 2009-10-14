/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.karaf.shell.commands;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.karaf.shell.console.OsgiCommandSupport;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.gogo.commands.Command;
import org.fusesource.jansi.Ansi;


@Command(scope = "shell", name="grep", description="Prints lines matching the given pattern")
public class GrepAction extends OsgiCommandSupport {

    public static enum ColorOption {
        never,
        always,
        auto
    }

    @Argument(index = 0, name = "pattern", description = "Regular expression", required = true, multiValued = false)
    private String regex;

    @Option(name = "-n", aliases = { "--line-number" }, description = "Prefixes each line of output with the line number within its input file.", required = false, multiValued = false)
    private boolean lineNumber;

    @Option(name = "-v", aliases = { "--invert-match" }, description = "Inverts the sense of matching, to select non-matching lines.", required = false, multiValued = false)
    private boolean invertMatch;

    @Option(name = "-w", aliases = { "--word-regexp" }, description = "Selects only those lines containing matches that form whole " +
                                                                      "words.  The test is that the matching substring must either be " +
                                                                      "at  the beginning of the line, or preceded by a non-word constituent " +
                                                                      "character.  Similarly, it must be either at the end of " +
                                                                      "the line or followed by a non-word constituent character.  " +
                                                                      "Word-constituent characters are letters, digits, and the underscore.", required = false, multiValued = false)
    private boolean wordRegexp;

    @Option(name = "-x", aliases = { "--line-regexp" }, description = "Selects only those matches that exactly match the whole line.", required = false, multiValued = false)
    private boolean lineRegexp;

    @Option(name = "-i", aliases = { "--ignore-case" }, description = "Ignores case distinctions in both the PATTERN and the input files.", required = false, multiValued = false)
    private boolean ignoreCase;

    @Option(name = "-c", aliases = { "--count" }, description = "only print a count of matching lines per FILE", required = false, multiValued = false)
    private boolean count;

    @Option(name = "--color", aliases = { "--colour" }, description = "use markers to distinguish the matching string. WHEN may be `always', `never' or `auto'", required = false, multiValued = false)
    private ColorOption color = ColorOption.auto;

    @Option(name = "-B", aliases = { "--before-context" }, description = "Print NUM lines of leading context before matching lines.  Places a line containing -- between contiguous groups of matches.", required = false, multiValued = false)
    private int before = -1;

    @Option(name = "-A", aliases = { "--after-context" }, description = "Print NUM lines of trailing context after matching lines.  Places a line containing -- between contiguous groups of matches.", required = false, multiValued = false)
    private int after = -1;

    @Option(name = "-C", aliases = { "--context" }, description = "Print NUM lines of output context.  Places a line containing -- between contiguous groups of matches.", required = false, multiValued = false)
    private int context = 0;


    protected Object doExecute() throws Exception {
        if (after < 0) {
            after = context;
        }
        if (before < 0) {
            before = context;
        }
        List<String> lines = new ArrayList<String>();

        String regexp = regex;
        if (wordRegexp) {
            regexp = "\\b" + regexp + "\\b";
        }
        if (lineRegexp) {
            regexp = "^" + regexp + "$";
        } else {
            regexp = ".*" + regexp + ".*";
        }
        if (ignoreCase) {
            regexp = regexp.toUpperCase();
        }
        Pattern p = Pattern.compile(regexp);
        Pattern p2 = Pattern.compile(regex);
        try {
            boolean firstPrint = true;
            int nb = 0;
            int lineno = 1;
            String line;
            int lineMatch = 0;
            Reader r = new InputStreamReader(System.in);
            while ((line = readLine(r)) != null) {
                String pattern = ignoreCase ? line.toUpperCase() : line;
                if (p.matcher(pattern).matches() ^ invertMatch) {

                    if (!count && lineNumber) {
                        System.out.print(String.format("%6d  ", lineno++));
                    }

                    Matcher matcher2 = p2.matcher(pattern);
                    StringBuffer sb = new StringBuffer();
                    while (matcher2.find()) {
                        if (!invertMatch && color != ColorOption.never) {
                            matcher2.appendReplacement(sb, Ansi.ansi()
                                .bg(Ansi.Color.YELLOW)
                                .fg(Ansi.Color.BLACK)
                                .a(matcher2.group())
                                .reset().toString());
                        } else {
                            matcher2.appendReplacement(sb, matcher2.group());
                        }
                        nb++;
                    }
                    matcher2.appendTail(sb);
					lines.add(sb.toString());
					lineMatch = lines.size();
                    lineno++;
                } else {
                    if (lineMatch != 0 & lineMatch + after + before <= lines.size()) {
                        if (!count) {
                            if (!firstPrint && before + after > 0) {
                                System.out.println("--");
                            } else {
                                firstPrint = false;
                            }
                            for (int i = 0; i < lineMatch + after; i++) {
                                System.out.println(lines.get(i));
                            }
                        }
                        while (lines.size() > before) {
                            lines.remove(0);
                        }
                        lineMatch = 0;
                    }
                    lines.add(line);
                    while (lineMatch == 0 && lines.size() > before) {
                        lines.remove(0);
                    }
                }
            }
            if (!count && lineMatch > 0) {
                if (!firstPrint && before + after > 0) {
                    System.out.println("--");
                } else {
                    firstPrint = false;
                }
                for (int i = 0; i < lineMatch + after; i++) {
                    System.out.println(lines.get(i));
                }
            }
            if (count) {
                System.out.println(nb);
            }
        } catch (IOException e) {
        }
        return null;
    }

    private String readLine(Reader in) throws IOException {
        StringBuffer buf = new StringBuffer();
        while (true) {
            int i = in.read();
            if (i == -1 && buf.length() == 0) {
                return null;
            }
            if (i == -1 || i == '\n' || i == '\r') {
                return buf.toString();
            }
            buf.append((char) i);
        }
    }

}