# HelloSiLA JAVA Implementation
The goal is to show how a SiLA server can be implemented in JAVA. This is a demonstrational package 
and obviously not intended for any practical use.

Additionally, the features in `sila_base` should normally be referenced from the `sila_base` repository, 
for the ease of introduction, the feature was copied for this example.

To understand the build process and source code, check `pom.xml` for a minimum build configuration that uses
maven and `src/main/java` for the reference Java classes.

## Building and Running

To install the package, simply use maven:

    mvn install
    
Then the JARs can be deployed on machines running JDK 8, and both server and client
can be tested:

    cd target/
    java -jar hello_sila.jar
    
Display help & usage:

    java -jar hello_sila.jar -h

You can run the server and client on any host on your local network with discovery enabled:

    java -jar hello_sila.jar -n local
    java -cp hello_sila.jar -Dloader.main=sila_java.servers.hello_sila.HelloSiLAClient org.springframework.boot.loader.PropertiesLauncher

We are currently packaging the jar using the spring boot packager, this can be done differently in your own code.

## Running Multiple Hello SiLA Servers

If you want to run multiple instance on the same machine, the port will be assigned automatically. 
You will have to define a different configuration file though by providing the path with the `-c` flag,
as you want to have different UUID and SiLA Server Names for different instances.