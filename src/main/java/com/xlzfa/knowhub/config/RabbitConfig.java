package com.xlzfa.knowhub.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String LIKE_EXCHANGE = "like.exchange";


    public static final String ANSWER_LIKE_QUEUE = "answer.like.queue";
    public static final String ANSWER_ROUTING_KEY = "answer.like";


    public static final String QUESTION_LIKE_QUEUE = "question.like.queue";
    public static final String QUESTION_ROUTING_KEY = "question.like";


    @Bean
    public Queue answerLikeQueue() {
        return new Queue(ANSWER_LIKE_QUEUE,true);
    }

    @Bean
    public Queue questionLikeQueue() {
        return new Queue(QUESTION_LIKE_QUEUE,true);
    }

    @Bean
    public DirectExchange likeExchange() {
        return new DirectExchange(LIKE_EXCHANGE);
    }

    @Bean
    public Binding answerLikeBinding(Queue answerLikeQueue, DirectExchange likeExchange) {
        return BindingBuilder.bind(answerLikeQueue)
                .to(likeExchange)
                .with(ANSWER_ROUTING_KEY);
    }

    @Bean
    public Binding questionLikeBinding(Queue questionLikeQueue, DirectExchange likeExchange) {
        return BindingBuilder.bind(questionLikeQueue)
                .to(likeExchange)
                .with(QUESTION_ROUTING_KEY);
    }



    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }


    //死信

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("answer.dlx.exchange");
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue("answer.like.dlq");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlx.routing");
    }

}
