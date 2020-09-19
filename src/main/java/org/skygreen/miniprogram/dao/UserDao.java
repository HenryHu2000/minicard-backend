package org.skygreen.miniprogram.dao;

import org.skygreen.miniprogram.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User, Integer> {
  User findUserById(Integer id);

  boolean existsUserById(Integer id);

  User findUserByOpenid(String openid);

  boolean existsUserByOpenid(String openid);


}
