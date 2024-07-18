import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Main {

    public static class Banheiro{
        private static final Semaphore catraca_homens = new Semaphore(1);
        private static final Semaphore catraca_mulheres = new Semaphore(1);
        private static final Semaphore banheiro_homens = new Semaphore(3);
        private static final Semaphore banheiro_mulheres= new Semaphore(3);
        private final Lock lock = new ReentrantLock();
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
                if(contador_mulheres == 0 && !booleana){
                    catraca_homens.acquire();
                }

                if (!booleana && homens >= 1){
                    contador_mulheres ++;
                }
                if (contador_mulheres == 6){
                    catraca_mulheres.acquire();
                }
            } finally {
                lock.unlock();
            }

            catraca_mulheres.acquire();
            catraca_mulheres.release();

            banheiro_mulheres.acquire();
            System.out.println(nome + numero + ": entrou no banheiro...");
            Thread.sleep(tempo);
            banheiro_mulheres.release();
            System.out.println(nome + numero + ": saiu do banheiro...");

            lock.lock();
            try{
                mulheres--;
                if (contador_mulheres == 5){
                    System.out.println("... É vez dos homens entrarem no banheiro...");
                    contador_mulheres = 0;
                    booleana = true;
                    catraca_homens.release();
                }
            }finally{
                lock.unlock();
            }
        }

        public void Homens(String nome, int numero, int tempo) throws InterruptedException {
            System.out.println(nome + numero + " chegou à fila do banheiro.");

            lock.lock();
            try{
                homens++;
                if(contador_homens == 0 && booleana){
                    catraca_mulheres.acquire();
                }
                if (booleana && mulheres >= 1){
                    contador_homens ++;
                }
                if (contador_homens == 6){
                    catraca_homens.acquire();
                }
            } finally {
                lock.unlock();
            }

            catraca_homens.acquire();
            catraca_homens.release();

            banheiro_homens.acquire();
            System.out.println(nome + numero + ": entrou no banheiro...");
            Thread.sleep(tempo);
            banheiro_homens.release();
            System.out.println(nome + numero + ": saiu do banheiro...");

            lock.lock();
            try{
                homens--;
                if (contador_homens == 5){
                    System.out.println("... É vez das mulheres entrarem no banheiro...");
                    contador_homens = 0;
                    booleana = false;
                    catraca_mulheres.release();
                }
            }finally{
                lock.unlock();
            }
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
        Random random = new Random();
        int contador_mulher = 0;
        int contador_homem = 0;

        for (int i = 0; i < 200; i++) {
            int num = random.nextInt(2);
            int num2 = random.nextInt(4);

            if (num == 0) {
                contador_mulher ++;
                Pessoa pessoa = new Pessoa(banheiro, "Mulher", contador_mulher, num2* 1000);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            } else{
                contador_homem ++;
                Pessoa pessoa = new Pessoa(banheiro, "Homem", contador_homem, num2* 1000);
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
