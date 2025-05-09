-- Insert past dates
INSERT INTO reservation_system.dates (`date`) VALUES
                                                  ('2024-01-15'),
                                                  ('2024-02-01'),
                                                  ('2024-02-15'),
                                                  ('2024-03-01');

-- Create flight dates for past dates using existing flight schedules
INSERT INTO reservation_system.flight_dates (flightScheduleId, dateId, available_seats) VALUES
-- Get the newly inserted date IDs (they should be 11, 12, 13, 14 if following the existing sequence)
(1, (SELECT id FROM dates WHERE date = '2024-01-15'), 0),  -- All seats taken for past flights
(2, (SELECT id FROM dates WHERE date = '2024-02-01'), 0),
(3, (SELECT id FROM dates WHERE date = '2024-02-15'), 0),
(4, (SELECT id FROM dates WHERE date = '2024-03-01'), 0);

-- Insert past reservations (using the new flight_dates)
INSERT INTO reservation_system.reservations (customerId, flightDateId, reserved_seats) VALUES
-- For customerId 1 (existing user)
(13, (SELECT fd.id FROM flight_dates fd
                           JOIN dates d ON fd.dateId = d.id
     WHERE d.date = '2024-01-15' LIMIT 1), 2),
(13, (SELECT fd.id FROM flight_dates fd
     JOIN dates d ON fd.dateId = d.id
     WHERE d.date = '2024-02-01' LIMIT 1), 3),

-- For customerId 2 (existing user)
(2, (SELECT fd.id FROM flight_dates fd
     JOIN dates d ON fd.dateId = d.id
     WHERE d.date = '2024-02-15' LIMIT 1), 4),
(2, (SELECT fd.id FROM flight_dates fd
     JOIN dates d ON fd.dateId = d.id
     WHERE d.date = '2024-03-01' LIMIT 1), 2);

-- Insert seat numbers for past reservations
INSERT INTO seat_numbers (reservationId, seat_number)
SELECT r.id, s.seat_number
FROM reservations r
         CROSS JOIN (
    SELECT 1 AS seat_number UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
) s
WHERE r.flightDateId IN (
    SELECT fd.id
    FROM flight_dates fd
             JOIN dates d ON fd.dateId = d.id
    WHERE d.date < '2025-01-01'
)
  AND s.seat_number <= r.reserved_seats;

-- Update tokens for customers based on these reservations
UPDATE customers c
SET c.tokens = (
    SELECT SUM(r.reserved_seats)
    FROM reservations r
    WHERE r.customerId = c.id
);