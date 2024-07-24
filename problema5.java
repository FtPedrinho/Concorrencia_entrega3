import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

//    Foram importados o semáforo, o lock e uma função Random para gerar tempos aleatórios
// para a chegada do ônibus.


public class Main {

    //   A classe do ônibus possui dois semáforos, um lock, um Random e duas variáveis
    // variáveis inteiras que serão usadas para controlar o acesso ao ônibus. O primeiro
    // semáforo é uma catraca que determinará quem poderá entrar na parada. Ela é utilizada
    // para proibir que as pessoas que chegaram no momento que o ônibus está parado subam
    // nele (conforme pedido na questão).

    //   O semáforo dos assentos corresponde ao semáforo do ônibus e seus assentos (0).
    // Os passageiros na parada ocupam esse semáforo para sinalizar que subiram no ônibus.
    // A liberação dos assentos e a determinação inicial dos 50 assentos ocorre na função
    // run() do motorista que será explicada adiante.

    //   O lock é utilizado para que cada thread manipule as variáveis inteiras de forma
    // isolada. Enquanto que o Random será utilizado na função do motorista para determinar
    // o tempo que cada ônibus demora para chegar à parada.

    //   As variáveis inteiras são utilizadas para determinar quando temos 50 pessoas
    // (determinando que o excesso irá no próximo ônibus) e para determinar quantas pessoas
    // de fato subiram no ônibus para fazer a futura ocupação de assentos (ônibus vai embora).

    public static class Onibus {
        private static final Semaphore assentos = new Semaphore(0);
        private static final Semaphore catraca = new Semaphore(1);
        private final Lock lock = new ReentrantLock();
        private Random gerador = new Random();
        private int pessoas_esperando = 0;
        private int pessoas_onibus = 0;

        //   essa função administra o lado dos passageiros. Cada um chega a parada e passa
        // pela catraca. Caso um ônibus esteja parado, a catraca é fechada, impedindo que
        // as pessoas recem chegadas entrem no ônibus. Quando não existe ônibus parado, as
        // pessoas passam na catraca e esperam (uma mensagem é printada caso o numero exceda 50
        // pessoas). Agora, as 50 primeiras Threads poderão ocupar o semáforo de assentos, caso
        // possível, subindo no ônibus e alterando variáveis.

        public void Parada(String nome, int numero) throws InterruptedException {
            System.out.println(nome + numero + ": chegou à parada de ônibus.");
            catraca.acquire();
            catraca.release();

            // Manipulação de variaveis.
            lock.lock();
            try{
                pessoas_esperando ++;
                if(pessoas_esperando > 50){
                    System.out.println(nome + numero + " terá que esperar o próximo ônibus.");
                }
            }finally{
                lock.unlock();
            }

            assentos.acquire();

            // Manipulação de variaveis.
            lock.lock();
            try {
                System.out.println(nome + numero + ": subiu no ônibus...");

                // Contador de quantas subiram no ônibus.
                pessoas_onibus++;
                pessoas_esperando--;
            }finally{
                lock.unlock();
            }
        }

        //   Está função é a do motorista, uma Thread será responsável por guiá-la: o motorista.
        // Nela temos um laço que representa o circuito de ônibus que chegam na parada. O tempo
        // é determinado de forma aleatoria em segundos e, quando o ônibus chega, a catraca é
        // trancada. O ônibus libera 50 assentos e fecha sem aumentar a quantidade de permissões
        // no semáforo. Uma vez que ele parte, a catraca é reaberta.

        public void Coordenar() throws InterruptedException{

            for (int i = 0; i < 6; i++){

                // Tempo de espera do ônibus chegar.
                int tempo = gerador.nextInt(3);
                Thread.sleep((tempo + 1) * 1000);

                // Catraca trancada e chegada do ônibus.
                catraca.acquire();
                System.out.println("... O onibus chegou...");

                // Liberação de todos os assentos (50) e simulação de tempo de embarque.
                assentos.release(50);
                Thread.sleep(500);

                // O ônibus fecha, mas sem aumentar a quantidade de pessoas que cabem no ônibus.
                assentos.acquire(50-pessoas_onibus);
                pessoas_onibus = 0;
                System.out.println("... O ônibus irá partir...");

                // A catraca é liberada.
                catraca.release();
            }
        }
    }

    //   Classe dos passageiro, com suas características (nome, número e ônibus)
    // ela é reaproveitada para fazer a Thread do motorista e coordenar a chegada
    // e partida dos ônibus, levando as diferentes Threads para funções diferentes.

    public static class Passageiro implements Runnable {
        private final Onibus onibus;
        private final String nome;
        private final int numero;

        public Passageiro(Onibus onibus, String nome, int numero) {
            this.onibus = onibus;
            this.nome = nome;
            this.numero = numero;
        }

        // Passageiro para função da parada e motorista para função de coordenar ônibus.
        public void run() {
            if (nome == "Passageiro"){
                try {
                    onibus.Parada(nome, numero);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try{
                    onibus.Coordenar();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //   A main envolve a criação da parada de ônibus, a criação do motorista
    // (levando-o a sua função) e a criação dos passageiros.

    public static void main(String[] args) {
        Onibus onibus = new Onibus();

        Passageiro motorista = new Passageiro(onibus, "Motorista",  0);
        Thread motoristaThread = new Thread(motorista);
        motoristaThread.start();

        for (int i = 0; i < 200; i++) {
            Passageiro passageiro = new Passageiro(onibus, "Passageiro", i + 1);
            Thread passageiroThread = new Thread(passageiro);
            passageiroThread.start();
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
