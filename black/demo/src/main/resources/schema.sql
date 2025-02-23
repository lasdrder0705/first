CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    nickname VARCHAR(255),
    avatar VARCHAR(255),
    role VARCHAR(50),
    phone VARCHAR(255),
    permissions TEXT,
    created_at TIMESTAMP,
    bio TEXT
);

CREATE TABLE IF NOT EXISTS owners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(255),
    building_id BIGINT,
    door_number VARCHAR(255),
    parking_space VARCHAR(255),
    vehicles TEXT
);

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(255) NOT NULL UNIQUE,
    owner_name VARCHAR(255),
    owner_type VARCHAR(50),
    vehicle_type VARCHAR(50),
    contact_number VARCHAR(255),
    parking_space VARCHAR(255),
    expiry_date DATE,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS buildings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    image VARCHAR(255),
    floors INT,
    occupancy INT,
    total_units INT,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS parking_spaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    space_number VARCHAR(255) NOT NULL UNIQUE,
    location VARCHAR(255),
    type VARCHAR(50),
    status VARCHAR(50) DEFAULT '空闲',
    price DECIMAL(10,2),
    size VARCHAR(50),
    description TEXT,
    area DECIMAL(10,2),
    building VARCHAR(255),
    allowed_vehicle_type VARCHAR(50),
    charging_pile VARCHAR(50),
    owner_name VARCHAR(255),
    contact_number VARCHAR(255),
    rental_start DATE,
    rental_end DATE,
    building_id BIGINT,
    CONSTRAINT fk_parking_building FOREIGN KEY (building_id) REFERENCES buildings(id)
);

CREATE TABLE IF NOT EXISTS visitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    id_card VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    visit_type VARCHAR(50) NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    visit_time TIMESTAMP NOT NULL,
    expected_leave_time TIMESTAMP,
    actual_leave_time TIMESTAMP,
    owner_id BIGINT,
    relation_with_owner VARCHAR(255),
    remarks TEXT,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS maintenance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    urgency VARCHAR(50) NOT NULL,
    building_id BIGINT NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_urls TEXT,
    created_by BIGINT,
    created_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    result TEXT,
    handled_by BIGINT,
    handled_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS patrol_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    time_slot VARCHAR(50) NOT NULL,
    area VARCHAR(255) NOT NULL,
    focus TEXT,
    status VARCHAR(50) NOT NULL,
    assigned_to BIGINT,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS patrol_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    patroller_id BIGINT NOT NULL,
    patroller_name VARCHAR(255) NOT NULL,
    time_slot VARCHAR(255) NOT NULL,
    area VARCHAR(255) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    route TEXT NOT NULL,
    has_abnormal BOOLEAN DEFAULT FALSE,
    abnormal_desc TEXT,
    image_urls TEXT
);

ALTER TABLE patrol_logs MODIFY COLUMN start_time TIMESTAMP NULL;
ALTER TABLE patrol_logs MODIFY COLUMN end_time TIMESTAMP NULL;
ALTER TABLE patrol_logs MODIFY COLUMN has_abnormal BOOLEAN DEFAULT FALSE;
ALTER TABLE patrol_logs ALTER COLUMN has_abnormal SET DEFAULT FALSE;

-- 添加清洁任务表
CREATE TABLE IF NOT EXISTS cleaning_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    time_slot VARCHAR(50) NOT NULL,
    area VARCHAR(255) NOT NULL,
    focus TEXT,
    status VARCHAR(50) NOT NULL,
    assigned_to BIGINT,
    created_at TIMESTAMP
);

-- 添加清洁记录表
CREATE TABLE IF NOT EXISTS cleaning_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    cleaner_id BIGINT NOT NULL,
    cleaner_name VARCHAR(255) NOT NULL,
    time_slot VARCHAR(255) NOT NULL,
    area VARCHAR(255) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    content TEXT NOT NULL,
    hygiene_condition VARCHAR(50) NOT NULL,
    condition_desc TEXT,
    has_damage BOOLEAN DEFAULT FALSE,
    damage_desc TEXT,
    image_urls TEXT,
    status VARCHAR(50) DEFAULT '待处理',
    repair_result TEXT,
    repair_time TIMESTAMP
); 