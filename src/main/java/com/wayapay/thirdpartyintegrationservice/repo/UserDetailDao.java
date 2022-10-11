package com.wayapay.thirdpartyintegrationservice.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public class UserDetailRepo {
    public static final String HASH_KEY = "Users";

    @SuppressWarnings("rawtypes")

    @Autowired
    private RedisTemplate template;

    @SuppressWarnings("unchecked")
    public Users save(Users redisUser) {
        template.opsForHash().put(HASH_KEY, redisUser.getId(), redisUser);
        return redisUser;
    }

    @SuppressWarnings("unchecked")
    public List<Users> findAll() {
        return template.opsForHash().values(HASH_KEY);
    }

    @SuppressWarnings("unchecked")
    public Users findUserById(int id) {
        return (Users) template.opsForHash().get(HASH_KEY, id);
    }

    @SuppressWarnings("unchecked")
    public String deleteUser(int id) {
        template.opsForHash().delete(HASH_KEY, id);
        return "user deleted !!";
    }
}
