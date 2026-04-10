-- ============================================================
-- ROW LEVEL SECURITY
-- ============================================================

-- users
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own record"
    ON public.users FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own record"
    ON public.users FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- lessons
ALTER TABLE public.lessons ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own lessons"
    ON public.lessons FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own lessons"
    ON public.lessons FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own lessons"
    ON public.lessons FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Service role can manage all lessons"
    ON public.lessons FOR ALL
    USING (auth.role() = 'service_role');

-- quiz_results
ALTER TABLE public.quiz_results ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own quiz results"
    ON public.quiz_results FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own quiz results"
    ON public.quiz_results FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Service role can manage all quiz results"
    ON public.quiz_results FOR ALL
    USING (auth.role() = 'service_role');

-- streaks
ALTER TABLE public.streaks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own streak"
    ON public.streaks FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can update own streak"
    ON public.streaks FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Service role can manage all streaks"
    ON public.streaks FOR ALL
    USING (auth.role() = 'service_role');

-- share_events
ALTER TABLE public.share_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own share events"
    ON public.share_events FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own share events"
    ON public.share_events FOR INSERT
    WITH CHECK (auth.uid() = user_id);
