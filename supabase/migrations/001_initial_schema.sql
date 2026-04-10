-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- USERS TABLE (extends auth.users)
-- ============================================================
CREATE TABLE public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    display_name TEXT,
    avatar_url TEXT,
    preferred_categories TEXT[] DEFAULT '{}',
    daily_target INTEGER DEFAULT 1 CHECK (daily_target BETWEEN 1 AND 20),
    reminder_time TIME DEFAULT '08:00:00',
    week_start_day INTEGER DEFAULT 1 CHECK (week_start_day IN (0, 1)), -- 0=Sunday, 1=Monday
    gemini_model TEXT DEFAULT 'gemini-2.5-pro',
    doom_scroll_model TEXT DEFAULT 'gemini-2.5-flash',
    images_enabled BOOLEAN DEFAULT true,
    video_enabled BOOLEAN DEFAULT true,
    email_weekly_digest BOOLEAN DEFAULT true,
    email_daily_nudge BOOLEAN DEFAULT false,
    notif_lesson_ready BOOLEAN DEFAULT true,
    notif_streak_risk BOOLEAN DEFAULT true,
    notif_weekly_review BOOLEAN DEFAULT true,
    notif_keep_learning BOOLEAN DEFAULT false,
    timezone TEXT DEFAULT 'UTC',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Auto-create user record on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, email, display_name, avatar_url)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email),
        NEW.raw_user_meta_data->>'avatar_url'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================
-- LESSONS TABLE
-- ============================================================
CREATE TABLE public.lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    topic TEXT NOT NULL,
    category TEXT NOT NULL,
    emoji TEXT DEFAULT '📚',
    content_json JSONB NOT NULL DEFAULT '[]'::jsonb,  -- LessonBlock[] array
    quiz_json JSONB DEFAULT '[]'::jsonb,               -- QuizQuestion[] array
    references_json JSONB DEFAULT '[]'::jsonb,         -- Reference[] array
    youtube_video_id TEXT,
    youtube_title TEXT,
    youtube_channel TEXT,
    youtube_duration_s INTEGER,
    video_watched BOOLEAN DEFAULT false,
    is_bonus BOOLEAN DEFAULT false,
    daily_position INTEGER DEFAULT 1,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMPTZ,
    estimated_read_minutes INTEGER DEFAULT 7,
    generated_by TEXT DEFAULT 'gemini-2.5-pro',
    share_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Prevent duplicate lessons per user per date per position
CREATE UNIQUE INDEX lessons_user_date_position_idx
    ON public.lessons(user_id, date, daily_position)
    WHERE NOT is_bonus;

-- ============================================================
-- QUIZ RESULTS TABLE
-- ============================================================
CREATE TABLE public.quiz_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    lesson_id UUID REFERENCES public.lessons(id) ON DELETE CASCADE,
    quiz_type TEXT NOT NULL CHECK (quiz_type IN ('daily', 'weekly')),
    score INTEGER NOT NULL CHECK (score >= 0),
    total INTEGER NOT NULL CHECK (total > 0),
    answers_json JSONB NOT NULL DEFAULT '[]'::jsonb,  -- [{questionIndex, selectedIndex, correct}]
    attempt_number INTEGER DEFAULT 1,
    completed_at TIMESTAMPTZ DEFAULT now()
);

-- ============================================================
-- STREAKS TABLE
-- ============================================================
CREATE TABLE public.streaks (
    user_id UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
    chain_streak INTEGER DEFAULT 0 CHECK (chain_streak >= 0),
    best_streak INTEGER DEFAULT 0 CHECK (best_streak >= 0),
    last_completed_date DATE,
    weekly_streak INTEGER DEFAULT 0 CHECK (weekly_streak BETWEEN 0 AND 7),
    weekly_start_date DATE,
    week_days_done BOOLEAN[] DEFAULT ARRAY[false,false,false,false,false,false,false],
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Auto-create streak record when user is created
CREATE OR REPLACE FUNCTION public.handle_new_user_streak()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.streaks (user_id)
    VALUES (NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_user_created_streak
    AFTER INSERT ON public.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user_streak();

-- ============================================================
-- SHARE EVENTS TABLE
-- ============================================================
CREATE TABLE public.share_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    lesson_id UUID REFERENCES public.lessons(id) ON DELETE SET NULL,
    share_type TEXT NOT NULL CHECK (share_type IN ('lesson', 'quiz_score', 'streak', 'weekly_stats', 'takeaway')),
    shared_at TIMESTAMPTZ DEFAULT now()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX lessons_user_date_idx ON public.lessons(user_id, date DESC);
CREATE INDEX lessons_user_bonus_idx ON public.lessons(user_id, is_bonus);
CREATE INDEX quiz_results_user_idx ON public.quiz_results(user_id, completed_at DESC);
CREATE INDEX quiz_results_lesson_idx ON public.quiz_results(lesson_id);
CREATE INDEX share_events_user_idx ON public.share_events(user_id, shared_at DESC);
