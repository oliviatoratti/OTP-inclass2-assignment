CREATE DATABASE IF NOT EXISTS assignment6
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE assignment6;

-- Language-independent unit information
CREATE TABLE IF NOT EXISTS units (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     unit_key VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL
    ) CHARACTER SET utf8mb4;

-- Translations for each unit
CREATE TABLE IF NOT EXISTS unit_translation (
                                                unit_id INT NOT NULL,
                                                lang VARCHAR(5) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (unit_id, lang),
    FOREIGN KEY (unit_id) REFERENCES units(id)
    ) CHARACTER SET utf8mb4;

-- Add the selected language to saved conversions
ALTER TABLE conversions
    ADD COLUMN IF NOT EXISTS language VARCHAR(5) NOT NULL DEFAULT 'en';

-- Insert units
INSERT INTO units (unit_key, category) VALUES
                                           ('celsius', 'temperature'),
                                           ('fahrenheit', 'temperature'),
                                           ('kelvin', 'temperature'),
                                           ('kilometer', 'distance'),
                                           ('hour', 'time'),
                                           ('km_per_hour', 'speed')
    ON DUPLICATE KEY UPDATE category = VALUES(category);

-- English
INSERT INTO unit_translation VALUES
                                 (1,'en','Celsius','Temperature measured in degrees Celsius'),
                                 (2,'en','Fahrenheit','Temperature measured in degrees Fahrenheit'),
                                 (3,'en','Kelvin','Absolute temperature unit'),
                                 (4,'en','Kilometer','Distance of one thousand metres'),
                                 (5,'en','Hour','Time period of sixty minutes'),
                                 (6,'en','Kilometre per hour','Speed measured in kilometres per hour')
    ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- Finnish
INSERT INTO unit_translation VALUES
                                 (1,'fi','Celsius','Lämpötila Celsius-asteina'),
                                 (2,'fi','Fahrenheit','Lämpötila Fahrenheit-asteina'),
                                 (3,'fi','Kelvin','Absoluuttinen lämpötilayksikkö'),
                                 (4,'fi','Kilometri','Tuhannen metrin matka'),
                                 (5,'fi','Tunti','Kuudenkymmenen minuutin aika'),
                                 (6,'fi','Kilometriä tunnissa','Nopeus kilometreinä tunnissa')
    ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- Swedish
INSERT INTO unit_translation VALUES
                                 (1,'sv','Celsius','Temperatur i grader Celsius'),
                                 (2,'sv','Fahrenheit','Temperatur i grader Fahrenheit'),
                                 (3,'sv','Kelvin','Absolut temperaturenhet'),
                                 (4,'sv','Kilometer','En sträcka på tusen meter'),
                                 (5,'sv','Timme','En tidsperiod på sextio minuter'),
                                 (6,'sv','Kilometer per timme','Hastighet i kilometer per timme')
    ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- Japanese
INSERT INTO unit_translation VALUES
                                 (1,'ja','摂氏','摂氏で測定する温度'),
                                 (2,'ja','華氏','華氏で測定する温度'),
                                 (3,'ja','ケルビン','絶対温度の単位'),
                                 (4,'ja','キロメートル','千メートルの距離'),
                                 (5,'ja','時間','六十分の時間'),
                                 (6,'ja','時速キロメートル','時速で表す速度')
    ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- Arabic
INSERT INTO unit_translation VALUES
                                 (1,'ar','درجة مئوية','درجة الحرارة بمقياس سلسيوس'),
                                 (2,'ar','فهرنهايت','درجة الحرارة بمقياس فهرنهايت'),
                                 (3,'ar','كلفن','وحدة قياس درجة الحرارة المطلقة'),
                                 (4,'ar','كيلومتر','مسافة تساوي ألف متر'),
                                 (5,'ar','ساعة','فترة زمنية مدتها ستون دقيقة'),
                                 (6,'ar','كيلومتر في الساعة','السرعة بالكيلومترات في الساعة')
    ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);