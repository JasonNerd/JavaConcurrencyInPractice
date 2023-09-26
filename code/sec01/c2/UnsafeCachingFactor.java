package sec01.c2;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static sec01.uitls.ArtificialUtils.factor;

/**
 * 4. 需要实现缓存机制: 将上一次计算的 factors 以及对应的 i 缓存起来.
 * 此时即使两个状态都是原子类(线程安全的). 然而, 由于不变性条件是要保证
 * 缓存的 factors 中各数字之积为 i. 我们无法同时写入 i 和 factors, 也
 * 无法保证同时读取到 i 和 factors.
 */
public class UnsafeCachingFactor {
    private final AtomicReference<Integer> lastNumber = new AtomicReference<>();
    private final AtomicReference<Integer[]> lastFactors = new AtomicReference<>();
    public void service(Integer i){
        if (i.equals(lastNumber.get())){
            System.out.println("Cache hit @ "+ Arrays.toString(lastFactors.get()));
        }else{
            Integer[] factors = factor(i);
            lastNumber.set(i);
            lastFactors.set(factors);
            System.out.println("save to cache.");
        }
    }
}
