package io.github.wreulicke.errorprone.imports;

import static org.junit.jupiter.api.Assertions.*;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ImportControlTest {

  @AfterEach
  void tearDown() {
    System.clearProperty("import-control.rules");
  }

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

  @Test
  void testFullQualifiedReference() {
    System.setProperty("import-control.rules", "src/test/resources/import-control.yaml");
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ImportControl.class, getClass());
    compilationHelper
        .addSourceLines(
            "com/exmaple/myapp/TestService.java",
            """
            package com.example.myapp.service;

            public class TestService{

              public static void builder() {}
            }
            """)
        .addSourceLines(
            "com/exmaple/myapp/TestRepository.java",
            """
            package com.example.myapp.repository;

            public class TestRepository {

              // BUG: Diagnostic contains: This import violates import control rule
              private final com.example.myapp.service.TestService service = null;

              // BUG: Diagnostic contains: This import violates import control rule
              private static final com.example.myapp.service.TestService staticService = null;

              void test() {
                // BUG: Diagnostic contains: This import violates import control rule
                com.example.myapp.service.TestService service = new com.example.myapp.service.TestService();

                // BUG: Diagnostic contains: This import violates import control rule
                com.example.myapp.service.TestService.builder();
              }

              // BUG: Diagnostic contains: This import violates import control rule
              com.example.myapp.service.TestService test2() {
                return null;
              }

              // BUG: Diagnostic contains: This import violates import control rule
              void test3(com.example.myapp.service.TestService service) {
              }

              void test4() {
                // BUG: Diagnostic contains: This import violates import control rule
                var x = com.example.myapp.service.TestService.class;
              }
            }
            """)
        .doTest();
  }

  @Test
  void test_safe() {
    System.setProperty("import-control.rules", "src/test/resources/import-control.yaml");
    CompilationTestHelper compilationHelper =
        CompilationTestHelper.newInstance(ImportControl.class, getClass());
    compilationHelper
        .addSourceLines(
            "com/example/myapp/service.java",
            """
            package com.example.myapp;
            public final class service {
              public static final class TestService {
                public static void builder() {}
              }
            }
            """)
        .addSourceLines(
            "com/exmaple/myapp/TestRepository.java",
            """
            package com.example.myapp.repository;

            public class TestRepository {
              void test4() {
                var x = com.example.myapp.service.TestService.class;
              }
            }
            """)
        .doTest();
  }
}
