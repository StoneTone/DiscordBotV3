package com.bot.discordbotv4.gpt;

class Message {
    Role role;
    String content;

    public Message(Role role, String content){
        this.role = role;
        this.content = content;
    }

    enum Role {
        user,
        system,
        assistant
    }
}
