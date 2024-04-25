package ds.adeesha.communication.server;

import ds.adeesha.synchronization.DistributedLock;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BankServer {
    public int serverPort;
    private DistributedLock leaderLock;
    private AtomicBoolean isLeader = new AtomicBoolean(false);
    public byte[] leaderData;

    private Map<String, Double> accounts = new HashMap();

    public BankServer(String host, int port) throws IOException, InterruptedException, KeeperException {
        this.serverPort = port;
        leaderLock = new DistributedLock("BankServerTestCluster", buildServerData(host, port));
    }

    public void setAccountBalance(String accountId, double value) {
        accounts.put(accountId, value);
    }

    public double getAccountBalance(String accountId) {
        Double value = accounts.get(accountId);
        return (value != null) ? value : 0.0;
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        int serverPort;
        DistributedLock.setZooKeeperURL("localhost:2181");
        if (args.length != 1) {
            System.out.println("Usage BankServer <port>");
            System.exit(1);
        }
        serverPort = Integer.parseInt(args[0].trim());
        BankServer server = new BankServer("localhost", serverPort);
        server.startServer();
    }

    public void startServer() throws IOException, InterruptedException, KeeperException {
        Server server = ServerBuilder
                .forPort(serverPort)
                .addService(new CheckBalanceServiceImpl(this))
                .addService(new SetBalanceServiceImpl(this))
                .build();
        server.start();
        System.out.println("BankServer Started and ready to accept requests on port " + serverPort);
        tryToBeLeader();
        server.awaitTermination();
    }

    public static String buildServerData(String IP, int port) {
        StringBuilder builder = new StringBuilder();
        builder.append(IP).append(":").append(port);
        return builder.toString();
    }

    public boolean isLeader() {
        return isLeader.get();
    }

    private synchronized void setCurrentLeaderData(byte[] leaderData) {
        this.leaderData = leaderData;
    }

    private void tryToBeLeader() throws KeeperException, InterruptedException {
        Thread leaderCampaignThread = new Thread(new LeaderCampaignThread());
        leaderCampaignThread.start();
    }

    public class LeaderCampaignThread implements Runnable {
        private byte[] currentLeaderData = null;

        @Override
        public void run() {
            System.out.println("Starting the leaderCampaign");
            try {
                boolean leader = leaderLock.tryAcquireLock();
                while (!leader) {
                    byte[] leaderData = leaderLock.getLockHolderData();
                    if (currentLeaderData != leaderData) {
                        currentLeaderData = leaderData;
                        setCurrentLeaderData(currentLeaderData);
                    }
                    Thread.sleep(10000);
                    leader = leaderLock.tryAcquireLock();
                }
                System.out.println("I got the leader lock.Now acting as primary");
                isLeader.set(true);
                currentLeaderData = null;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public synchronized String[] getCurrentLeaderData() {
        return new String(leaderData).split(":");
    }

    public List<String[]> getOthersData() throws KeeperException, InterruptedException {
        List<String[]> result = new ArrayList<>();
        List<byte[]> othersData = leaderLock.getOthersData();
        for (byte[] data : othersData) {
            String[] dataStrings = new String(data).split(":");
            result.add(dataStrings);
        }
        return result;
    }
}
