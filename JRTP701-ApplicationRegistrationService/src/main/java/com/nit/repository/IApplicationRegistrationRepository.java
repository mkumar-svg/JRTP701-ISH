package com.nit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nit.entity.CitizenAppRegistrationEntity;

public interface IApplicationRegistrationRepository extends JpaRepository<CitizenAppRegistrationEntity, Integer> {

}
