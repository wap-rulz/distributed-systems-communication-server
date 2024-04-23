package ds.adeesha.communication.server;

import ds.adeesha.naming.NameServiceClient;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BankServer {
    public static final String NAME_SERVICE_ADDRESS = "http://localhost:2379";

    public static void main(String[] args) throws IOException, InterruptedException {
        int serverPort = 11438;
        Server server = ServerBuilder.forPort(serverPort).addService(new BalanceServiceImpl()).build();
        server.start();
        NameServiceClient client = new NameServiceClient(NAME_SERVICE_ADDRESS);
        client.registerService("CheckBalanceService", "127.0.0.1", serverPort, "tcp");
        System.out.println("BankServer Started and ready to accept requests on port: " + serverPort);
        server.awaitTermination();
    }
}
