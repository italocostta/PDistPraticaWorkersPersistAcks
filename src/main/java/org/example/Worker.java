package org.example;

import com.rabbitmq.client.*;

public class Worker {

    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        // Configuração da conexão com o RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");


        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        // Declaração da fila com durabilidade ativada
        boolean durable = true;
        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] Aguardando mensagens. Para sair pressione CTRL+C");

        // Configuração de fair dispatch (envio justo)
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

        // Callback para processamento das mensagens
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Recebido: '" + message + "'");
            try {
                try {
                    doWork(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(" [x] Feito");
            } finally {
                // Confirmação manual da mensagem
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        // Iniciando o consumo da fila com acks manuais
        boolean autoAck = false; // Desabilitando auto-acknowledgement
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
    }

    // Método que simula o processamento da mensagem
    private static void doWork(String task) throws InterruptedException {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                Thread.sleep(1000); // Pausa de 1 segundo para cada ponto
            }
        }
    }
}
