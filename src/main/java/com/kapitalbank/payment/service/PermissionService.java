package com.kapitalbank.payment.service;

import com.kapitalbank.payment.dao.entity.Permission;
import com.kapitalbank.payment.dao.repo.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public Set<Permission> findAll() {
        return new HashSet<>(permissionRepository.findAll());
    }

}
