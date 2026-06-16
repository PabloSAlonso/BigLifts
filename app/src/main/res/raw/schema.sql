-- =============================================
-- BigLifts - Supabase Database Schema
-- =============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- 1. Users Table (extends auth.users)
-- =============================================
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    full_name TEXT,
    avatar_url TEXT,
    weight_unit TEXT DEFAULT 'kg' CHECK (weight_unit IN ('kg', 'lbs')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Auto-create profile on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, username, full_name)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1)),
        COALESCE(NEW.raw_user_meta_data->>'full_name', '')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- =============================================
-- 2. Workout Templates (reusable workout plans)
-- =============================================
CREATE TABLE public.workout_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- 3. Exercises (exercise library)
-- =============================================
CREATE TABLE public.exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN (
        'chest', 'back', 'shoulders', 'biceps', 'triceps',
        'legs', 'glutes', 'core', 'cardio', 'other'
    )),
    muscle_group TEXT,
    equipment TEXT,
    is_custom BOOLEAN DEFAULT FALSE,
    user_id UUID REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(name, user_id)
);

-- =============================================
-- 4. Template Exercises (exercises in a template)
-- =============================================
CREATE TABLE public.template_exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    template_id UUID NOT NULL REFERENCES public.workout_templates(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES public.exercises(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL DEFAULT 0,
    target_sets INTEGER DEFAULT 3,
    target_reps INTEGER DEFAULT 10,
    target_weight DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- 5. Workout Sessions (actual workouts performed)
-- =============================================
CREATE TABLE public.workout_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    template_id UUID REFERENCES public.workout_templates(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    started_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    duration_minutes INTEGER,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- 6. Session Exercises (exercises in a session)
-- =============================================
CREATE TABLE public.session_exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES public.workout_sessions(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES public.exercises(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- 7. Sets (individual sets)
-- =============================================
CREATE TABLE public.sets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_exercise_id UUID NOT NULL REFERENCES public.session_exercises(id) ON DELETE CASCADE,
    set_number INTEGER NOT NULL,
    weight DECIMAL(10, 2),
    reps INTEGER,
    rpe DECIMAL(3, 1),
    is_warmup BOOLEAN DEFAULT FALSE,
    is_dropset BOOLEAN DEFAULT FALSE,
    rest_seconds INTEGER,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- Indexes for performance
-- =============================================
CREATE INDEX idx_profiles_user_id ON public.profiles(id);
CREATE INDEX idx_workout_templates_user_id ON public.workout_templates(user_id);
CREATE INDEX idx_workout_sessions_user_id ON public.workout_sessions(user_id);
CREATE INDEX idx_workout_sessions_started_at ON public.workout_sessions(started_at DESC);
CREATE INDEX idx_session_exercises_session_id ON public.session_exercises(session_id);
CREATE INDEX idx_sets_session_exercise_id ON public.sets(session_exercise_id);
CREATE INDEX idx_exercises_user_id ON public.exercises(user_id);

-- =============================================
-- Row Level Security (RLS) Policies
-- =============================================

-- Profiles
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Workout Templates
ALTER TABLE public.workout_templates ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own templates"
    ON public.workout_templates FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can create own templates"
    ON public.workout_templates FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own templates"
    ON public.workout_templates FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own templates"
    ON public.workout_templates FOR DELETE
    USING (auth.uid() = user_id);

-- Exercises
ALTER TABLE public.exercises ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view custom exercises"
    ON public.exercises FOR SELECT
    USING (user_id IS NULL OR auth.uid() = user_id);

CREATE POLICY "Users can create custom exercises"
    ON public.exercises FOR INSERT
    WITH CHECK (auth.uid() = user_id OR user_id IS NULL);

-- Template Exercises
ALTER TABLE public.template_exercises ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own template exercises"
    ON public.template_exercises FOR SELECT
    USING (
        template_id IN (
            SELECT id FROM public.workout_templates WHERE user_id = auth.uid()
        )
    );

CREATE POLICY "Users can manage own template exercises"
    ON public.template_exercises FOR ALL
    USING (
        template_id IN (
            SELECT id FROM public.workout_templates WHERE user_id = auth.uid()
        )
    );

-- Workout Sessions
ALTER TABLE public.workout_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own sessions"
    ON public.workout_sessions FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can create own sessions"
    ON public.workout_sessions FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own sessions"
    ON public.workout_sessions FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own sessions"
    ON public.workout_sessions FOR DELETE
    USING (auth.uid() = user_id);

-- Session Exercises
ALTER TABLE public.session_exercises ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own session exercises"
    ON public.session_exercises FOR SELECT
    USING (
        session_id IN (
            SELECT id FROM public.workout_sessions WHERE user_id = auth.uid()
        )
    );

CREATE POLICY "Users can manage own session exercises"
    ON public.session_exercises FOR ALL
    USING (
        session_id IN (
            SELECT id FROM public.workout_sessions WHERE user_id = auth.uid()
        )
    );

-- Sets
ALTER TABLE public.sets ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own sets"
    ON public.sets FOR SELECT
    USING (
        session_exercise_id IN (
            SELECT se.id FROM public.session_exercises se
            JOIN public.workout_sessions ws ON se.session_id = ws.id
            WHERE ws.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can manage own sets"
    ON public.sets FOR ALL
    USING (
        session_exercise_id IN (
            SELECT se.id FROM public.session_exercises se
            JOIN public.workout_sessions ws ON se.session_id = ws.id
            WHERE ws.user_id = auth.uid()
        )
    );

-- =============================================
-- Seed default exercises
-- =============================================
INSERT INTO public.exercises (name, category, muscle_group, equipment) VALUES
-- Chest
('Bench Press', 'chest', 'Chest', 'Barbell'),
('Incline Bench Press', 'chest', 'Upper Chest', 'Barbell'),
('Dumbbell Press', 'chest', 'Chest', 'Dumbbell'),
('Incline Dumbbell Press', 'chest', 'Upper Chest', 'Dumbbell'),
('Cable Fly', 'chest', 'Chest', 'Cable'),
('Push Up', 'chest', 'Chest', 'Bodyweight'),
('Dips (Chest)', 'chest', 'Chest', 'Bodyweight'),

-- Back
('Deadlift', 'back', 'Back', 'Barbell'),
('Barbell Row', 'back', 'Back', 'Barbell'),
('Pull Up', 'back', 'Lats', 'Bodyweight'),
('Lat Pulldown', 'back', 'Lats', 'Cable'),
('Cable Row', 'back', 'Back', 'Cable'),
('Dumbbell Row', 'back', 'Back', 'Dumbbell'),
('T-Bar Row', 'back', 'Back', 'Machine'),

-- Shoulders
('Overhead Press', 'shoulders', 'Shoulders', 'Barbell'),
('Dumbbell Shoulder Press', 'shoulders', 'Shoulders', 'Dumbbell'),
('Lateral Raise', 'shoulders', 'Side Delts', 'Dumbbell'),
('Front Raise', 'shoulders', 'Front Delts', 'Dumbbell'),
('Face Pull', 'shoulders', 'Rear Delts', 'Cable'),
('Rear Delt Fly', 'shoulders', 'Rear Delts', 'Dumbbell'),

-- Biceps
('Barbell Curl', 'biceps', 'Biceps', 'Barbell'),
('Dumbbell Curl', 'biceps', 'Biceps', 'Dumbbell'),
('Hammer Curl', 'biceps', 'Biceps', 'Dumbbell'),
('Preacher Curl', 'biceps', 'Biceps', 'Barbell'),
('Cable Curl', 'biceps', 'Biceps', 'Cable'),

-- Triceps
('Tricep Pushdown', 'triceps', 'Triceps', 'Cable'),
('Skull Crusher', 'triceps', 'Triceps', 'Barbell'),
('Overhead Tricep Extension', 'triceps', 'Triceps', 'Dumbbell'),
('Dips (Triceps)', 'triceps', 'Triceps', 'Bodyweight'),
('Close Grip Bench Press', 'triceps', 'Triceps', 'Barbell'),

-- Legs
('Barbell Squat', 'legs', 'Quads', 'Barbell'),
('Front Squat', 'legs', 'Quads', 'Barbell'),
('Leg Press', 'legs', 'Quads', 'Machine'),
('Leg Extension', 'legs', 'Quads', 'Machine'),
('Romanian Deadlift', 'legs', 'Hamstrings', 'Barbell'),
('Leg Curl', 'legs', 'Hamstrings', 'Machine'),
('Bulgarian Split Squat', 'legs', 'Quads', 'Dumbbell'),
('Walking Lunge', 'legs', 'Quads', 'Dumbbell'),
('Calf Raise', 'legs', 'Calves', 'Machine'),

-- Glutes
('Hip Thrust', 'glutes', 'Glutes', 'Barbell'),
('Glute Bridge', 'glutes', 'Glutes', 'Bodyweight'),
('Cable Kickback', 'glutes', 'Glutes', 'Cable'),

-- Core
('Plank', 'core', 'Core', 'Bodyweight'),
('Crunch', 'core', 'Abs', 'Bodyweight'),
('Hanging Leg Raise', 'core', 'Abs', 'Bodyweight'),
('Cable Crunch', 'core', 'Abs', 'Cable'),
('Russian Twist', 'core', 'Obliques', 'Bodyweight'),
('Ab Wheel Rollout', 'core', 'Core', 'Ab Wheel');