package nsu.fit;

public class Main {

    public void processTransaction(int txNum) throws InterruptedException {
        System.err.println("Processing tx: " + txNum);
        int sleep = (int) (Math.random() * 1000);
        Thread.sleep(sleep);
        System.err.printf("tx: %d completed%n", txNum);
    }

    public static void main(String[] args) throws Exception {
        Main tp = new Main();
        for (int i = 0; i < 10; ++i) {
            tp.processTransaction(i);
        }
    }

}
