# Brinvex-Util-IBKR

## Introduction

_Brinvex-Util-IBKR_ is a compact Java library which enables developers 
to easily work with InteractiveBrokers reports.

## Maven dependency declaration
To use _Brinvex-Util-IBKR_ in your Maven project, declare the following dependency in your project's pom file. 
No transitive dependencies are required during compilation or at runtime.
````

<repository>
    <id>repository.brinvex</id>
    <name>Brinvex Repository</name>
    <url>https://github.com/brinvex/brinvex-repo/raw/main/</url>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>

<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-ibkr-api</artifactId>
    <version>1.8.1</version>
</dependency>
<dependency>
    <groupId>com.brinvex.util</groupId>
    <artifactId>brinvex-util-ibkr-impl</artifactId>
    <version>1.8.1</version>
    <scope>runtime</scope>
</dependency>
````

### Requirements
- Java 17 or above

### License

- The _Brinvex-Util-IBKR_ is released under version 2.0 of the Apache License.

## Some of our experiences with the Interactive Brokers 

#####  Symbol Discrepancy in Stock Position
- 2024-02-17
- _I am writing to bring to your attention a discrepancy in the display symbols for my stock position in the German company Siemens (ISIN: DE0007236101), which was purchased on IBIS.
Currently, in TWS and on the Portfolio screen of the IB web application, the symbol for this position is displayed as "SIE." However, in the report statements, including flex statements, the symbol is listed as "SIEd." This inconsistency is causing some confusion and inconvenience for me.
I would greatly appreciate your assistance in standardizing the symbol representation for this Siemens stock position. Ideally, I would like to see "SIE" consistently across all platforms and reports.
Could you please provide guidance on how I can achieve this uniformity? Your prompt attention to this matter is highly appreciated._

  - Answer from IBKR Support Center:
  
     _Please be advised that it might happen that symbol differs between what you can see in TWS or Portfolio and Statements - it is caused by the symbol used on a primary exchange, which is always displayed in the statements. However, if the ISIN and other details are the same it means that it is the same product. It is designed by default._


