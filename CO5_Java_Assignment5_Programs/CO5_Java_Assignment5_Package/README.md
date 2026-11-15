# CO5 Java Assignment 5
Name: ____________________  
Register Number: ____________________  
Java version: ____________________  
GitHub repository: ____________________

## Folder guide
- Task01: Text-file bus-pass store and buffered/unbuffered line counter.
- Task02: Generic DAO interface and StudentDao implementation.
- Task03: Serialization example; password and live connection-like field are transient.
- Task04: JDBC transaction transfer and unsafe/safe login examples.
- Task05: Batch loader and migration/metadata program.
- database/schema.sql: MySQL schema.

## Compile and run
Open a terminal in the relevant folder. Compile with `javac FileName.java`, then run with `java ClassName`.
Task01: `javac PassStore.java && java PassStore`; `javac BufferedLineCounter.java && java BufferedLineCounter attendance.txt`.
Task03: `javac SerializationDemo.java && java SerializationDemo`.
Task02, Task04 and Task05 require MySQL Connector/J and a local MySQL database. Add the connector JAR to the classpath and configure the JDBC URL, username and password. Do not commit real passwords.

## Important evidence to add
Record your own Java version, screenshots, actual timings for 1 MB/10 MB/50 MB and 1k/10k/100k records, JDBC timing table, and GitHub link. Do not invent measurements: run on your computer and paste the observed values into the report.
