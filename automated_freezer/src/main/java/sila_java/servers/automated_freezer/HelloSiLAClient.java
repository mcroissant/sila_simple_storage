package sila_java.servers.automated_freezer;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import sila2.org.silastandard.SiLAFramework;
import sila2.org.silastandard.core.silaservice.v1.SiLAServiceGrpc;
import sila2.org.silastandard.core.silaservice.v1.SiLAServiceOuterClass;
import sila2.org.silastandard.examples.greetingprovider.v1.GreetingProviderGrpc;
import sila2.org.silastandard.examples.greetingprovider.v1.GreetingProviderOuterClass;
import sila_java.library.core.communication.ChannelFactory;
import sila_java.library.core.sila.types.SiLAString;
import sila_java.library.manager.ServerFinder;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static sila_java.library.core.sila.types.SiLAErrors.generateSiLAErrorString;
import static sila_java.library.core.sila.types.SiLAErrors.retrieveSiLAError;

/**
 * Client to retrieve greeting from the {@link HelloSiLAServer}.
 *
 * This shows a simple client implementation, for a more generic client refer to the SiLAManager.
 */
@Slf4j
public class HelloSiLAClient {
    // Stub representing the GreetingProvider service calls
    private GreetingProviderGrpc.GreetingProviderBlockingStub blockingStub;

    private void buildStub(final ManagedChannel channel) {
        this.blockingStub = GreetingProviderGrpc.newBlockingStub(channel);
    }

    /**
     * Say hello to server.
     */
    private void greet(String name) {
        System.out.println("Will try to greet " + name + " ...");

        // Build the parameters and call the stub
        GreetingProviderOuterClass.SayHello_Parameters.Builder parameter = GreetingProviderOuterClass.SayHello_Parameters.newBuilder();
        GreetingProviderOuterClass.SayHello_Responses result;

        try {
            result = blockingStub.sayHello(parameter.setName(SiLAString.from(name)).build());
        } catch (StatusRuntimeException e) {
            // Automatic retrieval from SiLA conforming errors
            final SiLAFramework.SiLAError siLAError = retrieveSiLAError(e);

            if (siLAError == null) {
                throw new RuntimeException("Not A SiLA Error: " + e.getMessage());
            }

            System.out.println(generateSiLAErrorString(siLAError));
            return;
        }

        System.out.println("Greeting: " + result.getGreeting().getValue());
        System.out.println("Will try to get start year...");

        // Same as command, simply without parameters, notice how the builder still needs to be passed!
        GreetingProviderOuterClass.Get_StartYear_Responses startYear;
        try {
            startYear = blockingStub.getStartYear(
                    GreetingProviderOuterClass.Get_StartYear_Parameters.newBuilder().build()
            );
        } catch (StatusRuntimeException e) {
            final SiLAFramework.SiLAError siLAError = retrieveSiLAError(e);

            if (siLAError == null) {
                throw new RuntimeException("Not A SiLA Error: " + e.getMessage());
            }

            System.out.println(generateSiLAErrorString(siLAError));
            return;
        }
        System.out.println("Start Year: " + startYear.getStartYear());
    }

    /**
     * Simple Client that stops after using the GreetingProvider Feature
     */
    public static void main(String[] args) throws InterruptedException {
        try (final ServerManager serverManager = ServerManager.getInstance()) {
            HelloSiLAClient client = new HelloSiLAClient();

            // Create Manager for clients and start discovery
            final Server server = ServerFinder
                    .filterBy(ServerFinder.Filter.type(HelloSiLAServer.SERVER_TYPE))
                    .scanAndFindOne(Duration.ofMinutes(1))
                    .orElseThrow(() -> new RuntimeException("No HelloSiLA server found within time"));

            log.info("Found Server!");

            final ManagedChannel serviceChannel = ChannelFactory.withEncryption(server.getHost(), server.getPort());
            try {
                final SiLAServiceGrpc.SiLAServiceBlockingStub serviceStub = SiLAServiceGrpc.newBlockingStub(serviceChannel);

                System.out.println("Found Features:");
                final List<SiLAServiceOuterClass.DataType_FeatureIdentifier> featureIdentifierList = serviceStub
                        .getImplementedFeatures(SiLAServiceOuterClass.Get_ImplementedFeatures_Parameters.newBuilder().build())
                        .getImplementedFeaturesList();

                featureIdentifierList.forEach(featureIdentifier ->
                        System.out.println("\t" + featureIdentifier.getFeatureIdentifier())
                );

                // Use the discovered channel
                client.buildStub(serviceChannel);
                String user = "SiLA";
                client.greet(user);
            } finally {
                serviceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }
}