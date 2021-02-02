package com.wayapay.thirdpartyintegrationservice.repo;

import com.wayapay.thirdpartyintegrationservice.model.ThirdPartyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ThirdPartyConfigRepo extends JpaRepository<ThirdPartyConfig, Long> {

}
