SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;
BEGIN;
DROP SCHEMA IF EXISTS saved4 CASCADE;
CREATE SCHEMA saved4;
SET search_path TO saved4;
CREATE TYPE user_type as ENUM ('admin', 'company', 'port');
CREATE TYPE order_status as ENUM(
	'created',
	'scheduled',
	'undergoing',
	'completed',
	'canceled'
);
CREATE TYPE operation_type as ENUM('loading', 'unloading');
CREATE TYPE freight_type as ENUM('machinery', 'food', 'chemical', 'textile', 'electronic', 'other');
CREATE TYPE change_status AS ENUM('edited', 'rescheduled', 'removed');
CREATE TYPE container_status as ENUM('available', 'in_use', 'broken');

CREATE TYPE vehicle_status as ENUM('available', 'in_use', 'out_of_order', 'deleted');

CREATE OR REPLACE FUNCTION enforce_user_password()
RETURNS TRIGGER AS $$
BEGIN
IF NEW.password IS NULL OR NEW.password = '' THEN
    RAISE EXCEPTION 'Password cannot be null or empty';
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE users(
    username VARCHAR (255) PRIMARY KEY,
    password VARCHAR (255),
    email TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at TIMESTAMPTZ,
    type user_type NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL
);
INSERT INTO users VALUES ('port', null, 'port@example.com', CURRENT_TIMESTAMP, null, 'port', TRUE);
CREATE TRIGGER user_password_insert_trigger BEFORE INSERT ON users FOR EACH ROW EXECUTE PROCEDURE enforce_user_password();
CREATE TRIGGER user_password_update_trigger BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE enforce_user_password();

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE POLICY company_selectUser_policy ON users FOR SELECT USING (username = current_setting('username'));
CREATE POLICY company_insertUser_policy ON users FOR INSERT WITH CHECK (username = current_setting('username'));
CREATE POLICY company_updateUser_policy ON users FOR UPDATE USING (username = current_setting('username'));
CREATE POLICY admin_selectUser_policy ON users FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_updateUser_policy ON users FOR UPDATE USING (current_setting('role') = 'admin' AND (type != 'admin'::user_type OR username = current_setting('username')));
CREATE POLICY system_insertUser_policy ON users FOR INSERT WITH CHECK (current_setting('role') = 'system');

CREATE TABLE admins(
    username VARCHAR (255) PRIMARY KEY,
    name VARCHAR (255) NOT NULL
);
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;
CREATE POLICY admin_selectAdmin_policy ON admins FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_updateAdmin_policy ON admins FOR UPDATE USING (current_setting('role') = 'admin' AND current_setting('username') = username);
CREATE POLICY system_insertAdmin_policy ON admins FOR INSERT WITH CHECK (current_setting('role') = 'system');

CREATE TABLE companies(
    username VARCHAR (255) PRIMARY KEY,
    name VARCHAR (255) UNIQUE NOT NULL,
    location POINT NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON UPDATE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username)
);
ALTER TABLE companies ENABLE ROW LEVEL SECURITY;
CREATE POLICY company_company_policy ON companies USING ( username = current_setting('username'));
CREATE POLICY admin_selectCompany_policy ON companies FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_updateCompany_policy ON companies FOR UPDATE USING (current_setting('role') = 'admin');
CREATE POLICY system_company_policy ON companies FOR INSERT WITH CHECK (current_setting('role') = 'system');

CREATE TABLE port(
                     username VARCHAR (255) PRIMARY KEY,
                     location POINT NOT NULL,
                     FOREIGN KEY (username) REFERENCES users(username) ON UPDATE CASCADE
);
ALTER TABLE port ENABLE ROW LEVEL SECURITY;
INSERT INTO port VALUES ('port', point(0, 0));
CREATE POLICY company_selectPort_policy ON port FOR SELECT USING (current_setting('role') = 'company');
CREATE POLICY admin_selectPort_policy ON port FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_updatePort_policy ON port FOR UPDATE USING (current_setting('role') = 'admin');

CREATE TABLE chats(
                      company VARCHAR (255) PRIMARY KEY,
                      unread INT DEFAULT 0 NOT NULL,
                      FOREIGN KEY (company) REFERENCES companies(username) ON UPDATE CASCADE
);
ALTER TABLE chats ENABLE ROW LEVEL SECURITY;
CREATE POLICY company_selectChat_policy ON chats FOR SELECT USING (current_setting('role') = 'company' AND company = current_setting('username'));
CREATE POLICY admin_selectChat_policy ON chats FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY system_insertChat_policy ON chats FOR INSERT WITH CHECK (current_setting('role') = 'system');
CREATE POLICY system_updateChat_policy ON chats FOR UPDATE USING (current_setting('role') = 'system');

CREATE TABLE messages(
                         message_id SERIAL PRIMARY KEY,
                         company VARCHAR (255) NOT NULL,
                         time TIMESTAMPTZ NOT NULL,
                         sender VARCHAR (255) NOT NULL,
                         body VARCHAR (2000) NOT NULL,
                         FOREIGN KEY (company) REFERENCES chats(company) ON UPDATE CASCADE,
                         FOREIGN KEY (sender) REFERENCES users(username) ON UPDATE CASCADE
);
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
CREATE POLICY company_selectMessage_policy ON messages FOR SELECT USING (current_setting('role') = 'company' AND company = current_setting('username'));
CREATE POLICY company_insertMessage_policy ON messages FOR INSERT WITH CHECK (current_setting('role') = 'company' AND company = current_setting('username') AND sender = current_setting('username'));
CREATE POLICY admin_selectMessage_policy ON messages FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_insertMessage_policy ON messages FOR INSERT WITH CHECK (current_setting('role') = 'admin');

CREATE TABLE containers(
                           container_id SERIAL PRIMARY KEY,
                           max_weight DECIMAL(10, 2) NOT NULL,
                           container_status container_status DEFAULT 'available'::container_status not null,
                           is_active BOOLEAN DEFAULT TRUE NOT NULL
);
ALTER TABLE containers ENABLE ROW LEVEL SECURITY;
CREATE POLICY user_selectContainer_policy ON containers FOR SELECT USING (current_setting('role') IN ('admin', 'company'));
CREATE POLICY admin_insertContainer_policy ON containers FOR INSERT WITH CHECK (current_setting('role') = 'admin');
CREATE POLICY admin_updateContainer_policy ON containers FOR UPDATE USING (current_setting('role') = 'admin');
CREATE POLICY system_container_policy ON containers FOR SELECT USING (current_setting('role') = 'system');

CREATE TABLE vehicles (
                          vehicle_id SERIAL PRIMARY KEY,
                          position POINT NOT NULL,
                          battery_level DECIMAL(5, 2) NOT NULL,
                          status vehicle_status DEFAULT 'available'::vehicle_status NOT NULL,
                          distance DECIMAL(5, 2) NOT NULL
);
ALTER TABLE vehicles ENABLE ROW LEVEL SECURITY;
CREATE POLICY admin_selectVehicle_policy ON vehicles FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_insertVehicle_policy ON vehicles FOR INSERT WITH CHECK (current_setting('role') = 'admin');
CREATE POLICY admin_updateVehicle_policy ON vehicles FOR UPDATE USING (current_setting('role') = 'admin');
CREATE POLICY system_selectVehicle_policy ON vehicles FOR SELECT USING (current_setting('role') = 'system');
CREATE POLICY system_updateVehicle_policy ON vehicles FOR UPDATE USING (current_setting('role') = 'system');

CREATE TABLE routes(
	route_id SERIAL PRIMARY KEY,
	route PATH NOT NULL
);
CREATE TABLE orders(
	order_id SERIAL PRIMARY KEY,
	container_id INT REFERENCES containers(container_id),
	vehicle_id INT REFERENCES vehicles(vehicle_id),
	requester VARCHAR(255) NOT NULL REFERENCES users(username) ON UPDATE CASCADE,
	order_for VARCHAR(255) NOT NULL REFERENCES users(username) ON UPDATE CASCADE,
	route_id INT REFERENCES routes(route_id),
	made_at TIMESTAMPTZ NOT NULL,
	source VARCHAR(255) NOT NULL REFERENCES users(username) ON UPDATE CASCADE,
	destination VARCHAR(255) NOT NULL REFERENCES users(username) ON UPDATE CASCADE,
	preferred_shared BOOLEAN NOT NULL,
	is_shared BOOLEAN NOT NULL,
	tw_start TIMESTAMPTZ NOT NULL,
	tw_end TIMESTAMPTZ NOT NULL,
	eta TIMESTAMPTZ,
	status order_status DEFAULT 'created'::order_status NOT NULL,
	operation_type operation_type NOT NULL,
	freight_value DECIMAL(12, 2),
	freight_type freight_type,	
	freight_weight DECIMAL(6,2),
	departure_time TIMESTAMPTZ);

ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
CREATE POLICY company_selectOrder_policy ON orders FOR SELECT USING (current_setting('role') = 'company' AND (current_setting('username') = source OR current_setting('username') =  destination));
CREATE POLICY company_insertOrder_policy ON orders FOR INSERT WITH CHECK (current_setting('role') = 'company' AND requester = current_setting('username') AND (current_setting('username') = source OR current_setting('username') =  destination));
CREATE POLICY admin_selectOrder_policy ON orders FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_insertOrder_policy ON orders FOR INSERT WITH CHECK (current_setting('role') = 'admin');
CREATE POLICY admin_updateOrder_policy ON orders FOR UPDATE USING (current_setting('role') = 'admin');
CREATE POLICY system_selectOrder_policy ON orders FOR SELECT USING (current_setting('role') = 'system');
CREATE POLICY system_updateOrder_policy ON orders FOR UPDATE USING (current_setting('role') = 'system');

CREATE TABLE order_history (
	change_id SERIAL PRIMARY KEY,
	order_id INT NOT NULL REFERENCES orders(order_id)  ON DELETE CASCADE,
	modified_at TIMESTAMPTZ NOT NULL,
	change_status VARCHAR(255) NOT NULL,
	modified_by VARCHAR(255) NOT NULL REFERENCES users(username),
	order_owner VARCHAR(255) NOT NULL REFERENCES users(username),
	type_changed TEXT[] DEFAULT ARRAY[]::TEXT[], 
    data_initial JSONB DEFAULT '{}'::jsonb, 
    data_changed JSONB DEFAULT '{}'::jsonb,
	isRead JSONB NOT NULL DEFAULT '{}'::jsonb
);
ALTER TABLE routes ENABLE ROW LEVEL SECURITY;
CREATE POLICY system_selectRoute_policy ON routes FOR SELECT USING (current_setting('role') = 'system');
CREATE POLICY system_insertRoute_policy ON routes FOR INSERT WITH CHECK (current_setting('role') = 'system');
CREATE POLICY system_updateRoute_policy ON routes FOR UPDATE USING (current_setting('role') = 'system');

CREATE POLICY company_selectVehicle_policy ON vehicles FOR SELECT USING (current_setting('role') = 'company' AND vehicle_id IN (SELECT vehicle_id FROM orders WHERE status = 'undergoing'::order_status AND requester = current_setting('username')));

CREATE TABLE notifications(
    notification_id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    data VARCHAR(255) NOT NULL,
    receiver VARCHAR(255) NOT NULL REFERENCES users(username) ON UPDATE CASCADE ON DELETE CASCADE,
    order_id INT REFERENCES orders(order_id),
    message_id INT REFERENCES messages(message_id),
    read BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
CREATE POLICY user_notification_policy ON notifications FOR SELECT USING (current_setting('username') = receiver);
CREATE POLICY system_insertNotification_policy ON notifications FOR INSERT WITH CHECK (current_setting('role') = 'admin');
CREATE POLICY system_updateNotification_policy ON notifications FOR UPDATE USING (current_setting('role') = 'admin');

CREATE TABLE costs(
                      empty_ctr DECIMAL(4, 2),
                      full_ctr DECIMAL(4, 2),
                      per_km DECIMAL(4, 2)
);
ALTER TABLE costs ENABLE ROW LEVEL SECURITY;
INSERT INTO costs VALUES (0, 0, 0);

CREATE POLICY company_costs_policy ON costs FOR SELECT USING (current_setting('role') = 'company');
CREATE POLICY admin_selectCosts_policy ON costs FOR SELECT USING (current_setting('role') = 'admin');
CREATE POLICY admin_updateCosts_policy ON costs FOR UPDATE USING (current_setting('role') = 'admin');

-- INSERTS

INSERT INTO saved4.routes (route) VALUES
                                      ('[(1,1), (2,3), (3,5)]'::path),
                                      ('[(4,2), (5,6), (7,8), (8,9)]'::path),
                                      ('[(0,0), (1,2)]'::path),
                                      ('[(3,3), (4,4), (5,5), (6,6), (7,7)]'::path),
                                      ('[(2,1), (3,4), (4,6)]'::path);

-- insert vehicles
INSERT INTO vehicles (position, battery_level, status, distance)
VALUES (POINT(6.8937, 52.2215), 92.00, 'in_use', 8.0);

INSERT INTO vehicles (position, battery_level, status, distance)
VALUES (POINT(6.8980, 52.2235), 85.00, 'available', 4.5);

INSERT INTO vehicles (position, battery_level, status, distance)
VALUES (POINT(6.8895, 52.2190), 76.50, 'in_use', 6.2);

INSERT INTO vehicles (position, battery_level, status, distance)
VALUES (POINT(6.8840, 52.2160), 64.75, 'out_of_order', 0.0);

INSERT INTO vehicles (position, battery_level, status, distance)
VALUES (POINT(6.9005, 52.2258), 48.20, 'available', 2.7);

INSERT INTO containers (max_weight, container_status)
VALUES (1500.50, 'available');

INSERT INTO containers (max_weight, container_status)
VALUES (2000.00, 'available');

INSERT INTO containers (max_weight, container_status)
VALUES (1750.75, 'broken');

INSERT INTO containers (max_weight, container_status)
VALUES (1600.00, 'available');

INSERT INTO containers (max_weight, container_status)
VALUES (2200.00, 'available');

COMMIT;
