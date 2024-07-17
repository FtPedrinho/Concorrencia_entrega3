import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Main {

    public static class Banheiro {
        private static final Semaphore banheiro_Homens = new Semaphore(3);
        private static final Semaphore banheiro_mulheres= new Semaphore(3);
        private final Lock lock = new ReentrantLock();
        private Random gerador = new Random();
        private int mulheres = 0;
        private int homens = 0;
        private boolean vez = false; // 1 para homens e 0 para mulheres.

        public void Mulheres(String nome, int numero) throws InterruptedException {
        // xxx

        }

        public void Homens(String nome, int numero) throws InterruptedException {
        // xx

        }
    }

    public static class Pessoa implements Runnable {
        private final Banheiro banheiro;
        private final String nome;
        private final int numero;

        public Pessoa(Banheiro banheiro, String nome, int numero) {
            this.banheiro = banheiro;
            this.nome = nome;
            this.numero = numero;
        }
        public void run() {
            if (nome == "Homem"){
                try {
                    banheiro.Homens(nome, numero);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try{
                    banheiro.Mulheres(nome, numero);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro();
        Random random = new Random(2);

        for (int i = 0; i < 200; i++) {
            int num = random.nextInt();
            if (num == 0) {
                Pessoa pessoa = new Pessoa(banheiro, "Mulher", i + 1);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            } else{
                Pessoa pessoa = new Pessoa(banheiro, "Homem", i + 1);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
