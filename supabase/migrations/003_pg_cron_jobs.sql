-- ============================================================
-- SCHEDULED JOBS (pg_cron)
-- Requires pg_cron and pg_net extensions
-- ============================================================

-- Weekly digest: Every Sunday at 7 PM UTC
SELECT cron.schedule(
    'weekly-digest',
    '0 19 * * 0',
    $$
    SELECT net.http_post(
        url := current_setting('app.supabase_url') || '/functions/v1/send-weekly-digest',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'Authorization', 'Bearer ' || current_setting('app.service_role_key')
        ),
        body := '{}'::jsonb
    )
    $$
);

-- Daily nudge: Every day at 6 AM UTC
SELECT cron.schedule(
    'daily-nudge',
    '0 6 * * *',
    $$
    SELECT net.http_post(
        url := current_setting('app.supabase_url') || '/functions/v1/send-daily-nudge',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'Authorization', 'Bearer ' || current_setting('app.service_role_key')
        ),
        body := '{}'::jsonb
    )
    $$
);
