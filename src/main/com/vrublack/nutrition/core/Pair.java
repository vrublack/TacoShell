package com.vrublack.nutrition.core;

import java.io.Serializable;

public class Pair<A, B> implements Serializable, Comparable<Pair<A, B> >
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

    @Override
    public int compareTo(Pair<A, B> o)
    {
        if (!(first instanceof Comparable) || !(second instanceof Comparable))
            return 0;

        int compFirst = ((Comparable) first).compareTo(o.first);
        if (compFirst != 0)
            return compFirst;
        else
            return ((Comparable) second).compareTo(o.second);
    }
}
