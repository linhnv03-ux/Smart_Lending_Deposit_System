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
@Table("users")
public class UserEntity {

    @Id
    private Long id;

    private String username;

    private String email;

    private String password;

    @Column("full_name")
    private String fullName;

    private String role;

    @Column("branch_code")
    private String branchCode;

    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
