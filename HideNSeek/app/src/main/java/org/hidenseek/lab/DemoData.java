package org.hidenseek.lab;

import android.graphics.Color;

final class DemoData {
    static final class Conversation {
        final String id, name, initials, preview, time;
        final int color;
        final Message[] messages;

        Conversation(String id, String name, String initials, String preview, String time,
                     int color, Message... messages) {
            this.id = id;
            this.name = name;
            this.initials = initials;
            this.preview = preview;
            this.time = time;
            this.color = color;
            this.messages = messages;
        }
    }

    static final class Message {
        final String body, time;
        final boolean mine;
        final int mediaId;

        Message(String body, String time, boolean mine) {
            this(body, time, mine, -1);
        }

        Message(String body, String time, boolean mine, int mediaId) {
            this.body = body;
            this.time = time;
            this.mine = mine;
            this.mediaId = mediaId;
        }
    }

    static final class Media {
        final int id;
        final String conversation, title;
        final boolean identity;

        Media(int id, String conversation, String title, boolean identity) {
            this.id = id;
            this.conversation = conversation;
            this.title = title;
            this.identity = identity;
        }
    }

    static final Conversation[] CONVERSATIONS = {
        new Conversation("family", "Family Group", "FG", "Lena sent a secure photo", "09:42",
            Color.rgb(239, 158, 100),
            new Message("Good morning everyone!", "08:57", false),
            new Message("Sunday lunch at our place?", "09:03", true),
            new Message("That works for us 😊", "09:08", false),
            new Message("Found the old family photo too", "09:18", false, 3),
            new Message("Sunday paella idea", "09:20", false, 4),
            new Message("I'll bring the paperwork.", "09:31", false),
            new Message("Lena sent a secure photo", "09:42", false, 5)),
        new Conversation("maya", "Maya Chen", "MC", "The view was unreal 🌤", "Yesterday",
            Color.rgb(127, 185, 177),
            new Message("Are you still up for the trail?", "17:21", true),
            new Message("Absolutely. Sending a photo from last time", "17:23", false, 0),
            new Message("The view was unreal 🌤", "17:24", false)),
        new Conversation("theo", "Theo Martin", "TM", "Tickets are booked!", "Yesterday",
            Color.rgb(149, 141, 210),
            new Message("Did you get the seats?", "20:06", true),
            new Message("Tickets are booked!", "20:09", false, 1)),
        new Conversation("alex", "Alex Rivera", "AR", "See you at the café", "Tuesday",
            Color.rgb(221, 166, 134),
            new Message("Coffee next week?", "11:10", false),
            new Message("Of course!", "11:13", true),
            new Message("See you at the café", "11:16", false, 2)),
        new Conversation("nora", "Nora Patel", "NP", "These made my day 🌼", "Monday",
            Color.rgb(136, 174, 218),
            new Message("Happy birthday!", "10:03", true),
            new Message("These made my day 🌼", "10:06", false, 6))
    };

    static final Media[] MEDIA = {
        new Media(0, "maya", "Mountain morning", false),
        new Media(1, "theo", "Concert night", false),
        new Media(2, "alex", "Corner café", false),
        new Media(3, "family", "Family photograph", false),
        new Media(4, "family", "Sunday paella", false),
        new Media(5, "family", "National identity card", true),
        new Media(6, "nora", "Birthday flowers", false)
    };

    static Conversation conversation(String id) {
        for (Conversation item : CONVERSATIONS) if (item.id.equals(id)) return item;
        return CONVERSATIONS[0];
    }

    static Media media(int slot) {
        for (Media item : MEDIA) if (item.id == slot) return item;
        return null;
    }

    private DemoData() { }
}
