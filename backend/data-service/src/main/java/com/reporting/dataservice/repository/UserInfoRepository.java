package com.reporting.dataservice.repository;

import com.reporting.dataservice.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
    @Query("SELECT u FROM UserInfo u WHERE u.role = ?1 OR u.role = 'admin' ORDER BY u.name")
    List<UserInfo> findByRoleOrAdmin(String role);
    
    @Query("SELECT u FROM UserInfo u ORDER BY u.name")
    List<UserInfo> findAllOrderByName();
}
