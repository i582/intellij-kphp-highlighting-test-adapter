# intellij-kphp-highlighting-test-adapter

Library for testing highlighting in plugins for IDEA in KPHP format.

## Installation

### Kotlin DSL

```kotlin
plugins {
    id("com.vk.intellij-kphp-highlighting-test-adapter") version "0.1.0"
}
```

### Gradle DSL

```groovy
plugins {
    id 'com.vk.intellij-kphp-highlighting-test-adapter' version "0.1.0"
}
```

## Test Format

```text
<code line>
//    ^^^^ <- range in line
//    <type>: <message>
```

Example:

```php
$a = new Message();
//       ^^^^^^^
//       error: Undefined class 'Message'
```

Message can be multiline:

```php
echo($a);
//   ^^
//   error: Can't find variable: $a
//          Maybe you meant $b?
```

## Motivation

The standard test format for IDEA requires describing the error directly in the code using XML.
Because of this, the standard highlighting in the IDE breaks and gives a lot of errors.

Therefore, we came up with this format for tests in IDEA.
It allows you to conveniently describe errors in the code in the form of comments,
while the highlighting doesn't break.

## License

This project is under the MIT License. See the 
[LICENSE](./LICENSE)
file for the full license text.
