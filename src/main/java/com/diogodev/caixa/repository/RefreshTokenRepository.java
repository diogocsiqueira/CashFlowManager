package com.diogodev.caixa.repository;

import com.diogodev.caixa.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
    void deleteByUserId(Long userId);

    @Query("""
    select rt from RefreshToken rt
    join fetch rt.user u
    left join fetch u.roles
    where rt.tokenHash = :hash and rt.revoked = false
""")
    Optional<RefreshToken> findValidWithUserAndRoles(@Param("hash") String hash);

}
