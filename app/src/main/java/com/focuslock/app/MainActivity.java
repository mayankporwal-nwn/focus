package com.focuslock.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Offline-first focus companion. No network, ads or third-party SDKs. */
public class MainActivity extends Activity {
    private static final String[] TABS={"Today","Limits","Focus","Routine","Toolkit"};
    private static final String TIMER_PREF="focus_timer";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable timerTicker;
    private int tab=0,ideaIndex=0,tipIndex=0;
    private LinearLayout page;
    private List<AppItem> appCache;
    private boolean showAllApps=false;
    private TextView timerReadout;

    private static final class AppItem {
        final String pkg,name;
        AppItem(String p,String n) {pkg=p;name=n;}
    }
    private int dp(float n) {return Ui.dp(this,n);}
    private void title(LinearLayout dest,String value) {dest.addView(Ui.spaceText(this,value,25,Ui.INK,true,5));}
    private void section(LinearLayout dest,String value) {dest.addView(Ui.spaceText(this,value,18,Ui.INK,true,18));}
    private void para(LinearLayout dest,String value) {dest.addView(Ui.spaceText(this,value,14,Ui.MUTED,false,0));}
    private void small(LinearLayout dest,String value) {dest.addView(Ui.spaceText(this,value,12,Ui.MUTED,false,5));}
    private void addButton(LinearLayout dest,String value,boolean primary,Runnable task) {
        Button b=Ui.button(this,value,primary);Ui.addButton(this,dest,b);b.setOnClickListener(v -> task.run());
    }
    private void toast(String text) {Toast.makeText(this,text,Toast.LENGTH_SHORT).show();}

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        if(b!=null) tab=b.getInt("tab",0);
        LimitStore.applyDue(this);
        ideaIndex=Math.floorMod(DayKeys.today().hashCode(),FocusContent.IDEAS.length);
        tipIndex=Math.floorMod(DayKeys.today().hashCode()+37,FocusContent.TIPS.length);
        draw();
    }
    @Override protected void onSaveInstanceState(Bundle b) {b.putInt("tab",tab);super.onSaveInstanceState(b);}
    @Override protected void onResume() {
        super.onResume();
        long deadline=timerPrefs().getLong("deadline",0L);
        if(deadline>0 && deadline<=SystemClock.elapsedRealtime()) {
            endTimer();DailyStore.completedFocus(this);
            toast("Focus session completed! Welcome back.");
        }
        if(page!=null)draw();
    }
    @Override protected void onPause() {stopTicker();super.onPause();}
    @Override protected void onDestroy() {stopTicker();super.onDestroy();}
    private void switchTo(int t) {tab=t;stopTicker();draw();}
    private void draw() {
        stopTicker();
        LinearLayout shell=Ui.column(this,0);
        shell.setPadding(0,0,0,0);shell.setBackgroundColor(Ui.BG);
        shell.setOrientation(LinearLayout.VERTICAL);
        if(Build.VERSION.SDK_INT>=35) {
            shell.setOnApplyWindowInsetsListener((v,insets)-> {
                Insets bars=insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
                return insets;
            });
        }
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setClipToPadding(false);
        page=Ui.column(this,19);page.setPadding(dp(19),dp(19),dp(19),dp(30));
        scroll.addView(page,new ScrollView.LayoutParams(-1,-2));
        shell.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=Ui.row(this);nav.setPadding(dp(7),dp(5),dp(7),dp(9));nav.setBackgroundColor(Color.WHITE);
        for(int i=0;i<TABS.length;i++) {
            final int target=i;
            Button b=Ui.button(this,TABS[i],i==tab);
            b.setTextSize(11);b.setMinHeight(dp(48));b.setPadding(dp(1),0,dp(1),0);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(49),1);
            lp.setMargins(dp(2),0,dp(2),0);nav.addView(b,lp);
            b.setOnClickListener(v -> switchTo(target));
        }
        shell.addView(nav,new LinearLayout.LayoutParams(-1,-2));
        setContentView(shell);
        switch(tab) {
            case 0: today();break;
            case 1: limits();break;
            case 2: focus();break;
            case 3: routine();break;
            default: toolkit();break;
        }
    }
    private void permissionsCard(LinearLayout dest) {
        boolean usage=Permissions.usage(this),blocking=Permissions.blocking(this);
        if(usage && blocking) return;
        LinearLayout card=Ui.card(this,dest,Color.rgb(255,242,221));
        card.addView(Ui.spaceText(this,"Finish setup to enable blocking",17,Ui.INK,true,0));
        para(card,"FocusLock needs two Android permissions. No permission is requested silently.");
        addButton(card,usage?"Usage access: ON":"1. Grant usage access",false,()->openSettings(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        addButton(card,blocking?"App blocker: ON":"2. Enable app blocking",true,this::discloseAccessibility);
        small(card,"Usage Access reads on-device app usage. Accessibility sees only which app/window appears, to send an over-limit app to Home. No screen text is read or shared.");
    }
    private void openSettings(String action) {
        try {startActivity(new Intent(action));} catch(ActivityNotFoundException e) {toast("Open Android Settings and find Special App Access.");}
    }
    private void discloseAccessibility() {
        new AlertDialog.Builder(this)
            .setTitle("Accessibility access: please read")
            .setMessage("FocusLock will receive app/window-change events, identify the foreground app, and return you to your Home screen when a limit is exceeded. It does not read messages, passwords or screen contents, and sends no data to a server. Android lets you revoke this sensitive permission at any time. Blocking is not tamper-proof.\n\nDo you consent to enable this feature?")
            .setPositiveButton("I consent — open settings",(d,w)->openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            .setNegativeButton("Not now",(d,w)->{})
            .show();
    }
    private void today() {
        LinearLayout hero=Ui.card(this,page,Ui.INK);
        hero.addView(Ui.spaceText(this,"FOCUSLOCK",12,Color.rgb(172,228,216),true,0));
        hero.addView(Ui.spaceText(this,"Less scrolling.\nMore living.",29,Color.WHITE,true,6));
        hero.addView(Ui.spaceText(this,"You don't need a perfect day. You need a worthwhile next step.",14,Color.rgb(224,235,240),false,4));
        permissionsCard(page);
        Map<String,Integer> configured=LimitStore.all(this);
        LinearLayout summary=Ui.card(this,page,Color.WHITE);
        summary.addView(Ui.spaceText(this,"Your progress today",18,Ui.INK,true,0));
        para(summary,configured.size()+" apps with daily limits  •  "+DailyStore.routineCount(this)+"/"+FocusContent.ROUTINE_NAMES.length+" routines  •  "+DailyStore.alternatives(this)+" alternatives  •  "+DailyStore.focusSessions(this)+" focus sessions");
        if(configured.isEmpty()) {
            para(summary,"Start by choosing one social app. Try a realistic limit rather than cutting everything at once.");
            addButton(summary,"Set your first app limit",true,()->switchTo(1));
        } else {
            if(!Permissions.usage(this)) small(summary,"Grant usage permission to see time remaining.");
            for(Map.Entry<String,Integer> e:configured.entrySet()) {
                String pkg=e.getKey();String name=appName(pkg);
                long used=Permissions.usage(this)?UsageClock.usedMillis(this,pkg)/60000:0;
                int limit=e.getValue();
                LinearLayout r=Ui.row(this);
                TextView nameText=Ui.text(this,name,14,Ui.INK,true);
                r.addView(nameText,new LinearLayout.LayoutParams(0,-2,1));
                r.addView(Ui.text(this,Permissions.usage(this)?Math.min(used,9999)+" / "+limit+"m":"— / "+limit+"m",12,Ui.MUTED,false));
                summary.addView(r);
                ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
                progress.setMax(limit);progress.setProgress((int)Math.min(used,limit));
                progress.setProgressTintList(android.content.res.ColorStateList.valueOf(used>=limit?Color.rgb(216,91,83):Ui.TEAL));
                LinearLayout.LayoutParams bar=new LinearLayout.LayoutParams(-1,dp(8));bar.setMargins(0,dp(5),0,dp(14));summary.addView(progress,bar);
            }
        }
        String intention=DailyStore.intention(this);
        LinearLayout pledge=Ui.card(this,page,Ui.PALE);
        pledge.addView(Ui.spaceText(this,"Your reason to focus",17,Ui.INK,true,0));
        pledge.addView(Ui.spaceText(this,intention.isEmpty()?"What would you like more time for? Add your own reason in Toolkit.":intention,14,Ui.INK,false,5));
        addButton(pledge,"Start a 15-minute focus sprint",true,()->{startTimer(15);switchTo(2);});
        section(page,"A better next action");
        LinearLayout task=Ui.card(this,page,Color.WHITE);
        task.addView(Ui.spaceText(this,"Try this instead of scrolling",17,Ui.INK,true,0));
        para(task,FocusContent.IDEAS[ideaIndex%FocusContent.IDEAS.length][1]+": "+FocusContent.IDEAS[ideaIndex%FocusContent.IDEAS.length][2]);
        addButton(task,"Give me another idea",false,()->{ideaIndex++;draw();});
        section(page,"Small wins, not perfection");
        para(page,"Checked off "+DailyStore.routineCount(this)+" daily actions. Missed one? Restart with the next one.");
        addButton(page,"Open today's routine",false,()->switchTo(3));
        addButton(page,"Privacy, permissions and medical notes",false,this::showInfo);
    }
    private void limits() {
        title(page,"Daily app limits");
        para(page,"Choose what deserves your attention. When a selected app reaches its allowance, FocusLock attempts to return you to Home.");
        permissionsCard(page);
        LinearLayout advice=Ui.card(this,page,Ui.PALE);
        advice.addView(Ui.spaceText(this,"Optional strict mode",16,Ui.INK,true,0));
        para(advice,"Set your limits first. With strict mode ON, later changes take effect the next local day. This discourages impulsive edits, but cannot prevent uninstalling or disabling permissions.");
        boolean strict=LimitStore.strict(this);
        addButton(advice,strict?"Strict mode: ON (changes tomorrow)":"Enable next-day-only edits",strict==false,()->{
            if(!LimitStore.strict(this)) {
                new AlertDialog.Builder(this).setTitle("Turn on strict mode?")
                    .setMessage("All limit edits will be scheduled for tomorrow, including removing limits. You can disable strict mode starting tomorrow. Android settings and uninstall remain outside this app's control.")
                    .setPositiveButton("Enable",(d,w)->{LimitStore.enableStrict(this);draw();})
                    .setNegativeButton("Cancel",null).show();
            } else if(LimitStore.disableStrict(this)) {toast("Strict mode disabled");draw();}
            else toast("Strict mode can be turned off tomorrow.");
        });
        section(page,"Choose apps");
        EditText search=new EditText(this);
        search.setSingleLine(true);search.setHint("Search installed apps, e.g. Instagram");search.setTextSize(15);
        search.setPadding(dp(12),dp(12),dp(12),dp(12));search.setBackground(Ui.shape(Color.WHITE,12,this));
        page.addView(search,new LinearLayout.LayoutParams(-1,dp(50)));
        LinearLayout list=Ui.column(this,0);list.setPadding(0,dp(4),0,0);page.addView(list);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int count,int after) {}
            @Override public void onTextChanged(CharSequence s,int st,int before,int count) {renderAppList(list,s.toString());}
            @Override public void afterTextChanged(Editable e) {}
        });
        renderAppList(list,"");
        small(page,"Safety: FocusLock deliberately excludes system settings, launchers, emergency and phone apps. Android may have additional special restrictions.");
    }
    private List<AppItem> installedApps() {
        if(appCache!=null)return appCache;
        PackageManager pm=getPackageManager();
        Intent intent=new Intent(Intent.ACTION_MAIN);intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found=pm.queryIntentActivities(intent,0);
        Map<String,String> unique=new HashMap<>();
        for(ResolveInfo ri:found) {
            if(ri.activityInfo==null) continue;
            String pkg=ri.activityInfo.packageName;
            if(SafetyPolicy.canBlock(pkg)) unique.put(pkg,String.valueOf(ri.loadLabel(pm)));
        }
        appCache=new ArrayList<>();
        for(Map.Entry<String,String> e:unique.entrySet()) appCache.add(new AppItem(e.getKey(),e.getValue()));
        Collections.sort(appCache,(a,b)->a.name.compareToIgnoreCase(b.name));
        return appCache;
    }
    private boolean suggested(String pkg) {
        String p=pkg.toLowerCase(Locale.US);
        return p.contains("instagram")||p.contains("youtube")||p.contains("snapchat")||p.contains("facebook")
            ||p.contains("reddit")||p.contains("twitter")||p.contains("tiktok")||p.contains("whatsapp")
            ||p.contains("pinterest")||p.contains("linkedin")||p.contains("discord")||p.contains("telegram")
            ||p.contains("netflix")||p.contains("primevideo")||p.contains("threads");
    }
    private void renderAppList(LinearLayout list,String query) {
        list.removeAllViews();
        String q=query.trim().toLowerCase(Locale.ROOT);
        Map<String,Integer> limits=LimitStore.all(this);
        List<AppItem> matches=new ArrayList<>();
        for(AppItem app:installedApps()) {
            if(!q.isEmpty() && !(app.name.toLowerCase(Locale.ROOT).contains(q)||app.pkg.toLowerCase(Locale.ROOT).contains(q))) continue;
            if(q.isEmpty() && !showAllApps && !suggested(app.pkg) && !limits.containsKey(app.pkg)) continue;
            matches.add(app);
        }
        matches.sort((a,b)-> {
            int aa=limits.containsKey(a.pkg)?0:1,bb=limits.containsKey(b.pkg)?0:1;
            if(aa!=bb)return aa-bb;
            return a.name.compareToIgnoreCase(b.name);
        });
        if(matches.isEmpty()) para(list,"Nothing here yet. Search for an app, or show all installed apps.");
        for(AppItem item:matches) appCard(list,item);
        if(q.isEmpty()) addButton(list,showAllApps?"Show only suggested + limited apps":"Show all installed apps",false,()->{
            showAllApps=!showAllApps;renderAppList(list,"");
        });
    }
    private void appCard(LinearLayout dest,AppItem app) {
        int value=LimitStore.get(this,app.pkg);
        LinearLayout card=Ui.card(this,dest,Color.WHITE);
        LinearLayout r=Ui.row(this);
        ImageView icon=new ImageView(this);
        try{icon.setImageDrawable(getPackageManager().getApplicationIcon(app.pkg));}catch(PackageManager.NameNotFoundException e){}
        r.addView(icon,new LinearLayout.LayoutParams(dp(34),dp(34)));
        LinearLayout names=Ui.column(this,0);names.setPadding(dp(11),0,0,0);
        names.addView(Ui.text(this,app.name,16,Ui.INK,true));
        names.addView(Ui.text(this,value>0?"Daily limit: "+value+" min":"No limit set",12,Ui.MUTED,false));
        r.addView(names,new LinearLayout.LayoutParams(0,-2,1));card.addView(r);
        if(value>0 && Permissions.usage(this)) {
            long minutes=UsageClock.usedMillis(this,app.pkg)/60000;
            small(card,minutes+" min used today"+(minutes>=value?" • limit reached":" • "+(value-minutes)+" min left"));
            ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
            progress.setMax(value);progress.setProgress((int)Math.min(value,minutes));
            progress.setProgressTintList(android.content.res.ColorStateList.valueOf(minutes>=value?Color.rgb(216,91,83):Ui.TEAL));
            LinearLayout.LayoutParams bar=new LinearLayout.LayoutParams(-1,dp(7));bar.setMargins(0,dp(2),0,dp(8));card.addView(progress,bar);
        }
        LinearLayout edit=Ui.row(this);
        EditText input=new EditText(this);input.setSingleLine(true);input.setTextSize(15);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);input.setHint("min / day (0 removes)");
        input.setSelectAllOnFocus(true);
        if(value>0)input.setText(String.valueOf(value));
        edit.addView(input,new LinearLayout.LayoutParams(0,dp(48),1));
        Button save=Ui.button(this,"Save",true);
        LinearLayout.LayoutParams btnParams=new LinearLayout.LayoutParams(dp(87),dp(45));btnParams.setMargins(dp(8),0,0,0);edit.addView(save,btnParams);
        card.addView(edit);
        Integer pending=LimitStore.pending(this,app.pkg);
        if(pending!=null) small(card,"Scheduled for next day: "+(pending==0?"remove limit":pending+" min"));
        save.setOnClickListener(v -> {
            try {
                String raw=input.getText().toString().trim();
                int n=raw.isEmpty()?0:Integer.parseInt(raw);
                if(n<0 || n>1440)throw new NumberFormatException();
                boolean now=LimitStore.set(this,app.pkg,n);
                toast(now?"Limit saved":"Change scheduled for tomorrow");
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);
                draw();
            }catch(NumberFormatException ex){toast("Enter a number from 0 to 1440.");}
        });
    }
    private String appName(String pkg) {
        try{return String.valueOf(getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)));}
        catch(PackageManager.NameNotFoundException e){return pkg;}
    }
    private android.content.SharedPreferences timerPrefs() {return getSharedPreferences(TIMER_PREF,MODE_PRIVATE);}
    private void startTimer(int mins) {
        long deadline=SystemClock.elapsedRealtime()+mins*60_000L;
        timerPrefs().edit().putLong("deadline",deadline).putInt("minutes",mins).apply();
    }
    private void endTimer() {timerPrefs().edit().remove("deadline").remove("minutes").apply();}
    private long remainingMs() {
        long until=timerPrefs().getLong("deadline",0L);
        // elapsedRealtime reset after reboot: discard an implausible timer.
        if(until>SystemClock.elapsedRealtime()+60*60*1000L){endTimer();return 0;}
        return until==0?0:Math.max(0,until-SystemClock.elapsedRealtime());
    }
    private void stopTicker() {if(timerTicker!=null)handler.removeCallbacks(timerTicker);timerTicker=null;timerReadout=null;}
    private void focus() {
        title(page,"One thing at a time");
        para(page,"A focus timer for realistic, phone-free work. This timer updates while the app is open; it does not send background alarms.");
        LinearLayout card=Ui.card(this,page,Color.WHITE);
        card.addView(Ui.spaceText(this,"FOCUS SESSION",13,Ui.TEAL,true,0));
        timerReadout=Ui.text(this,"15:00",43,Ui.INK,true);timerReadout.setGravity(Gravity.CENTER);
        timerReadout.setPadding(0,dp(12),0,dp(14));card.addView(timerReadout);
        long remaining=remainingMs();
        if(remaining>0) {
            para(card,"Put your phone aside and work on one tiny, clear task.");
            addButton(card,"Stop focus session",false,()->{endTimer();stopTicker();draw();});
            startTicker();
        } else {
            para(card,"Pick a duration that is achievable today.");
            LinearLayout choices=Ui.row(this);
            for(int minutes:new int[]{5,15,25,45}) {
                Button b=Ui.button(this,minutes+"m",minutes==15);b.setTextSize(13);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(45),1);lp.setMargins(dp(2),dp(5),dp(2),0);
                choices.addView(b,lp);b.setOnClickListener(v->{startTimer(minutes);draw();});
            }
            card.addView(choices);
        }
        LinearLayout tips=Ui.card(this,page,Ui.PALE);
        tips.addView(Ui.spaceText(this,"Prepare your environment",17,Ui.INK,true,0));
        para(tips,"1. Put your phone face-down and away from reach.\n2. Decide what 'done' looks like for this session.\n3. Open only the tools you need.\n4. When distracted, write it down and return to your task.");
        section(page,"Your if-then plan");
        para(page,DailyStore.ifThen(this));
        addButton(page,"Edit my plan",false,()->switchTo(4));
    }
    private void startTicker() {
        timerTicker=new Runnable() {
            @Override public void run() {
                if(timerReadout==null)return;
                long remain=remainingMs();
                if(remain<=0) {
                    endTimer();DailyStore.completedFocus(MainActivity.this);
                    toast("You finished a focus session. Nice work!");
                    draw();return;
                }
                long s=(remain+999)/1000;
                timerReadout.setText(String.format(Locale.US,"%02d:%02d",s/60,s%60));
                handler.postDelayed(this,1000);
            }
        };
        handler.post(timerTicker);
    }
    private void routine() {
        title(page,"A gentler daily routine");
        para(page,"Build consistency through small actions. Tick only what you actually do. No shame, no broken streaks.");
        LinearLayout summary=Ui.card(this,page,Ui.PALE);
        summary.addView(Ui.spaceText(this,DailyStore.routineCount(this)+" of "+FocusContent.ROUTINE_NAMES.length+" actions today",19,Ui.TEAL,true,0));
        para(summary,"Even one completed action is progress. These are optional suggestions, not medical treatment.");
        for(int i=0;i<FocusContent.ROUTINE_NAMES.length;i++) {
            final int id=i;
            LinearLayout task=Ui.card(this,page,Color.WHITE);
            CheckBox check=new CheckBox(this);
            check.setText(FocusContent.ROUTINE_NAMES[i]);check.setTextSize(15);check.setTextColor(Ui.INK);
            check.setChecked(DailyStore.done(this,i));task.addView(check);
            para(task,FocusContent.ROUTINE_HINTS[i]);
            check.setOnCheckedChangeListener((v,on)->{DailyStore.done(this,id,on);draw();});
        }
        section(page,"A realistic morning / evening");
        LinearLayout morning=Ui.card(this,page,Color.WHITE);
        morning.addView(Ui.spaceText(this,"Morning (5–15 minutes)",16,Ui.INK,true,0));
        para(morning,"Get some light • drink water • write today's single priority • begin with a 2-minute action.");
        LinearLayout evening=Ui.card(this,page,Color.WHITE);
        evening.addView(Ui.spaceText(this,"Evening (10 minutes)",16,Ui.INK,true,0));
        para(evening,"Put the phone away • write down unfinished thoughts • prepare tomorrow's first step • keep a steady bedtime.");
    }
    private EditText edit(String hint,String value,int minLines) {
        EditText t=new EditText(this);t.setHint(hint);t.setText(value);t.setMinLines(minLines);t.setTextSize(15);
        t.setPadding(dp(12),dp(12),dp(12),dp(12));t.setBackground(Ui.shape(Color.WHITE,13,this));
        return t;
    }
    private void toolkit() {
        title(page,"Your focus toolkit");
        para(page,"Realistic encouragement, not magic cures. Find a replacement habit when you feel the urge to scroll.");
        LinearLayout replacement=Ui.card(this,page,Ui.PALE);
        String[] idea=FocusContent.IDEAS[ideaIndex%FocusContent.IDEAS.length];
        replacement.addView(Ui.spaceText(this,"DO THIS INSTEAD • "+idea[0],12,Ui.TEAL,true,0));
        replacement.addView(Ui.spaceText(this,idea[1],20,Ui.INK,true,5));
        para(replacement,idea[2]);
        addButton(replacement,"I did this — log a small win",true,()->{DailyStore.completedAlternative(this);ideaIndex++;draw();});
        addButton(replacement,"Another idea",false,()->{ideaIndex++;draw();});
        small(replacement,"Today's alternatives completed: "+DailyStore.alternatives(this));
        section(page,"Focus tips and useful strategies");
        LinearLayout tip=Ui.card(this,page,Color.WHITE);
        String[] t=FocusContent.TIPS[tipIndex%FocusContent.TIPS.length];
        tip.addView(Ui.spaceText(this,t[0],19,Ui.INK,true,0));para(tip,t[1]);small(tip,t[2]);
        addButton(tip,"Next tip",false,()->{tipIndex++;draw();});
        addButton(tip,"Read supporting source",false,()->openUrl(FocusContent.sourceForTip(tipIndex%FocusContent.TIPS.length)));
        section(page,"Clear the fog: a gentle reset");
        LinearLayout fog=Ui.card(this,page,Color.WHITE);
        para(fog,"1. Pause, look away from the screen.\n2. Ask whether you slept and ate enough.\n3. Move for 5–10 minutes if comfortable.\n4. Write one tiny next task.\n5. Try a short focus session and take a break.\n6. Persistent or worsening cognitive fog deserves medical evaluation.");
        section(page,"Manifest intentions as actions");
        para(page,"Visualization can be a reflection tool, not a guarantee. Write what you want to build, then one thing you will actually do.");
        LinearLayout pledge=Ui.card(this,page,Ui.PALE);
        pledge.addView(Ui.spaceText(this,"I want more time for...",15,Ui.INK,true,0));
        EditText intention=edit("e.g. Learning coding and being present with family",DailyStore.intention(this),2);
        pledge.addView(intention,new LinearLayout.LayoutParams(-1,-2));
        addButton(pledge,"Save my intention",true,()->{DailyStore.intention(this,intention.getText().toString().trim());toast("Your intention is saved locally.");});
        section(page,"Make an if-then plan");
        para(page,"Link your scrolling trigger to a practical response. Example: If I open Instagram without a purpose, then I will take a 2-minute walk.");
        LinearLayout plan=Ui.card(this,page,Color.WHITE);
        EditText ifThen=edit("If I ___, then I will ___",DailyStore.ifThen(this),3);plan.addView(ifThen,new LinearLayout.LayoutParams(-1,-2));
        addButton(plan,"Save my plan",true,()->{DailyStore.ifThen(this,ifThen.getText().toString().trim());toast("Plan saved locally.");});
        section(page,"Quick brain dump");
        para(page,"Write down what is taking up mental space, then choose only the next useful step.");
        LinearLayout journal=Ui.card(this,page,Color.WHITE);
        EditText notes=edit("What is on your mind today?",DailyStore.journal(this),4);
        journal.addView(notes,new LinearLayout.LayoutParams(-1,-2));
        addButton(journal,"Save today's notes",true,()->{DailyStore.journal(this,notes.getText().toString());toast("Saved on this device.");});
        section(page,"ADHD and brain fog: important");
        para(page,"ADHD has no established cure. Medication, therapy and tailored supports may help. These habits are supportive, not a diagnosis or treatment. If persistent attention problems or brain fog affect your life, speak with a qualified clinician. Sudden severe confusion needs urgent medical assessment.");
        addButton(page,"Sources, security and limits",false,this::showInfo);
    }
    private void openUrl(String url) {
        try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
        catch(ActivityNotFoundException ex){toast("No web browser available.");}
    }
    private void showInfo() {
        new AlertDialog.Builder(this).setTitle("Privacy • health • limitations")
            .setMessage("Privacy: no internet permission, ad SDK, analytics, contacts, message-reading or cloud account. Limits, routine checkmarks and your personal notes are stored only on this device. Android's Usage Access can reveal which apps you use; accessibility monitors app/window changes without reading screen content.\n\nLimitations: Blocking requires both permissions and is BEST-EFFORT. Android may delay events, kill services, allow multi-window bypasses, or permit uninstall/permission revocation. Limits reset at local midnight and phone clock changes can affect enforcement. Do not use as a parental-control security boundary.\n\nHealth: ADHD cannot be cured by an app. Persistent brain fog or concentration problems may have medical causes. Focus tools are educational support, not diagnosis or treatment.\n\nSources: NIH/NIMH ADHD; NICE NG87; NHS sleep/fatigue; WHO physical activity; implementation-intention review (2024). Full links in the project SOURCES.md.")
            .setPositiveButton("Understood",null)
            .setNeutralButton("NIH ADHD guide",(d,w)->openUrl("https://health.nih.gov/health-topics-a-z/adhd"))
            .show();
    }
}
