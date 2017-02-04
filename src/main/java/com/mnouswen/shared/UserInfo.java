package com.mnouswen.shared;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "nickname")
    private String nickname;

    @Basic
    @Column(name = "picture")
    private String pictureUrl;

    @Transient
    private String sessionId;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String emailAddress) {
        this.email = emailAddress;
    }

    public String getName() {
        return nickname;
    }

    public void setName(final String nickname) {
        this.nickname = nickname;
    }

    public void setPictureUrl(final String pictureUrl) {
        this.pictureUrl = pictureUrl;

    }

    public String getPictureUrl() {
        return pictureUrl;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "emailAddress='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", pictureUrl='" + pictureUrl + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}