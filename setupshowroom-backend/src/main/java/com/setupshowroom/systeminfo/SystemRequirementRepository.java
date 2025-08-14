package com.setupshowroom.systeminfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemRequirementRepository extends JpaRepository<SystemRequirement, String> {}
