package io.github.wreulicke.errorprone.imports;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.ErrorProneFlags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.ImportTree;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.inject.Inject;

@AutoService(BugChecker.class)
@BugPattern(
    summary = "Import control rule",
    severity = BugPattern.SeverityLevel.ERROR,
    link = "github.com/wreulicke/errorprone-import-control",
    linkType = BugPattern.LinkType.CUSTOM)
public class ImportControl extends BugChecker implements BugChecker.ImportTreeMatcher {

  private final List<ImportRule> rules;

  @Inject
  public ImportControl(ErrorProneFlags flags) {
    String property = System.getProperty("import-control.rules");
    String path =
        property == null
            ? flags.get("ImportControl:RuleFile").orElse(".import-control.yaml")
            : property;
    Path fpath = Path.of(path);

    YAMLMapper yamlParser = new YAMLMapper();
    List<ImportRule> rules;
    try (Reader reader = Files.newBufferedReader(fpath, StandardCharsets.UTF_8)) {
      rules = yamlParser.readValue(reader, new TypeReference<>() {});
    } catch (IOException e) {
      this.rules = List.of();
      Logger.getLogger(ImportControl.class.getName())
          .warning("cannot read import rules: " + e.getMessage());
      return;
    }
    this.rules = rules;
  }

  @Override
  public Description matchImport(ImportTree tree, VisitorState state) {
    if (rules.isEmpty()) {
      return Description.NO_MATCH;
    }

    String packageName =
        state.getSourceForNode(state.getPath().getCompilationUnit().getPackageName());
    Optional<ImportRule> found =
        rules.stream()
            .filter(rule -> rule.packageRegex().matcher(packageName).matches())
            .findFirst();
    if (found.isEmpty()) {
      return Description.NO_MATCH;
    }

    String imporz = state.getSourceForNode(tree.getQualifiedIdentifier());
    if (imporz == null) {
      return Description.NO_MATCH;
    }

    ImportRule importRule = found.orElseThrow();
    for (Pattern d : importRule.deny()) {
      if (d.matcher(imporz).matches()) {
        return buildDescription(tree)
            .setMessage("This import violates import control rule")
            .build();
      }
    }

    return Description.NO_MATCH;
  }
}
