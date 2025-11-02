package com.vericode.data.User;

import java.sql.Timestamp;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(name = "email")
    private String email;

    @Column(name = "date_created")
    private LocalDate dateCreated;

    @Column(name = "last_login_timestamp")
    private Timestamp lastLogin;

    @Column(name = "password_hash")
    private String passwordHash;

    // Hibernate expects entities to have a no-arg constructor,
    // though it does not necessarily have to be public.
    UserEntity() {}

    public UserEntity(String email, String passwordHash) {
            this.email = email;
            this.passwordHash = passwordHash;
            this.dateCreated = LocalDate.now();
            this.lastLogin = null;
    }

    public Integer getUserId() {
            return userId;
    }

    public void setUserId(Integer userId) {
            this.userId = userId;
    }

    public String getEmail() {
            return email;
    }

    public void setEmail(String email) {
            this.email = email;
    }

    public LocalDate getDateCreated() {
            return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
            this.dateCreated = dateCreated;
    }

    public Timestamp getLastLogin() {
            return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
            this.lastLogin = lastLogin;
    }

    public String getPasswordHash() {
            return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
    }

}