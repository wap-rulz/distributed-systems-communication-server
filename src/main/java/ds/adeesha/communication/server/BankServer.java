package ds.adeesha.communication.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BankServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        int serverPort = 11436;
        Server server = ServerBuilder.forPort(serverPort).addService(new BalanceServiceImpl()).build();
        server.start();
        System.out.println("BankServer Started and ready to accept requests on port: " + serverPort);
        server.awaitTermination();
    }
}
