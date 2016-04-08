package com.vrublack.nutrition.core;

import java.io.Serializable;

public class Pair<A, B> implements Serializable
{
    private static final long serialVersionUID = 10;

    public A first;
    public B second;

    public Pair(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public Pair()
    {

    }
}
