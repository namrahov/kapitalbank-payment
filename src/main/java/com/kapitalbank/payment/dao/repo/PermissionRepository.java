package com.kapitalbank.payment.dao.repo;

import com.kapitalbank.payment.dao.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
