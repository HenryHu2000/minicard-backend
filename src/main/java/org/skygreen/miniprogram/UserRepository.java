package org.skygreen.miniprogram;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
  User findUserById(Integer id);

  boolean existsUserById(Integer id);

  User findUserByOpenid(String openid);

  boolean existsUserByOpenid(String openid);


}
