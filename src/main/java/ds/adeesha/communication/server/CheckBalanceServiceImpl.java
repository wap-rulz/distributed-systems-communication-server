package ds.adeesha.communication.server;

import ds.adeesha.communication.grpc.generated.CheckBalanceRequest;
import ds.adeesha.communication.grpc.generated.CheckBalanceResponse;
import ds.adeesha.communication.grpc.generated.CheckBalanceServiceGrpc;
import io.grpc.stub.StreamObserver;

public class CheckBalanceServiceImpl extends CheckBalanceServiceGrpc.CheckBalanceServiceImplBase {
    private BankServer server;

    public CheckBalanceServiceImpl(BankServer server) {
        this.server = server;
    }

    @Override
    public void checkBalance(CheckBalanceRequest request, StreamObserver<CheckBalanceResponse> responseObserver) {
        String accountId = request.getAccountId();
        System.out.println("Request received..");
        double balance = getAccountBalance(accountId);
        CheckBalanceResponse response = CheckBalanceResponse.newBuilder().setBalance(balance).build();
        System.out.printf("Responding, balance for account " + accountId + " is %.2f\n\n", balance);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private double getAccountBalance(String accountId) {
        System.out.println("Checking balance for Account " + accountId);
        return server.getAccountBalance(accountId);
    }
}
