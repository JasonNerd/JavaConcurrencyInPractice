package sec01.c2;

import java.util.Arrays;

import static sec01.uitls.ArtificialUtils.factor;

/**
 * 5. 在 4 的基础上再添加 计算缓存命中率(cache hit ratio) 的功能,
 * 它需要统计 服务被调用次数 hits 和 缓存命中次数 cacheHits
 */
public class CachedFactor {
    // 该类具有四个可变域(状态), 需要统一一把锁进行管理
    private Integer lastNumber;
    private Integer[] lastFactors;
    private long hits;
    private long cacheHits;
    /** 返回服务调用次数 */
    public synchronized long getHits(){
        return hits;
    }
    /** 计算命中率 */
    public synchronized double getCacheHitRatio(){
        return (double)cacheHits / (double)hits;
    }
    /** 服务程序 */
    public void service(Integer i){
        Integer[] factors = null;
        // 1. 该同步块判断是否命中, 并将 lastFactors 复制到 本地变量
        synchronized (this) {
            hits++;
            if (i.equals(lastNumber)) {
                cacheHits++;
                factors = lastFactors.clone();
            }
        }
        // 2. 在非同步的情况下, 可以使用本地变量进行判断和计算等操作
        if (factors == null)
            factors = factor(i);    // 此为耗时操作, 尽量并发进行(也即不要将它放在同步代码块内)
        // 3. 接下来的同步块更新 lastNumber 和 lastFactors
        synchronized (this){
            lastNumber = i;
            lastFactors = factors.clone();
        }
//        System.out.println(Arrays.toString(factors));   // 模拟封装响应
    }
    /** 测试代码 */
    public static void main(String[] args) throws InterruptedException {
        CachedFactor f = new CachedFactor();
        int tn = 1000;
        Thread[] threads = new Thread[tn];
        for(int i=0; i<tn; i++) {
            Integer j = (int) (Math.random()*10);   // 0-9 的数
            threads[i] = new Thread(() -> f.service(j));
        }
        for(int i=0; i<tn; i++)
            threads[i].start();
        for(int i=0; i<tn; i++)
            threads[i].join();
        // 查看缓存命中率
        System.out.println(f.getHits());
        System.out.println(f.getCacheHitRatio());
    }
}
