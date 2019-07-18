package sila_java.servers.hello_sila;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
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
import sila_java.library.server_base.utils.ArgumentHelper;
import sila_java.servers.automated_freezer.HelloSiLAServer;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static sila_java.library.core.sila.types.SiLAErrors.retrieveSiLAError;

/**
 * Simple Integration Test for the HelloSiLA Server
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HelloSiLAServerTest {
    private HelloSiLAServer server;
    private ManagedChannel channel;
    private GreetingProviderGrpc.GreetingProviderBlockingStub blockingStub;

    @BeforeAll
    void HelloSiLAServerTest() {
        log.info("Starting HelloSiLAServer...");
        final String[] args = {"-n", "local"};
        this.server = new HelloSiLAServer(new ArgumentHelper(args, HelloSiLAServer.SERVER_TYPE));

        final Server server = ServerFinder
                .filterBy(ServerFinder.Filter.type(HelloSiLAServer.SERVER_TYPE))
                .scanAndFindOne(Duration.ofMinutes(1))
                .orElseThrow(RuntimeException::new);

        this.channel = ChannelFactory.withEncryption(server.getHost(), server.getPort());
        this.blockingStub = GreetingProviderGrpc.newBlockingStub(this.channel);
    }

    @AfterAll
    void cleanup() throws InterruptedException {
        if (server != null) {
            server.close();
            server = null;
        }
        ServerManager.getInstance().close();
        this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void testSiLAService() {
        final SiLAServiceGrpc.SiLAServiceBlockingStub serviceStub = SiLAServiceGrpc
                .newBlockingStub(this.channel);

        final List<SiLAServiceOuterClass.DataType_FeatureIdentifier> featureIdentifierList = serviceStub
                .getImplementedFeatures(SiLAServiceOuterClass.Get_ImplementedFeatures_Parameters
                        .newBuilder()
                        .build())
                .getImplementedFeaturesList();

        assertEquals(1, featureIdentifierList.size());

        Assertions.assertTrue(
                featureIdentifierList
                        .stream()
                        .anyMatch(featureIdentifier ->
                                "org.silastandard/examples/v1/GreetingProvider".equals(
                                        featureIdentifier.getFeatureIdentifier().getValue()
                                )
                        )
        );
    }

    @Test
    void testHello() {
        final GreetingProviderOuterClass.SayHello_Parameters.Builder parameter =
                GreetingProviderOuterClass.SayHello_Parameters.newBuilder();

        final String testName = "SiLA";
        final GreetingProviderOuterClass.SayHello_Responses result = blockingStub.sayHello(
                parameter.setName(SiLAString.from(testName)).build()
        );

        assertEquals("Hello " + testName, result.getGreeting().getValue());
    }

    @Test
    void testEmptyParameter() {
        final GreetingProviderOuterClass.SayHello_Parameters.Builder parameter =
                GreetingProviderOuterClass.SayHello_Parameters.newBuilder();

        // Has to throw a validation error
        try {
            blockingStub.sayHello(parameter.build());
        } catch (StatusRuntimeException e) {
            final SiLAFramework.SiLAError siLAError = retrieveSiLAError(e);
            assertNotNull(siLAError);
            assertTrue(siLAError.hasValidationError());
            return;
        }
        fail("HelloSiLAServer did not throw a validation error with empty parameter");
    }
}
