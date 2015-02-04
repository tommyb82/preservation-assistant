INSERT INTO ROLES ( "ROLE", "DESCRIPTION" ) VALUES ( 'ROLE_USER', 'Normal user account');
INSERT INTO ROLES ( "ROLE", "DESCRIPTION" ) VALUES ( 'ROLE_ADMIN', 'Admin level access');

-- Default password: adm1n
INSERT INTO USERS ( "USERNAME", "PASSWORD" ) VALUES ( 'admin', '$2a$10$408HsqMnj5BKOvg1w3aluOxG8YDgwLsvHri8EY5zckztyYAu.Or52');
-- Default password: pr3s3rv4tr0n
INSERT INTO USERS ( "USERNAME", "PASSWORD" ) VALUES ( 'p3kuser', '$2a$10$P5JntEaSSYjmexfkA8uCWe/.Vr1Q6voK18kb3NEk.tte5UhhgrY2W');

INSERT INTO USER_ROLE ( "USERNAME", "ROLE" ) VALUES ( 'admin', 'ROLE_USER' );
INSERT INTO USER_ROLE ( "USERNAME", "ROLE" ) VALUES ( 'admin', 'ROLE_ADMIN' );
INSERT INTO USER_ROLE ( "USERNAME", "ROLE" ) VALUES ( 'p3kuser', 'ROLE_USER' );