package com.example.sweater.entity;

import javax.persistence.*;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String text;
    private String tag;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User author;

    @Lob
    private byte[] image;

    public Message() {
    }

    public Message(String text, String tag, User author, byte[] image) {
        this.text = text;
        this.tag = tag;
        this.author = author;
        this.image = image;
    }

    public Message(Long id, String text, String tag, User author, byte[] image) {
        this.id = id;
        this.text = text;
        this.tag = tag;
        this.author = author;
        this.image = image;
    }

    public Message(String text, String tag, User author) {
        this.text = text;
        this.tag = tag;
        this.author = author;
    }

    public byte[] getImage() {
        return this.image;
    }

    public String getImgData() {
        return Base64.getMimeEncoder().encodeToString(this.image);
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}