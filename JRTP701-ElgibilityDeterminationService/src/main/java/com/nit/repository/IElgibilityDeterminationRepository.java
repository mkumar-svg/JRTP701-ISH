package com.nit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nit.entity.ElgibilityDetailsEntity;

public interface IElgibilityDeterminationRepository extends JpaRepository<ElgibilityDetailsEntity, Integer> {

}
