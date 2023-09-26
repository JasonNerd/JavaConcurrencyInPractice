package sec01.c2;

import java.util.concurrent.atomic.AtomicLong;

import static sec01.uitls.ArtificialUtils.factor;

/**
 * 3. 先不考虑锁机制, 使用线程安全的类来管理新增的一个状态
 */
public class CountingFactor {
    private AtomicLong counter = new AtomicLong(0);
    public long getCount(){
        return counter.get();
    }
    public void service(Integer i){
        Integer[] fs = factor(i);
        counter.addAndGet(1);    // 每被调用一次, 计数值加一
    }
}
