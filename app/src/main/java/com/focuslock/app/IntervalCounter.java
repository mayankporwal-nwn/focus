package com.focuslock.app;

/** Pure-Java interval accumulator to make usage counting testable. */
public final class IntervalCounter {
    private final long dayStart;
    private long started=-1L;
    private long total=0L;
    private String lastActivity=null;
    public IntervalCounter(long dayStart) {this.dayStart=dayStart;}
    public void resume(long at,String activity) {
        if(started<0) started=at;
        lastActivity=activity;
    }
    public void pause(long at,String activity) {
        if(started>=0 && (lastActivity==null || lastActivity.equals(activity))) stop(at);
    }
    public void stop(long at) {
        if(started>=0) {
            long begin=Math.max(dayStart,started);
            total+=Math.max(0,at-begin);
        }
        started=-1;lastActivity=null;
    }
    public long totalUntil(long at) {
        long extra=started>=0?Math.max(0,at-Math.max(dayStart,started)):0;
        return Math.min(Math.max(0,at-dayStart),total+extra);
    }
}
