
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
        private int contador_espera = 0;
        private boolean booleana = false; // 1 para homens e 0 para mulheres.

        public void Mulheres(String nome, int numero, int tempo, int contador) throws InterruptedException {
            System.out.println(nome + numero + " chegou à fila do banheiro.");

            //
            lock.lock();
            try{
                mulheres++;
            }finally{
                lock.unlock();
            }

            //
            if (contador == 1){
                catraca_homens.acquire();
                booleana = false;
            }else{
                Thread.sleep(50);
            }

            //
            if (!booleana){
                lock.lock();
                try{
                    contador_mulheres++;
                    if (homens >= 1) {
                        if (contador_espera == 2) {
                            catraca_mulheres.acquire();
                            contador_espera = -1;
                            booleana = true;
                        }
                        contador_espera ++;
                    }
                }finally{
                    lock.unlock();
                }
            }

            //
            lock.lock();
            try{
                mulheres++;
                if (booleana && homens == 0){
                    booleana = false;
                }
            }finally{
                lock.unlock();
            }

            //
            catraca_mulheres.acquire();
            catraca_mulheres.release();

            //
            banheiro_mulheres.acquire();
            System.out.println(nome + numero + ": entrou no banheiro...");
            Thread.sleep(tempo);
            System.out.println(nome + numero + ": saiu do banheiro...");
            banheiro_mulheres.release();

            //
            lock.lock();
            try{
                mulheres--;
                contador_mulheres--;
                if (booleana || mulheres == 0){
                    if (mulheres == 0){
                        contador_espera = 0;
                    }
                    System.out.println("... É a vez dos HOMENS usarem o banheiro...");
                    catraca_homens.release();
                }
            }finally{
                lock.unlock();
            }
        }

        public void Homens(String nome, int numero, int tempo, int contador) throws InterruptedException {
            System.out.println(nome + numero + " chegou à fila do banheiro.");

            //
            lock.lock();
            try{
                homens++;
            }finally{
                lock.unlock();
            }

            //
            if (contador == 1){
                catraca_mulheres.acquire();
                booleana = true;
            }else{
                Thread.sleep(50);
            }

            //
            if (booleana){
                lock.lock();
                try{
                    contador_homens++;
                    if (mulheres >= 1) {
                        if (contador_espera == 2) {
                            catraca_homens.acquire();
                            contador_espera = -1;
                            booleana = false;
                        }
                        contador_espera ++;
                    }
                }finally{
                    lock.unlock();
                }
            }

            //
            lock.lock();
            try{
                homens++;
                if (!booleana && mulheres == 0){
                    booleana = true;
                }
            }finally{
                lock.unlock();
            }

            //
            catraca_homens.acquire();
            catraca_homens.release();

            //
            banheiro_homens.acquire();
            System.out.println(nome + numero + ": entrou no banheiro...");
            Thread.sleep(tempo);
            System.out.println(nome + numero + ": saiu do banheiro...");
            banheiro_homens.release();

            //
            lock.lock();
            try{
                homens--;
                contador_homens--;
                if (!booleana || homens == 0) {
                    if (homens == 0){
                        contador_espera = 0;
                    }
                    System.out.println("... É a vez das MULHERES usarem o banheiro...");
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
        private final int contador;

        public Pessoa(Banheiro banheiro, String nome, int numero, int tempo, int contador) {
            this.banheiro = banheiro;
            this.nome = nome;
            this.numero = numero;
            this.tempo = tempo;
            this.contador = contador;
        }
        public void run() {
            if (nome == "Homem"){
                try {
                    banheiro.Homens(nome, numero, tempo, contador);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try{
                    banheiro.Mulheres(nome, numero, tempo, contador);
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
                Pessoa pessoa = new Pessoa(banheiro, "Mulher", contador_mulher, num2* 1000, i + 1);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            } else{
                contador_homem ++;
                Pessoa pessoa = new Pessoa(banheiro, "Homem", contador_homem, num2* 1000, i + 1);
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
