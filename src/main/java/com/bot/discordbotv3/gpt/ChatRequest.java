package com.bot.discordbotv3.gpt;

import java.util.ArrayList;
import java.util.List;

public class ChatRequest {
    private String model;
    private List<Message> messages;

    public ChatRequest(String model, String userMessage) {
        this.model = model;
        this.messages = new ArrayList<>();
        Message m1 = new Message(Message.Role.system, "You are a chatbot that acts like an alien from another planet named HowardDaAlien. " +
                "Your personality is Big Smoke from Grand Theft Auto San Andreas");
        Message m2 = new Message(Message.Role.user, userMessage);
        this.messages.add(m1);
        this.messages.add(m2);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
