

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Main {

    public static class Banheiro{
        private static final Semaphore catraca = new Semaphore(1);
        private static final Semaphore banheiro = new Semaphore(3);
        private static final Lock lock = new ReentrantLock();
        private static boolean boleana = true; // 0 = mulher; 1 = homem.
        private static int pessoas = 0;


        public void usarBanheiro(String nome, int numero, int tempo, int contador) throws InterruptedException {
            System.out.println(nome + numero + " chegou à fila do banheiro.");

            lock.lock();
            try {
                if (contador == 1) {
                    if (nome == "Homem") {
                        boleana = true;
                    } else {
                        boleana = false;
                    }
                }
            }finally{
                lock.unlock();
            }

            if ((!boleana && nome == "Homem") || (boleana && nome == "Mulher")){
                catraca.acquire();
            }

            catraca.acquire();
            catraca.release();

            lock.lock();
            try{
                pessoas++;
            }finally{
                lock.unlock();
            }

            System.out.println(nome + numero + ": entrou no banheiro...");
            banheiro.acquire();
            Thread.sleep(tempo);
            banheiro.release();
            System.out.println(nome + numero + ": saiu do banheiro...");

            lock.lock();
            try{
                pessoas--;
                if (pessoas == 0 && ((boleana && nome == "Homem") || (!boleana && nome == "Mulher"))){
                    System.out.println("... Trocando a vez de usar o banheiro...");
                    if (boleana == true){
                        boleana = false;
                    }else{
                        boleana = true;
                    }
                    catraca.release();
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
        private final int contador;

        public Pessoa(Banheiro banheiro, String nome, int numero, int tempo, int contador) {
            this.banheiro = banheiro;
            this.nome = nome;
            this.numero = numero;
            this.tempo = tempo;
            this.contador = contador;
        }
        public void run() {
            try {
                banheiro.usarBanheiro(nome, numero, tempo, contador);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Banheiro banheiro = new Banheiro();
        Random random = new Random();
        int contador_mulher = 0;
        int contador_homem = 0;

        for (int i = 0; i < 200; i++) {
            int genero = random.nextInt(2);
            int tempo = random.nextInt(4);

            if (genero == 0) {
                contador_mulher ++;
                Pessoa pessoa = new Pessoa(banheiro, "Mulher", contador_mulher, tempo * 1000, i + 1);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            } else{
                contador_homem ++;
                Pessoa pessoa = new Pessoa(banheiro, "Homem", contador_homem, tempo * 1000, i + 1);
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
