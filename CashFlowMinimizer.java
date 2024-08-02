import java.util.*;

class Bank {
    String name;
    int netAmount;
    Set<String> types;

    Bank(String name) {
        this.name = name;
        this.netAmount = 0;
        this.types = new HashSet<>();
    }
}

public class CashFlowMinimizer {

    public static int getMinIndex(Bank[] listOfNetAmounts) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    public static int getSimpleMaxIndex(Bank[] listOfNetAmounts) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    public static Pair<Integer, String> getMaxIndex(Bank[] listOfNetAmounts, int minIndex, Bank[] input, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = "";

        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0 || listOfNetAmounts[i].netAmount < 0) continue;

            Set<String> intersection = new HashSet<>(listOfNetAmounts[minIndex].types);
            intersection.retainAll(listOfNetAmounts[i].types);

            if (!intersection.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();
            }
        }
        return new Pair<>(maxIndex, matchingType);
    }

    public static void printAns(Pair<Integer, String>[][] ansGraph, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows:\n");
        for (int i = 0; i < ansGraph.length; i++) {
            for (int j = 0; j < ansGraph.length; j++) {
                if (i == j) continue;

                if (ansGraph[i][j].first != 0 && ansGraph[j][i].first != 0) {
                    if (ansGraph[i][j].first.equals(ansGraph[j][i].first)) {
                        ansGraph[i][j] = new Pair<>(0, "");
                        ansGraph[j][i] = new Pair<>(0, "");
                    } else if (ansGraph[i][j].first > ansGraph[j][i].first) {
                        ansGraph[i][j] = new Pair<>(ansGraph[i][j].first - ansGraph[j][i].first, ansGraph[i][j].second);
                        ansGraph[j][i] = new Pair<>(0, "");
                        System.out.println(input[i].name + " pays Rs " + ansGraph[i][j].first + " to " + input[j].name + " via " + ansGraph[i][j].second);
                    } else {
                        ansGraph[j][i] = new Pair<>(ansGraph[j][i].first - ansGraph[i][j].first, ansGraph[j][i].second);
                        ansGraph[i][j] = new Pair<>(0, "");
                        System.out.println(input[j].name + " pays Rs " + ansGraph[j][i].first + " to " + input[i].name + " via " + ansGraph[j][i].second);
                    }
                } else if (ansGraph[i][j].first != 0) {
                    System.out.println(input[i].name + " pays Rs " + ansGraph[i][j].first + " to " + input[j].name + " via " + ansGraph[i][j].second);
                } else if (ansGraph[j][i].first != 0) {
                    System.out.println(input[j].name + " pays Rs " + ansGraph[j][i].first + " to " + input[i].name + " via " + ansGraph[j][i].second);
                }

                ansGraph[i][j] = new Pair<>(0, "");
                ansGraph[j][i] = new Pair<>(0, "");
            }
        }
    }

    public static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, int[][] graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];
        for (int i = 0; i < numBanks; i++) {
            listOfNetAmounts[i] = new Bank(input[i].name);
            listOfNetAmounts[i].types = input[i].types;

            int amount = 0;
            for (int j = 0; j < numBanks; j++) {
                amount += graph[j][i] - graph[i][j];
            }
            listOfNetAmounts[i].netAmount = amount;
        }

        @SuppressWarnings("unchecked")
        Pair<Integer, String>[][] ansGraph = new Pair[numBanks][numBanks];
        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                ansGraph[i][j] = new Pair<>(0, "");
            }
        }

        int numZeroNetAmounts = (int) Arrays.stream(listOfNetAmounts).filter(bank -> bank.netAmount == 0).count();

        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts);
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, minIndex, input, maxNumTypes);
            int maxIndex = maxAns.first;

            if (maxIndex == -1) {
                ansGraph[minIndex][0] = new Pair<>(Math.abs(listOfNetAmounts[minIndex].netAmount), input[minIndex].types.iterator().next());
                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts);
                ansGraph[0][simpleMaxIndex] = new Pair<>(Math.abs(listOfNetAmounts[minIndex].netAmount), input[simpleMaxIndex].types.iterator().next());

                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[simpleMaxIndex].netAmount == 0) numZeroNetAmounts++;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);
                ansGraph[minIndex][maxIndex] = new Pair<>(transactionAmount, maxAns.second);

                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[maxIndex].netAmount == 0) numZeroNetAmounts++;
            }
        }
        printAns(ansGraph, input);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n\t\t\t\t********************* Welcome to CASH FLOW MINIMIZER SYSTEM ***********************\n\n\n");
        System.out.println("This system minimizes the number of transactions among multiple banks in the different corners of the world that use different modes of payment. There is one world bank (with all payment modes) to act as an intermediary between banks that have no common mode of payment.\n\n");
        System.out.println("Enter the number of banks participating in the transactions.");
        int numBanks = sc.nextInt();
        sc.nextLine(); // consume newline

        Bank[] input = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>();

        System.out.println("Enter the details of the banks and transactions as stated:");
        System.out.println("Bank name, number of payment modes it has and the payment modes.");
        System.out.println("Bank name and payment modes should not contain spaces");

        int maxNumTypes = 0;
        /*for (int i = 0; i < numBanks; i++) {
            if (i == 0) {
                System.out.print("World Bank: ");
            } else {
                System.out.print("Bank " + i + ": ");
            }

            String name = sc.nextLine();
            input[i] = new Bank(name);
            indexOf.put(name, i);

            int numTypes = Integer.parseInt(sc.next().trim());
            sc.nextLine(); // consume newline

            for (int j = 0; j < numTypes; j++) {
                String type = sc.next().trim();
                input[i].types.add(type);
            }*/

            for (int i = 0; i < numBanks; i++) {
                if (i == 0) {
                    System.out.print("World Bank: ");
                } else {
                    System.out.print("Bank " + i + ": ");
                }
            
                String name = sc.nextLine();
                input[i] = new Bank(name);
                indexOf.put(name, i);
            
                System.out.print("Enter number of payment modes: ");
                int numTypes = Integer.parseInt(sc.nextLine().trim());
            
                for (int j = 0; j < numTypes; j++) {
                    System.out.print("Enter payment mode " + (j + 1) + ": ");
                    String type = sc.nextLine().trim();
                    input[i].types.add(type);
                }
            
            

            if (numTypes > maxNumTypes) maxNumTypes = numTypes;
        }

        /*System.out.println("Enter the number of transactions.");
        int numTransactions = sc.nextInt();
        sc.nextLine(); // consume newline

        int[][] graph = new int[numBanks][numBanks];

        System.out.println("Enter the transactions one by one as stated below:");
        for (int i = 0; i < numTransactions; i++) {
            System.out.println("Transaction " + (i + 1));
            System.out.println("Enter name of bank that pays, name of bank that gets money and the amount.");

            String debtor = sc.nextLine();
            String creditor = sc.nextLine();
            int amount = sc.nextInt();
            sc.nextLine(); // consume newline

            graph[indexOf.get(debtor)][indexOf.get(creditor)] = amount;
        }*/

        System.out.println("Enter the number of transactions.");
int numTransactions = sc.nextInt();
sc.nextLine(); // consume newline

int[][] graph = new int[numBanks][numBanks];

System.out.println("Enter the transactions one by one as stated below:");
for (int i = 0; i < numTransactions; i++) {
    System.out.println("Transaction " + (i + 1));
    System.out.println("Enter name of bank that pays, name of bank that gets money and the amount.");

    String debtor = sc.nextLine();
    String creditor = sc.nextLine();
    int amount = sc.nextInt();
    sc.nextLine(); // consume newline

    // Validate debtor and creditor names
    if (!indexOf.containsKey(debtor)) {
        System.out.println("Error: Debtor bank '" + debtor + "' does not exist.");
        continue; // Skip this transaction
    }
    if (!indexOf.containsKey(creditor)) {
        System.out.println("Error: Creditor bank '" + creditor + "' does not exist.");
        continue; // Skip this transaction
    }

    graph[indexOf.get(debtor)][indexOf.get(creditor)] = amount;
}


         minimizeCashFlow(numBanks, input, indexOf, numTransactions, graph, maxNumTypes);
         
    }
}

class Pair<F, S> {
    F first;
    S second;

    Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
}
