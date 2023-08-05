-- DROP PROCEDURE IF EXISTS sendNotification(text);
CREATE OR REPLACE PROCEDURE ms_schema.sendNotification(customer_id text, OUT message text)
LANGUAGE plpgsql AS $$
BEGIN
  -- logic here
  message := 'Notification sent to ' || customer_id;
END;
$$;

CREATE OR REPLACE FUNCTION ms_schema.sendNotificationFunc(customer_id text) RETURNS text
LANGUAGE plpgsql AS $$
DECLARE
  message text;
BEGIN
  message := 'PostgreSQL:sendNotificationFunc: Notification sent to ' || customer_id;
  RETURN message;
END;
$$;
