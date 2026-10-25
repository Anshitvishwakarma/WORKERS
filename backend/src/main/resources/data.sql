-- =====================================================================
-- FixMate Initial Services Data
-- =====================================================================
INSERT INTO services (name, description, icon, active, created_at)
SELECT 'Plumbing', 'Expert pipe fixing, drain cleaning, leak repair, and bathroom fixture installation.', 'bi-wrench-adjustable', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Plumbing');

INSERT INTO services (name, description, icon, active, created_at)
SELECT 'Electrician', 'Wiring, fuse repair, switchboard installations, lighting fixtures, and electrical safety audits.', 'bi-lightning-charge', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Electrician');

INSERT INTO services (name, description, icon, active, created_at)
SELECT 'Home Cleaning', 'Deep home sanitization, kitchen and bathroom cleaning, floor polishing, and post-renovation cleanup.', 'bi-stars', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Home Cleaning');

INSERT INTO services (name, description, icon, active, created_at)
SELECT 'AC Repair', 'Air conditioning servicing, gas refilling, filter cleaning, cooling diagnostics, and installation.', 'bi-snow', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'AC Repair');

INSERT INTO services (name, description, icon, active, created_at)
SELECT 'Carpenter', 'Custom wood repairs, furniture assembly, door & window hinge fixes, and cabinet making.', 'bi-hammer', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Carpenter');

INSERT INTO services (name, description, icon, active, created_at)
SELECT 'Painting', 'Interior and exterior wall painting, waterproof coating, primer application, and texture finishing.', 'bi-brush', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Painting');

INSERT INTO services (name, description, icon, active, created_at)
SELECT 'Appliance Repair', 'Washing machine, refrigerator, microwave, and dishwasher diagnostic and repair services.', 'bi-plug', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'Appliance Repair');

INSERT INTO services (name, description, icon, active, created_at)
SELECT 'RO & Water Purifier', 'Filter replacement, membrane cleaning, water testing, and installation of all purifier brands.', 'bi-droplet-half', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM services WHERE name = 'RO & Water Purifier');
