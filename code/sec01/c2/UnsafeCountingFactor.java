package sec01.c2;

import java.util.Arrays;

import static sec01.uitls.ArtificialUtils.factor;

/**
 * 2. 添加一个可变域 count, 它统计服务被调用的次数
 */
public class UnsafeCountingFactor {
    private long count = 0;
    public long getCount(){
        return count;
    }
    public void service(Integer i){
        Integer[] fs = factor(i);
        count++;    // 每被调用一次, 计数值加一
//        System.out.println(Arrays.toString(fs));
    }
    public static void main(String[] args) throws InterruptedException {
        UnsafeCountingFactor f = new UnsafeCountingFactor();
        int tn = 10000;
        Thread[] threads = new Thread[tn];
        for(int i=0; i<tn; i++)
            threads[i] = new Thread(()-> f.service(5));
        for(int i=0; i<tn; i++)
            threads[i].start();
        for(int i=0; i<tn; i++)
            threads[i].join();
        System.out.println(f.getCount()==tn);

    }
}
