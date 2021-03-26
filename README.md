Jar Jar Abrams CLI
==============

A CLI wrapper around [Jar Jar Abrams](https://github.com/eed3si9n/jarjar-abrams). Builds a command line utility which can process and shade JAR files.

See Releases for downloads

## Usage
```
$ jarjar-abrams-cli -h
  -j, --jar  <arg>     The JAR to process
  -o, --out  <arg>     The output JAR path
  -r, --rules  <arg>   The JSON rules file containing jarjar rules
  -v, --verbose        Run in verbose mode
  -h, --help           Show help message
```

Jar Jar Abrams CLI takes in a `rules.json` file in the following format:

```
{
  "rename": [{"from": "<pattern>", "to": "<result>"}, ...],
  "zap": ["<pattern>", ...],
  "keep": ["<pattern>", ...]
}
```

These patterns are in the same format as described by [jarjar-links](https://github.com/pantsbuild/jarjar/blob/master/src/main/java/org/pantsbuild/jarjar/help.txt)

### Example Usage

```sh
$ cat rules.json
{
  "rename": [{"from": "com.google.protobuf.**", "to": "shaded.com.google.protobuf.@1"}]
}
```
```sh
$ jarjar-abrams-cli -r rules.json -j protobuf-java-3.5.1.jar -o test.jar
```


## Building
In order to build `Jar Jar Abrams` yourself, assuming you have `sbt` installed, you can run:
```sh
$ sbt assembly
```

This will output an executable to `target/scala-2.12/jarjar-abrams-cli-<version>`. Note that this executable requires Java 8 to run.

## License

Licensed under the Apache License, Version 2.0.

## Credits

- [Eugene Yokota](https://github.com/eed3si9n) for creating and maintaining [jarjar-abrams](https://github.com/eed3si9n/jarjar-abrams)

Credits as listed in jarjar-abrams' README:

- [Jar Jar Links][links] was created by herbyderby (Chris Nokleberg) in 2004.
- Pants build team has been maintaining a fork [pantsbuild/jarjar][pj] since 2015.
- In 2015, Wu Xiang added shading support in [sbt-assembly#162](https://github.com/sbt/sbt-assembly/pull/162).
- In 2020, Jeroen ter Voorde added Scala signature processor in [sbt-assembly#393](https://github.com/sbt/sbt-assembly/pull/393).

  [links]: https://code.google.com/archive/p/jarjar/
  [pj]: https://github.com/pantsbuild/jarjar
