package org.tektutor;

import javax.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class JmsProducer {
    public static void main(String[] args) throws Exception {
        // Use internal service name for in-cluster communication
        String brokerUrl = "tcp://amq-broker-core-0-svc:61616";
        String user = System.getenv("AMQ_USER");
        String password = System.getenv("AMQ_PASSWORD");

        // Fallback to hardcoded values if env vars not set
        if (user == null) user = "gsFZ8w5b";
        if (password == null) password = "DtLY5U1h";

        // For local testing, use localhost
        // String brokerUrl = "tcp://localhost:61616";

        try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl, user, password);
             JMSContext context = cf.createContext()) {

            Queue queue = context.createQueue("order.queue");
            context.createProducer().send(queue, "Order #1234 created");
            System.out.println("Message sent successfully!");
        }
    }
}
