
# XML and JSON parsing test for OpenJDK and GraalVM native compiled app

## Building

For Linux

1. Download and install OpenJDK 25 and GraalVM 25
2. Set environment variable `GRAALVM_HOME` to GraalVM home directory
3. Build project with Maven: `mvn clean package`
4. Copy dependencies JARs to `./target/` folder: `mvn dependency:copy-dependencies -DoutputDirectory=./target/`
5. Generate test JSON and XML files with 1 million records each:
`java -jar ./target/nativetest-0.0.1-SNAPSHOT.jar genjson`
`java -jar ./target/nativetest-0.0.1-SNAPSHOT.jar genxml`
6. Build GraalVM native image: `mvn -Pnative package`


## Running test

In case of running on a laptop don't forget to turn power mode to maximum performance mode to prevent CPU performance fluctuations.

Choose one of the mods:

- `readxml` - for reading and parsing XML in a single thread
- `readxmlparallel` - for reading XML in a single thread, and parsing with virtual threads
- `readjson` - for reading and parsing JSON in a single thread
- `readjsonparallel` - for reading JSON in a single thread, and parsing with virtual threads

Each mode starts with a prompt, and actual parsing starts sfter pressing Enter. This is done to compare Java performance after several consecutive runs within a single process. Time is measured after pressing Enter to the cycle finish.

For 'readjson', for example:

1. For Java: `java -jar ./target/nativetest-0.0.1-SNAPSHOT.jar readjson`
2. For GraalVM native image: `./target/nativetest readjson`


## Results on my laptop (AMD Ryzen 7, 8 logical cores) for 10 runs, min - max, in seconds:

| Test             | OpenJDK      | GraalVM native |
| ---------------- | ------------ | -------------- |
| readxml          | 52,8 - 54,2  | 71,4 - 72,8    |
| readxmlparallel  | 14,0 - 25,3  | 28,4 - 31,7    |
| readjson         | 23,5 - 24,7  | 32,6 - 33,8    |
| readjsonparallel | 10,9 - 21,9  | 20,5 - 26,9    |



## Generating GraalVM reachability-metadata

1. Build the application: `mvn clean package`
2. Copy dependencies JARs to `./target/` folder: `mvn dependency:copy-dependencies -DoutputDirectory=./target/`
3. Delete project file `./src/main/resources/META-INF/native-image/org.ng/nativetest/reachability-metadata.json`
4. Build metadata with training run:
`$GRAALVM_HOME/bin/java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image/org.ng/nativetest/ -jar ./target/nativetest-0.0.1-SNAPSHOT.jar graaltrainingrun`

