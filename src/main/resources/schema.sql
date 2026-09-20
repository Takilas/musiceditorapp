-- =====================================================================
-- schema.sql — Схема БД проєкту "Музичний редактор"
-- СУБД: SQLite
-- =====================================================================

PRAGMA foreign_keys = ON;

-- =========================================================
-- artists — довідникова сутність (reference entity)
-- Нормальна форма: 3НФ (атомарні атрибути, немає транзитивних
-- залежностей: country залежить тільки від artist_id)
-- =========================================================
CREATE TABLE artists (
    artist_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name        VARCHAR(150) NOT NULL,
    country     VARCHAR(100)
);

-- =========================================================
-- genres — довідникова сутність (reference entity)
-- Нормальна форма: 3НФ
-- =========================================================
CREATE TABLE genres (
    genre_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    name        VARCHAR(80) NOT NULL UNIQUE
);

-- =========================================================
-- users — основна сутність (main/master entity)
-- Нормальна форма: 3НФ (email/username унікальні й атомарні,
-- role — атомарне значення, не потребує окремого довідника
-- при такій кількості ролей)
-- =========================================================
CREATE TABLE users (
    user_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    username            VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    role                VARCHAR(20)  NOT NULL DEFAULT 'USER',
    registration_date   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- registration_verifications — тимчасова/лог-таблиця (transient/log table)
-- Нормальна форма: 3НФ. email навмисно НЕ винесено як FK на
-- users.email — верифікація відбувається ДО створення акаунту
-- (в процесі реєстрації), тому цей зв'язок не може бути жорстким
-- =========================================================
CREATE TABLE registration_verifications (
    verification_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    email               VARCHAR(150) NOT NULL,
    code                VARCHAR(10)  NOT NULL,
    created_date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_date        TIMESTAMP    NOT NULL,
    is_used             INTEGER      NOT NULL DEFAULT 0
);

-- =========================================================
-- albums — основна сутність
-- Зв'язок: 1:N з artists (один виконавець - багато альбомів)
-- Нормальна форма: 3НФ
-- =========================================================
CREATE TABLE albums (
    album_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    title           VARCHAR(200) NOT NULL,
    release_year    INTEGER,
    artist_id       INTEGER NOT NULL,
    CONSTRAINT fk_albums_artist
        FOREIGN KEY (artist_id) REFERENCES artists(artist_id)
        ON DELETE CASCADE
);

-- =========================================================
-- tracks — основна сутність, з self-referencing зв'язком
-- Зв'язки: 1:N з albums; 1:N рекурсивний (source_track_id ->
-- tracks.track_id) — одна оригінальна версія має багато
-- відредагованих версій; N:1 з users (edited_by)
-- Нормальна форма: 3НФ+ (BCNF). Поле edit_params зберігає
-- параметри операції як рядок (наприклад "gain=1.8") замість
-- окремих колонок під кожен тип операції — свідоме спрощення
-- на етапі практики, структура рядка залежить від edit_type
-- =========================================================
CREATE TABLE tracks (
    track_id            INTEGER PRIMARY KEY AUTOINCREMENT,
    title               VARCHAR(200) NOT NULL,
    duration_seconds    INTEGER NOT NULL,
    file_path           VARCHAR(500),
    album_id            INTEGER,
    added_date          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source_track_id     INTEGER,
    edit_type           VARCHAR(20),
    edit_params         VARCHAR(255),
    edited_by           INTEGER,
    edited_date         TIMESTAMP,
    CONSTRAINT fk_tracks_album
        FOREIGN KEY (album_id) REFERENCES albums(album_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_tracks_source
        FOREIGN KEY (source_track_id) REFERENCES tracks(track_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_tracks_editor
        FOREIGN KEY (edited_by) REFERENCES users(user_id)
        ON DELETE SET NULL
);

-- =========================================================
-- playlists — основна сутність
-- Зв'язок: 1:N з users
-- Нормальна форма: 3НФ
-- =========================================================
CREATE TABLE playlists (
    playlist_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    name            VARCHAR(150) NOT NULL,
    user_id         INTEGER NOT NULL,
    created_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playlists_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- =========================================================
-- playlist_tracks — зв'язуюча таблиця (junction table)
-- Реалізує Багато-до-Багатьох: playlists <-> tracks,
-- з додатковим атрибутом position (порядок треку в плейлисті)
-- Нормальна форма: 3НФ
-- =========================================================
CREATE TABLE playlist_tracks (
    playlist_id     INTEGER NOT NULL,
    track_id        INTEGER NOT NULL,
    position        INTEGER NOT NULL,
    added_date      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_playlist_tracks PRIMARY KEY (playlist_id, track_id),
    CONSTRAINT fk_pt_playlist
        FOREIGN KEY (playlist_id) REFERENCES playlists(playlist_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pt_track
        FOREIGN KEY (track_id) REFERENCES tracks(track_id)
        ON DELETE CASCADE
);

-- =========================================================
-- track_genres — зв'язуюча таблиця (junction table)
-- Реалізує Багато-до-Багатьох: tracks <-> genres,
-- "чиста" зв'язуюча таблиця без додаткових атрибутів
-- Нормальна форма: 3НФ
-- =========================================================
CREATE TABLE track_genres (
    track_id    INTEGER NOT NULL,
    genre_id    INTEGER NOT NULL,
    CONSTRAINT pk_track_genres PRIMARY KEY (track_id, genre_id),
    CONSTRAINT fk_tg_track
        FOREIGN KEY (track_id) REFERENCES tracks(track_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tg_genre
        FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
        ON DELETE CASCADE
);

-- =========================================================
-- favorites — зв'язуюча таблиця (junction table)
-- Реалізує Багато-до-Багатьох: users <-> tracks,
-- з додатковим атрибутом added_date
-- Нормальна форма: 3НФ
-- =========================================================
CREATE TABLE favorites (
    user_id     INTEGER NOT NULL,
    track_id    INTEGER NOT NULL,
    added_date  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_favorites PRIMARY KEY (user_id, track_id),
    CONSTRAINT fk_fav_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_fav_track
        FOREIGN KEY (track_id) REFERENCES tracks(track_id)
        ON DELETE CASCADE
);
-- =========================================================
-- conversion_orders — бізнес-сутність: замовлення платної послуги
-- Зв'язки: N:1 з users, N:1 з tracks
-- Нормальна форма: 3НФ (price фіксується на момент замовлення,
-- а не береться "живим" з довідника — це свідоме рішення:
-- історична ціна замовлення не повинна змінюватись заднім числом,
-- навіть якщо тариф компанії зміниться в майбутньому)
-- =========================================================
CREATE TABLE conversion_orders (
    order_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL,
    track_id        INTEGER NOT NULL,
    target_format   VARCHAR(10) NOT NULL,
    price           DECIMAL(6,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    result_file_path VARCHAR(500),
    created_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_date  TIMESTAMP,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_orders_track
        FOREIGN KEY (track_id) REFERENCES tracks(track_id)
        ON DELETE CASCADE
);