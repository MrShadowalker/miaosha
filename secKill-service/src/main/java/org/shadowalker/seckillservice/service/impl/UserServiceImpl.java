package org.shadowalker.seckillservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.shadowalker.seckilldao.dao.Stock;
import org.shadowalker.seckilldao.dao.User;
import org.shadowalker.seckilldao.mapper.UserMapper;
import org.shadowalker.seckilldao.utils.CacheKey;
import org.shadowalker.seckillservice.service.StockService;
import org.shadowalker.seckillservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String SALT = "randomString";
    private static final int ALLOW_COUNT = 10;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StockService stockService;

    @Override
    public String getVerifyHash(Integer sid, Integer userId) throws Exception {

        // 验证是否在抢购时间内
        log.info("请自行验证是否在抢购时间内");

        // 检查用户合法性
        User user = userMapper.selectByPrimaryKey(userId.longValue());
        if (user == null) {
            throw new Exception("用户不存在");
        }
        log.info("用户信息:【{}】", user.toString());

        // 检查商品合法性
        Stock stock = stockService.getStockById(sid);
        if (stock == null) {
            throw new Exception("商品不存在");
        }
        log.info("商品信息:【{}】", stock.toString());

        // 生成 hash
        String verify = SALT + sid + userId;
        String verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes());

        // 将 hash 和用户商品信息存入 Redis
        String hashKey = CacheKey.HASH_KEY.getKey() + "_" + sid + "_" + userId;
        stringRedisTemplate.opsForValue().set(hashKey, verifyHash, 3600, TimeUnit.SECONDS);
        log.info("Redis 写入:【{}】 【{}】", hashKey, verifyHash);
        return verifyHash;
    }

    @Override
    public int addUserCount(Integer userId) throws Exception {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        stringRedisTemplate.opsForValue().setIfAbsent(limitKey, "0", 3600, TimeUnit.SECONDS);
        Long limit = stringRedisTemplate.opsForValue().increment(limitKey);
        return Integer.parseInt(String.valueOf(limit));
    }

    @Override
    public boolean getUserIsBanned(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY.getKey() + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            log.error("该用户没有访问申请验证值记录，疑似异常");
            return true;
        }
        return Integer.parseInt(limitNum) > ALLOW_COUNT;
    }
}
