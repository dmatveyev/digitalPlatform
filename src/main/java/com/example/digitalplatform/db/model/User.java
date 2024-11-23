package com.example.digitalplatform.db.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String login;
    private String password;
    private boolean enabled;
    @Column(name = "token_expired")
    private boolean tokenExpired;

    @OneToMany(mappedBy = "customer")
    private List<Request> created;
    @OneToMany(mappedBy = "worker")
    private List<Request> assigned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Role role;

}
