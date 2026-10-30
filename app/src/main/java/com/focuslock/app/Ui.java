package com.focuslock.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Lightweight offline UI without external libraries or network dependencies. */
public final class Ui {
    private Ui() {}
    public static final int BG=Color.rgb(246,248,251),INK=Color.rgb(25,43,62),MUTED=Color.rgb(98,115,129);
    public static final int TEAL=Color.rgb(0,111,104),PALE=Color.rgb(232,247,242),CORAL=Color.rgb(252,189,130);
    public static int dp(Context c,float x) {return (int)(x*c.getResources().getDisplayMetrics().density+.5f);}
    public static GradientDrawable shape(int color,int radius,Context c) {
        GradientDrawable g=new GradientDrawable(); g.setColor(color);g.setCornerRadius(dp(c,radius));return g;
    }
    public static LinearLayout column(Context c,int pad) {
        LinearLayout x=new LinearLayout(c);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(c,pad),dp(c,pad),dp(c,pad),dp(c,pad));return x;
    }
    public static LinearLayout row(Context c) {
        LinearLayout x=new LinearLayout(c);x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);return x;
    }
    public static TextView text(Context c,String txt,int sp,int color,boolean bold) {
        TextView t=new TextView(c);t.setText(txt);t.setTextSize(sp);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setLineSpacing(dp(c,2),1f);return t;
    }
    public static TextView spaceText(Context c,String txt,int sp,int color,boolean bold,int top) {
        TextView t=text(c,txt,sp,color,bold);t.setPadding(0,dp(c,top),0,dp(c,7));return t;
    }
    public static LinearLayout card(Context c,LinearLayout parent,int color) {
        LinearLayout card=column(c,17);card.setBackground(shape(color,18,c));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(c,7),0,dp(c,7));parent.addView(card,lp);return card;
    }
    public static Button button(Context c,String txt,boolean primary) {
        Button b=new Button(c);b.setText(txt);b.setTextAllCaps(false);b.setTextSize(14);
        b.setTextColor(primary?Color.WHITE:TEAL);
        b.setBackground(shape(primary?TEAL:PALE,13,c));
        b.setPadding(dp(c,10),dp(c,2),dp(c,10),dp(c,2));
        b.setMinHeight(dp(c,46));return b;
    }
    public static void addButton(Context c,LinearLayout root,Button b) {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(c,47));lp.setMargins(0,dp(c,5),0,dp(c,5));root.addView(b,lp);
    }
    public static void spacer(Context c,LinearLayout l,int h) {View v=new View(c);l.addView(v,new LinearLayout.LayoutParams(1,dp(c,h)));}
}
