package io.github.wreulicke.errorprone.imports;

import java.util.List;
import java.util.regex.Pattern;

public record ImportRule(Pattern packageRegex, List<Pattern> deny) {}
