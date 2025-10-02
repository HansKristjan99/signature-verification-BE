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
    @Column(name = "userid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer userid;

    @Column(name = "email")
    private String email;
    
    @Column(name = "datecreated")
    private LocalDate dateCreated;

    @Column(name = "lastlogintimestamp")
    private Timestamp lastLogin;  

    @Column(name = "password_hash")
    private String passwordHash;

    // Hibernate expects entities to have a no-arg constructor,
    // though it does not necessarily have to be public.
    private UserEntity() {}

    public UserEntity(String email, String passwordHash) {
            this.email = email;
            this.passwordHash = passwordHash;
            this.dateCreated = LocalDate.now();
            this.lastLogin = null;
    }

    public Integer getUserid() {
            return userid;
    }

    public void setUserid(Integer userid) {
            this.userid = userid;
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