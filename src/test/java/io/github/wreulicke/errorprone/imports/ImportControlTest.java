package io.github.wreulicke.errorprone.imports;

import static org.junit.jupiter.api.Assertions.*;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

class ImportControlTest {

  @Test
  void testWithFlags() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ImportControl.class, getClass())
            .setArgs("-XepOpt:ImportControl:RuleFile=src/test/resources/import-control.yaml");
    compilationHelper
        .addSourceLines(
            "com/exmaple/myapp/TestService.java",
            """
            package com.example.myapp.service;

            public class TestService{
            }
            """)
        .addSourceLines(
            "com/exmaple/myapp/TestRepository.java",
            """
        package com.example.myapp.repository;

        // BUG: Diagnostic contains: This import violates import control rule
        import com.example.myapp.service.TestService;

        public class TestRepository {
        }
        """)
        .doTest();
  }

  @Test
  void testWithSystemProperty() {
    System.setProperty("import-control.rules", "src/test/resources/import-control.yaml");
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ImportControl.class, getClass());
    compilationHelper
        .addSourceLines(
            "com/exmaple/myapp/TestService.java",
            """
            package com.example.myapp.service;

            public class TestService{
            }
            """)
        .addSourceLines(
            "com/exmaple/myapp/TestRepository.java",
            """
            package com.example.myapp.repository;

            // BUG: Diagnostic contains: This import violates import control rule
            import com.example.myapp.service.TestService;

            public class TestRepository {
            }
            """)
        .doTest();
  }

  @Test
  void test_RuleFileDoesNotExist() {
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ImportControl.class, getClass())
            .setArgs("-XepOpt:ImportControl:RuleFile=file-does-not-exist.yaml");
    compilationHelper
        .addSourceLines(
            "com/exmaple/myapp/TestService.java",
            """
            package com.example.myapp.service;

            public class TestService{
            }
            """)
        .addSourceLines(
            "com/exmaple/myapp/TestRepository.java",
            """
            package com.example.myapp.repository;

            import com.example.myapp.service.TestService;

            public class TestRepository {
            }
            """)
        .doTest();
  }
}
