package com.abdev.fastballlauncher;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class AppDetail implements Serializable
{
    public AppDetail(){}
    public AppDetail(CharSequence label,CharSequence name,Drawable icon)
    {
        this.label = label;
        this.name = name;
        this.icon = icon;
    }

    CharSequence label;
    CharSequence name;
    Drawable icon;

    @Override
    public String toString() {
        return "{"+label+" "+name+" "+icon+"}";
    }
}