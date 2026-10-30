package com.focuslock.app;

/** Offline editorial content; see SOURCES.md for evidence and limitations. */
public final class FocusContent {
    private FocusContent() {}
    public static final String[] ROUTINE_NAMES = {
        "Get outside for a little daylight", "Drink water and eat a proper meal",
        "Choose ONE most important task", "Do a 10-minute movement break",
        "Do one phone-free focus block", "Write tomorrow's first step",
        "Make the final hour before bed calmer"
    };
    public static final String[] ROUTINE_HINTS = {
        "Morning: a short walk is an easy start.", "Regular meals help you avoid running on empty.",
        "Define a task you can finish, not a huge goal.", "Walk, stretch or dance; start small.",
        "A 10-minute block counts. Silence notifications.", "Make tomorrow's start easier.",
        "Dim lights, put the phone away, keep a regular bedtime."
    };
    public static final String[][] IDEAS = {
        {"2 min", "Reset your space", "Put away five things near you. Less visual clutter can make starting easier."},
        {"2 min", "Drink water", "Get up, drink some water and take a brief screen break."},
        {"2 min", "Name the urge", "Ask: Am I bored, anxious, tired or avoiding something? Choose one useful next action."},
        {"2 min", "Breathe slowly", "Try a few comfortable, unforced slow breaths. Stop if uncomfortable."},
        {"2 min", "Send a real message", "Check in with someone you value, then put the phone down."},
        {"5 min", "Take a short walk", "Walk around your building or room without your phone."},
        {"5 min", "Start embarrassingly small", "Open the document and write one sentence. Starting is the task."},
        {"5 min", "Brain dump", "Write all the things on your mind. Circle just one next step."},
        {"5 min", "Stretch and reset", "Move your neck, shoulders and legs gently. Break the sitting loop."},
        {"5 min", "Read two pages", "Pick a book you enjoy; short reading counts."},
        {"5 min", "Make tea mindfully", "Make a drink and enjoy it without any screen."},
        {"5 min", "Tidy one surface", "Clear only the desk or shelf in front of you."},
        {"15 min", "Do a single-task sprint", "Set a 15-minute timer; one task, no notifications."},
        {"15 min", "Go outside", "Take a short walk or sit somewhere with daylight."},
        {"15 min", "Practice a skill", "Draw, play an instrument, write code or review your notes."},
        {"15 min", "Call a friend", "Have a real conversation instead of watching updates."},
        {"15 min", "Prepare something healthy", "Make a snack or plan your next meal."},
        {"30 min", "Move your body", "Go for a brisk walk, dance or choose movement you enjoy."},
        {"30 min", "Learn something", "Work on one small lesson; write down what you learned."},
        {"30 min", "Build instead of browse", "Make something: a journal entry, recipe, drawing or project."}
    };
    public static final String[][] TIPS = {
        {"If-then plan", "If I reach for Instagram automatically, then I will place my phone down and do a two-minute reset.", "An if-then plan links a specific trigger to an action. Rehearse yours."},
        {"Start with two minutes", "Write the smallest visible next step: open your editor, write a title, or wash one dish.", "Making the task concrete can reduce initiation friction."},
        {"Externalize memory", "Put your reminders, appointments and next steps in one visible list.", "Useful for forgetfulness and overwhelm; not ADHD treatment by itself."},
        {"Design the environment", "Put your phone out of reach, turn off nonessential alerts and keep your materials ready.", "Make the helpful choice easier than the distracting one."},
        {"Take short breaks", "Try 15–25 minutes of focused effort, then a few minutes of rest. Adjust the interval.", "A productivity technique, not a proven ADHD cure."},
        {"Movement counts", "A walk or gentle activity may help you feel more alert. Start with what is manageable.", "Regular exercise supports health and cognitive well-being."},
        {"Protect sleep", "Choose a realistic bedtime and wake time. Try to reduce late-night scrolling.", "Adults commonly need 7–9 hours; persistent sleep problems need medical advice."},
        {"Compassionate restart", "A difficult morning does not erase the day. Choose the next useful 5 minutes.", "Consistency matters more than perfect streaks."},
        {"Define a value", "What kind of person do you want to be? Link that value to one action today.", "Reflection can help clarify motivation; manifestation doesn't guarantee outcomes."},
        {"Brain fog is a symptom", "Check sleep, stress, meals and workload; seek assessment if fog persists or worsens.", "Brain fog has multiple possible causes, not just screen time or ADHD."},
        {"If distraction is persistent", "Consider a qualified health professional if attention difficulties disrupt work or daily life.", "ADHD requires proper assessment; medications and therapy can help."}
    };
    public static String sourceForTip(int i) {
        if (i == 0) return "https://doi.org/10.1080/10463283.2024.2334563";
        if (i == 5) return "https://www.who.int/initiatives/behealthy/physical-activity";
        if (i == 6) return "https://www.nhs.uk/conditions/insomnia/";
        if (i == 9) return "https://www.nhs.uk/symptoms/tiredness-and-fatigue/";
        if (i == 10 || i == 2) return "https://health.nih.gov/health-topics-a-z/adhd";
        return "https://www.nimh.nih.gov/health/publications/attention-deficit-hyperactivity-disorder-what-you-need-to-know";
    }
}
