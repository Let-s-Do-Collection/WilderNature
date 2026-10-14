package net.satisfy.wildernature.client;

public final class ClientBountyBoardNotes {
    private static int openBounties = -1;
    private static int totalBounties = -1;

    public static void set(int open, int total) {
        openBounties = open;
        totalBounties = total;
    }

    public static int getVisibleContracts(int maxContracts) {
        if (totalBounties <= 0) {
            return maxContracts;
        }
        if (openBounties <= 0) {
            return 0;
        }
        return Math.max(1, Math.min(maxContracts, (int) Math.ceil(openBounties * (double) maxContracts / totalBounties)));
    }

    private ClientBountyBoardNotes() {
    }
}
