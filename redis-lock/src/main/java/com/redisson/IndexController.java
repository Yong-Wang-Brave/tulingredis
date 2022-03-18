package com.redisson;

import org.apache.tomcat.jni.Thread;
import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class IndexController {

    @Autowired
    private Redisson redisson;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/deduct_stock")
    public String deductStock() {
        String lockKey = "product_101";
        String clientId = UUID.randomUUID().toString();
        RLock redissonLock = redisson.getLock(lockKey);
        try {
            //Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "zhuge"); //jedis.setnx(k,v)
            //stringRedisTemplate.expire(lockKey, 10, TimeUnit.SECONDS);

            /*Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, clientId, 30, TimeUnit.SECONDS);

            if (!result) {
                return "error_code";
            }*/

            //加锁
            redissonLock.lock();  //setIfAbsent(lockKey, clientId, 30, TimeUnit.SECONDS);
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock")); // jedis.get("stock")
            if (stock > 0) {
                int realStock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock", realStock + ""); // jedis.set(key,value)
                System.out.println("扣减成功，剩余库存:" + realStock);
            } else {
                System.out.println("扣减失败，库存不足");
            }

        } finally {
            redissonLock.unlock();
            /*if (clientId.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                stringRedisTemplate.delete(lockKey);
            }*/
        }

        return "end";
    }





    //如果该服务的端口没启动，nginx出问题 报错
    @RequestMapping("/deduct_stock1")
    public String deductStock1() throws InterruptedException{
        int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
      //这个关键字只在单机情况下有用。
       synchronized (this){
           if ((stock>0)) {
               int realStock=stock-1;
               stringRedisTemplate.opsForValue().set("stock",realStock+"");
               System.out.println("扣减成功，剩余库存："+realStock+"");
           }else{
               System.out.println("扣减失败，库存不足");
           }
       }
       return "end";
    }



   @RequestMapping("/deduct_stock2")
    public String deductStock2() throws InterruptedException{

        String lockKey="lockKey";
//始终只邮一把锁，分布式的状态下，谁拿到谁操作。
       Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "zhuge");
       if (!result) {
           return "1001";
       }
       int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
       if ((stock>0)) {
           int realStock=stock-1;
           stringRedisTemplate.opsForValue().set("stock",realStock+"");
           System.out.println("扣减成功，剩余库存："+realStock+"");
       }else{
           System.out.println("扣减成功，库存不足");
       }
stringRedisTemplate.delete(lockKey);
       return "end";
   }

//抛出异常也会释放锁
    @RequestMapping("/deduct_stock3")
    public  String deductStock3() throws InterruptedException{

        String lockKey="lockKey";
//始终只邮一把锁，分布式的状态下，谁拿到谁操作。
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "zhuge");
            if (!result) {
                return "1001";
            }
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
                if ((stock > 0)) {
                    int realStock = stock - 1;
                    stringRedisTemplate.opsForValue().set("stock", realStock + "");
                    System.out.println("扣减成功，剩余库存：" + realStock + "");
                } else {
                    System.out.println("扣减成功，库存不足");
                }
        } finally {
            stringRedisTemplate.delete(lockKey);
        }

        return "end";
    }

    @RequestMapping("/deduct_stock4")
    public String deductStock4() throws InterruptedException{

        String lockKey="lockKey";
//始终只邮一把锁，分布式的状态下，谁拿到谁操作。
        try {
            //设置key的超时时间 防止宕机（kill）引起的死锁。
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "zhuge",30,TimeUnit.SECONDS);
            if (!result) {
                return "1001";
            }
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if ((stock>0)) {
                int realStock=stock-1;
                stringRedisTemplate.opsForValue().set("stock",realStock+"");
                System.out.println("扣减成功，剩余库存："+realStock+"");
            }else{
                System.out.println("扣减成功，库存不足");
            }
        } finally {
            stringRedisTemplate.delete(lockKey);
        }

        return "end";
    }
    //reddision 实现锁续命
    @RequestMapping("/deduct_stock5")
    public String deductStock5() {
        String lockKey = "product_101";
        RLock redissonLock = redisson.getLock(lockKey);
        try {
            //加锁，实现锁续命
            redissonLock.lock();  //setIfAbsent(lockKey, clientId, 30, TimeUnit.SECONDS);
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock")); // jedis.get("stock")
            if (stock > 0) {
                int realStock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock", realStock + ""); // jedis.set(key,value)
                System.out.println("扣减成功，剩余库存:" + realStock);
            } else {
                System.out.println("扣减失败，库存不足");
            }
        } finally {
            redissonLock.unlock();
        }
        return "end";
    }
    @RequestMapping("/deduct_stock6")
    public String deductStock6() throws InterruptedException{

        String lockKey="lockKey";
//始终只邮一把锁，分布式的状态下，谁拿到谁操作。

        //谁加的锁谁去释放
        String clientId =UUID.randomUUID().toString();
        try {

            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if ((stock>0)) {
                int realStock=stock-1;
                stringRedisTemplate.opsForValue().set("stock",realStock+"");
                System.out.println("扣减成功，剩余库存："+realStock+"");
            }else{
                System.out.println("扣减成功，库存不足");
            }
        } finally {
            if (clientId.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                stringRedisTemplate.delete(lockKey);
            }

        }

        return "end";
    }
    @RequestMapping("/redlock")
    public String redlock() {
        String lockKey = "product_001";
        //这里需要自己实例化不同redis实例的redisson客户端连接，这里只是伪代码用一个redisson客户端简化了
        RLock lock1 = redisson.getLock(lockKey);
        RLock lock2 = redisson.getLock(lockKey);
        RLock lock3 = redisson.getLock(lockKey);

        /**
         * 根据多个 RLock 对象构建 RedissonRedLock （最核心的差别就在这里）
         */
        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
        try {
            /**
             * waitTimeout 尝试获取锁的最大等待时间，超过这个值，则认为获取锁失败
             * leaseTime   锁的持有时间,超过这个时间锁会自动失效（值应设置为大于业务处理的时间，确保在锁有效期内业务能处理完）
             */
            boolean res = redLock.tryLock(10, 30, TimeUnit.SECONDS);
            if (res) {
                //成功获得锁，在这里处理业务
            }
        } catch (Exception e) {
            throw new RuntimeException("lock fail");
        } finally {
            //无论如何, 最后都要解锁
            redLock.unlock();
        }

        return "end";
    }

    @RequestMapping("/get_stock")
    public String getStock(@RequestParam("clientId") Long clientId) throws InterruptedException {
        String lockKey = "product_stock_101";

        RReadWriteLock readWriteLock = redisson.getReadWriteLock(lockKey);
        RLock rLock = readWriteLock.readLock();

        rLock.lock();
        System.out.println("获取读锁成功：client=" + clientId);
        String stock = stringRedisTemplate.opsForValue().get("stock");
        if (StringUtils.isEmpty(stock)) {
            System.out.println("查询数据库库存为10。。。");
            java.lang.Thread.sleep(5000);
            stringRedisTemplate.opsForValue().set("stock", "10");
        }
        rLock.unlock();
        System.out.println("释放读锁成功：client=" + clientId);

        return "end";
    }

    @RequestMapping("/update_stock")
    public String updateStock(@RequestParam("clientId") Long clientId) throws InterruptedException {
        String lockKey = "product_stock_101";

        RReadWriteLock readWriteLock = redisson.getReadWriteLock(lockKey);
        RLock writeLock = readWriteLock.writeLock();

        writeLock.lock();
        System.out.println("获取写锁成功：client=" + clientId);
        System.out.println("修改商品101的数据库库存为6。。。");
        stringRedisTemplate.delete("stock");
        java.lang.Thread.sleep(5000);
        writeLock.unlock();
        System.out.println("释放写锁成功：client=" + clientId);

        return "end";
    }

}