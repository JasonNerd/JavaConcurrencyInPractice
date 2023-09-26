package sec01.c2;

import java.util.Arrays;

import static sec01.uitls.ArtificialUtils.factor;

/**
 * 1. 无状态的因数分解类, 该类不包含任何域或者对其他类域的引用
 */
public class StatelessFactor {
    public void service(Integer i){
        Integer[] fs = factor(i);
        System.out.println(Arrays.toString(fs));
    }

    public static void main(String[] args) {
        StatelessFactor f = new StatelessFactor();
        f.service(5);
    }
}
