# Brinvex-Util-IBKR

## Introduction

_Brinvex-Util-IBKR_ is a compact Java library which enables developers 
to easily work with InteractiveBrokers reports.

## Maven dependency declaration
To use _Brinvex-Util-IBKR_ in your Maven project, declare the following dependency in your project's pom file. 
No transitive dependencies are required during compilation or at runtime.
````

<repository>
    <id>brinvex-mvnrepo</id>
    <url>https://github.com/brinvex/brinvex-mvnrepo/raw/main/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>

<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-ibkr-api</artifactId>
    <version>1.4.0</version>
</dependency>
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-ibkr-impl</artifactId>
    <version>1.4.0</version>
    <scope>runtime</scope>
</dependency>
````

### Requirements
- Java 17 or above

### License

- The _Brinvex-Util-IBKR_ is released under version 2.0 of the Apache License.
