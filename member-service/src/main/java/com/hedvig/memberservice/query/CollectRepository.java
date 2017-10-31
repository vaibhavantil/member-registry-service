package com.hedvig.memberservice.query;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectRepository extends JpaRepository<CollectType, String> {
}
