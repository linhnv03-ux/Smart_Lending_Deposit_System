package com.bank.slds.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("USERS")
public class UserEntity {

    @Id
    @Column("ID")
    private Long id;

    @Column("USERNAME")
    private String username;

    @Column("EMAIL")
    private String email;

    @Column("PASSWORD")
    private String password;

    @Column("FULL_NAME")
    private String fullName;

    @Column("ROLE")
    private String role;

    @Column("BRANCH_CODE")
    private String branchCode;

    @Column("STATUS")
    private String status;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;
}
