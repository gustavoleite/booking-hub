package com.bookinghub.auth.infrastructure.adapters.out.database;

import com.bookinghub.auth.core.domain.Role;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_users")
@Getter
@Setter
public class UserEntity {

  @Id
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "tb_user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role_name")
  @Enumerated(EnumType.STRING)
  private Set<Role> roles;

  private boolean active;
}
