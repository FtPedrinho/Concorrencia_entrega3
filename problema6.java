import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Main {
    
    //    A classe do banheiro foi criada com um semáforo, um lock e uma string de identificação para determinar o
    // genero da pessoa no banheiro. As threads que entram no banheiro com o mesmo genero da variavel podem entrar
    // no banheiro e as threads que entram sendo do genero oposto travam a entrada e alteram a variavel ate que todos 
    // saiam do banheiro.

    public static class Banheiro{
        private static final Semaphore banheiro = new Semaphore(3);
        private static final Lock lock = new ReentrantLock();
        private static String genero = "nada";

        public void usarBanheiro(String nome, int numero, int tempo) throws InterruptedException {
            System.out.println(nome + numero + " chegou à fila do banheiro.");

            // determinação do genero e possivel travamento da fila até a troca
            lock.lock();
            try {
                if (genero == "nada") {
                    genero = nome;
                }

                if (nome != genero){
                    banheiro.acquire(3);
                    System.out.println("... trocando a vez...");
                    genero = nome;
                    banheiro.release(3);
                }

            }finally{
                lock.unlock();
            }

            // entrada e saída do banheiro
            banheiro.acquire();
            System.out.println(nome + numero + ": entrou no banheiro");
            Thread.sleep(tempo);
            System.out.println(nome + numero + ": saiu do banheiro");
            banheiro.release();

        }
    }

    //   A classe das pessoas vem com 4 variaveis: banheiro, nome ("homem" ou "mulher"), numero de contagem e numero
    // para tempo do banheiro. As threads são levadas para a função do banheiro.
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
            try {
                banheiro.usarBanheiro(nome, numero, tempo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //    Na main, utilizamos uma variável random para gerar pessoas de genero e um tempo de utilização
    // do banheiro aleatórios. Existem contadores individuais para homens e mulheres que serão levados ao código.
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
                Pessoa pessoa = new Pessoa(banheiro, "Mulher", contador_mulher, tempo * 1000);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            } else{
                contador_homem ++;
                Pessoa pessoa = new Pessoa(banheiro, "Homem", contador_homem, tempo * 1000);
                Thread pessoaThread = new Thread(pessoa);
                pessoaThread.start();
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
