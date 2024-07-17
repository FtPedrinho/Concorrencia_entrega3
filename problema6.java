import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Main {

    public static class Banheiro {
        private static final Semaphore catraca_homem = new Semaphore(1);
        private static final Semaphore catraca_mulher = new Semaphore(1);
        private static final Semaphore banheiro_Homens = new Semaphore(3);
        private static final Semaphore banheiro_mulheres= new Semaphore(3);
        private final Lock lock = new ReentrantLock();
        private Random gerador = new Random();
        private int mulheres = 0;
        private int contador_mulheres = 0;
        private int homens = 0;
        private int contador_homens = 0;
        private boolean booleana = false; // 1 para homens e 0 para mulheres.

        public void Mulheres(String nome, int numero, int tempo) throws InterruptedException {
            System.out.println(nome + numero + " chegou à fila do banheiro.");

            lock.lock();
            try{
                mulheres++;
                if (!booleana && mulheres == 1 && homens <= 3){
                    catraca_homem.acquire();
                }
            }finally{
                lock.unlock();
            }

            if (!booleana && homens >= 4){
                booleana = true;
                catraca_mulher.acquire();
            }
            catraca_mulher.acquire();
            catraca_mulher.release();

            System.out.println(nome + numero + ": entrou no banheiro...");
        }

        public void Homens(String nome, int numero, int tempo) throws InterruptedException {
            // xx

        }
    }

    public static class Pessoa implements Runnable {
        private final Banheiro banheiro;
        private final String nome;
        private final int numero;
        private final int tempo;

        public Pessoa(Banheiro banheiro, String nome, int numero, int tempo) {
            this.banheiro = banheiro;
            this.nome = nome;
            this.numero = numero;
            this.tempo = tempo;
        }
        public void run() {
            if (nome == "Homem"){
                try {
                    banheiro.Homens(nome, numero, tempo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try{
                    banheiro.Mulheres(nome, numero, tempo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro();
        Random random = new Random(2);
        Random tempo = new Random(5);

        for (int i = 0; i < 200; i++) {
            int num = random.nextInt();
            int num2 = random.nextInt();

            if (num == 0) {
                Pessoa pessoa = new Pessoa(banheiro, "Mulher", i + 1, num2*1000);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            } else{
                Pessoa pessoa = new Pessoa(banheiro, "Homem", i + 1, num2*1000);
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
