# Typhon: It's a programming language.

[ ![Download](https://api.bintray.com/packages/iconmaster5326/maven/typhon/images/download.svg) ](https://bintray.com/iconmaster5326/maven/typhon/_latestVersion)

## Overview

Typhon is a statically-typed, virtual-machine based, cross-platform programming language, designed to improve upon the concepts built into languages such as Java or C#. Typhon is even written in Java, allowing you to easily use the compiler itself as a library in many other projects.

## Features

* Full type inference- Use the `var` keyword instead of specifying a type!
* Object-oriented with full multiple inheritance!
* Templates for classes and functions, whose type data are retained run-time!
* Full operator overloading support!
* Many convienience features for the programmer!
* Compiler-as-a-library achitecture, inspired by [LLVM](http://llvm.org): Make use of the Typhon compiler easily in any Java program!
* Extendable with a plugin system: Add plugin JARs to your classpath, and they just install themselves!

## Examples

Check out the `examples` directory for example Typhon programs.

## Running

To run Typhon, first download the JAR from [Bintray](https://bintray.com/iconmaster5326/maven/typhon/_latestVersion), our Maven repository and download provider. Then invoke it like so:

```
java -jar typhon.jar
```

This will print the basic Typhon usage, showing you what subcommands and options you can specify. The most basic use of Typhon is to check programs for compilation errors; to do this, run the `check` subcommand as follows:

```
java -jar typhon.jar check your_file.tn
```

To run Typhon programs, check out [TnBox](https://github.com/TyphonLang/TnBox), the official Typhon VM. You can also check and run programs from Eclipse using [TnClipse](https://github.com/TyphonLang/TnClipse).

## Building

This project uses [Gradle](http://gradle.org) to build. To build this repository, all you have to do is check it out and run:

```
./gradlew build
```

This will compile Typhon, downloading all the needed dependencies from Maven.

There are other Gradle tasks to run as well, such as `jar`, `sourcesJar`, `compileJava`, `generateGrammarSource`, and `test`. Run `./gradlew help` for more options.

## Contributing

Feel free to make any pull requests you desire, and check out our [issue tracker](https://github.com/TyphonLang/Typhon/issues) to report any bugs.