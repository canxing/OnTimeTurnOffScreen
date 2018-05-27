package com.example.canxing.ontimeturnoffscreen.util;

public class Tuple {
    public static <A, B> TwoTuple<A, B> towTuple(A a, B b){
        return new TwoTuple<>(a, b);
    }
    public static <A, B, C> ThreeTuple<A, B, C> threeTuple(A a, B b, C c){
        return new ThreeTuple<>(a, b, c);
    }
    public static <A, B, C, D> FourTuple<A, B, C, D> fourTuple(A a, B b, C c, D d){
        return new FourTuple<>(a, b, c, d);
    }
}
