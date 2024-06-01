# errorprone-import-control

Control imports like checkstyle's ImportControl.

## Install

### Maven

TBD

### Gradle

```ruby
dependencies {
  annotationProcessor 'io.github.wreulicke:errorprone-import-control:0.0.3'
  # or you can write below when you use net.ltgt.errorprone plugin
  errorprone 'io.github.wreulicke:errorprone-import-control:0.0.3'
}
```

## Rules

- [ImportControl](#importcontrol)

### ImportControl

ImportControl rule restrict imports like [checkstyle's ImportControl](https://checkstyle.sourceforge.io/checks/imports/importcontrol.html).
Currently, this only supports deny rule.

```yaml
- packageRegex: "com\\.example\\.myapp\\.repository"
  deny:
    - "com\\.example\\.myapp\\.service\\..*"
```

```java
package com.example.myapp.repository;

// invalid: This import violates import control rule
import com.example.myapp.service.TestService;

public class TestRepository {
}
```

## License

MIT License