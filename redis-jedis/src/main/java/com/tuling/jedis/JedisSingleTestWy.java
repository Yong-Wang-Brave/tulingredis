package com.tuling.jedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
/**
 * 访问redis单机
 *
 * @author 图灵-诸葛老师
 */
public class JedisSingleTestWy {
    public static void main(String[] args) throws IOException {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);

        // timeout，这里既是连接超时又是读写超时，从Jedis 2.8开始有区分connectionTimeout和soTimeout的构造函数
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379, 3000, null);

        Jedis jedis = null;
        try {
            //从redis连接池里拿出一个连接执行命令
            jedis = jedisPool.getResource();

            //lua脚本模拟一个商品减库存的原子操作
            //lua脚本命令执行方式：redis-cli --eval /tmp/test.lua , 10
            jedis.set("product_count_10016", "15");  //初始化商品10016的库存
            String script = " local count = redis.call('get', KEYS[1]) " +
                    " local a = tonumber(count) " +
                    " local b = tonumber(ARGV[1]) " +
                    " if a >= b then " +
                    "   redis.call('set', KEYS[1], a-b) " +
                    "   return 1 " +
                    //lua脚本赋值是=不是==，所以会报错
                    /*"   a==b" +*/
                    " end " +
                    " return 0 ";
            Object obj = jedis.eval(script, Arrays.asList("product_count_10016"), Arrays.asList("10"));
            System.out.println(obj);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池。
            if (jedis != null)
                jedis.close();
        }
    }
}

