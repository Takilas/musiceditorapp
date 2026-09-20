-- =====================================================================
-- DML.sql — Наповнення БД тестовими даними (SQLite)
-- Порядок вставки враховує FK-залежності.
-- =====================================================================

PRAGMA foreign_keys = ON;

INSERT INTO artists (name, country) VALUES
    ('Океан Ельзи', 'Україна'),
    ('The Beatles', 'United Kingdom'),
    ('Daft Punk', 'France'),
    ('KALUSH', 'Україна');

INSERT INTO genres (name) VALUES
    ('Rock'), ('Pop'), ('Electronic'), ('Hip-Hop'), ('Folk');

INSERT INTO users (username, email, password_hash, role) VALUES
    ('admin',  'admin@example.com',  '$2a$hash_admin',  'ADMIN'),
    ('taras',  'taras@example.com',  '$2a$hash_taras',  'USER'),
    ('olena',  'olena@example.com',  '$2a$hash_olena',  'USER');

INSERT INTO registration_verifications (email, code, expires_date, is_used) VALUES
    ('newuser@example.com', '482913', datetime('now', '+10 minutes'), 0),
    ('taras@example.com',   '119284', datetime('now', '-1 day'),      1);

INSERT INTO albums (title, release_year, artist_id) VALUES
    ('Модель',            2001, 1),
    ('Abbey Road',        1969, 2),
    ('Discovery',         2001, 3),
    ('Stefania (Single)', 2022, 4);

INSERT INTO tracks (title, duration_seconds, file_path, album_id) VALUES
    ('Не питай',           245, '/music/oe_ne_pytay.mp3',      1),
    ('Веснянка',           198, '/music/oe_vesnyanka.mp3',     1),
    ('Come Together',      259, '/music/beatles_ct.mp3',       2),
    ('Here Comes the Sun', 185, '/music/beatles_hcs.mp3',      2),
    ('One More Time',      320, '/music/dp_omt.mp3',           3),
    ('Harder Better Faster Stronger', 224, '/music/dp_hbfs.mp3', 3),
    ('Stefania',           195, '/music/kalush_stefania.mp3',  4),
    ('Незнайомий трек',    210, '/music/unknown_track.mp3',    NULL);

INSERT INTO tracks (title, duration_seconds, file_path, album_id,
                     source_track_id, edit_type, edit_params, edited_by, edited_date)
VALUES
    ('Не питай (обрізано)', 60, '/music/edited/oe_ne_pytay_trim.mp3', 1,
     1, 'TRIM', 'start=00:00:30,end=00:01:30', 2, CURRENT_TIMESTAMP);

INSERT INTO tracks (title, duration_seconds, file_path, album_id,
                     source_track_id, edit_type, edit_params, edited_by, edited_date)
VALUES
    ('Stefania (голосніше)', 195, '/music/edited/kalush_stefania_vol.mp3', 4,
     7, 'VOLUME', 'gain=1.8', 3, CURRENT_TIMESTAMP);

INSERT INTO playlists (name, user_id) VALUES
    ('Улюблене',        2),
    ('Для тренування',  2),
    ('Вечірній чіл',     3);

INSERT INTO playlist_tracks (playlist_id, track_id, position) VALUES
    (1, 1, 1), (1, 3, 2), (1, 7, 3),
    (2, 5, 1), (2, 6, 2),
    (3, 2, 1), (3, 4, 2), (3, 8, 3);

INSERT INTO track_genres (track_id, genre_id) VALUES
    (1, 1), (1, 5), (2, 5), (3, 1), (4, 1), (5, 3), (6, 3), (7, 4), (7, 2);

INSERT INTO favorites (user_id, track_id) VALUES
    (2, 1), (2, 7), (3, 4), (3, 5);