-- Demo data.
--
-- Seeded through Flyway so a freshly started stack (docker compose up) is immediately usable:
-- one account to sign in with and enough contacts to make sorting, filtering and paging
-- meaningful rather than theoretical.
--
-- The password hash below is BCrypt for 'demo1234'. Fine for a demo, obviously not for anything
-- real: see the README for how to change it.

INSERT INTO users (id, username, password_hash, display_name)
VALUES ('00000000-0000-4000-8000-000000000001',
        'demo',
        '$2a$10$WloPrYig/Or6F4i8NRiLP.88C5wJUxRl6uQQr1bAYBaYcm0y8gemG',
        'Demo User');

INSERT INTO user_roles (user_id, role)
VALUES ('00000000-0000-4000-8000-000000000001', 'USER'),
       ('00000000-0000-4000-8000-000000000001', 'ADMIN');

INSERT INTO contacts (id, first_name, last_name, email, phone_number, company, job_title, address,
                      created_at, updated_at)
VALUES
    ('1c9c08d3-7bf7-4937-af7b-efe90309713c', 'Ada', 'Johnson', 'ada.johnson@example.com', '+33 1 41 34 55 26', 'Globex', 'Sales Director', '3 Via Roma, Milan', now() - interval '129 days', now() - interval '129 days'),
    ('bb0699e6-0ae9-44d9-94f4-ba3f6528615a', 'Grace', 'Perlman', 'grace.perlman@example.com', '+33 1 37 62 65 87', 'Globex', 'CTO', '8 Bahnhofstrasse, Zurich', now() - interval '137 days', now() - interval '107 days'),
    ('d70fb337-30ed-4acc-9b39-c3e1e643db5b', 'Alan', 'van Rossum', 'alan.vanrossum@example.com', '+33 1 19 45 73 32', 'Hooli', 'Designer', '12 Marylebone Road, London', now() - interval '61 days', now() - interval '23 days'),
    ('291e41f5-329e-41ce-9cec-7a33059a529f', 'Katherine', 'Nilsson', 'katherine.nilsson@example.com', '+33 1 46 56 40 21', 'Umbrella Labs', 'Researcher', '42 Wallaby Way, Sydney', now() - interval '178 days', now() - interval '57 days'),
    ('f7e9f090-b3cb-4a5c-a0c6-4bed79732c51', 'Barbara', 'Schmidt', 'barbara.schmidt@example.com', '+33 1 44 48 14 69', 'Umbrella Labs', 'Researcher', '3 Via Roma, Milan', now() - interval '307 days', now() - interval '186 days'),
    ('c2d0dc26-6dfe-4a45-89ae-32e85344c0a8', 'Edsger', 'Rahman', 'edsger.rahman@example.com', '+33 1 83 88 81 75', 'Vandelay Industries', 'Technical Writer', '8 Bahnhofstrasse, Zurich', now() - interval '67 days', now() - interval '23 days'),
    ('a21935a8-1ef3-4006-a7e5-7e89409dc30e', 'Margaret', 'Volkov', 'margaret.volkov@example.com', '+33 1 15 72 36 25', 'Soylent Corp', 'QA Engineer', '19 Damrak, Amsterdam', now() - interval '379 days', now() - interval '342 days'),
    ('4d151564-05ec-462c-a07f-965809b86103', 'Linus', 'Turing', 'linus.turing@example.com', '+33 1 82 82 99 48', 'Contoso', 'Account Manager', '77 Gran Via, Madrid', now() - interval '106 days', now() - interval '54 days'),
    ('8be8b6a8-1b7a-4d37-9fcf-5b4e3598c8a5', 'Dorothy', 'Berners-Lee', 'dorothy.bernerslee@example.com', '+33 1 78 45 22 82', 'Vandelay Industries', 'Technical Writer', '6 Nyhavn, Copenhagen', now() - interval '285 days', now() - interval '0 days'),
    ('09665bd6-2f95-47f0-ae8a-e1fb60dd1ce4', 'Tim', 'Knuth', 'tim.knuth@example.com', '+33 1 17 27 66 84', 'Umbrella Labs', 'DevOps Engineer', '6 Nyhavn, Copenhagen', now() - interval '340 days', now() - interval '180 days'),
    ('f20d5ae9-2e3f-404d-bc76-b6c632fbb3ca', 'Radia', 'Haddad', 'radia.haddad@example.com', '+33 1 40 16 37 82', 'Umbrella Labs', 'Technical Writer', '221B Baker Street, London', now() - interval '202 days', now() - interval '144 days'),
    ('ef9ae782-1d97-4656-aaee-53553ddff19a', 'Anita', 'Farouk', 'anita.farouk@example.com', '+33 1 81 52 56 36', 'Initech', 'Data Analyst', '5 Rue Lepic, Paris', now() - interval '377 days', now() - interval '154 days'),
    ('35e3e1d8-1d7d-405f-a196-90fa215f7b66', 'Shafi', 'Tanaka', 'shafi.tanaka@example.com', '+33 1 96 18 70 23', 'Cyberdyne', 'Sales Director', '221B Baker Street, London', now() - interval '273 days', now() - interval '207 days'),
    ('a612497f-0475-4960-9b89-eee3d505c1e0', 'Leslie', 'Ricci', 'leslie.ricci@example.com', '+33 1 49 15 51 45', 'Vandelay Industries', 'Sales Director', '19 Damrak, Amsterdam', now() - interval '17 days', now() - interval '12 days'),
    ('9ddc67ac-11c9-4288-8b4e-d9d3c305bb5e', 'Frances', 'Hopper', 'frances.hopper@example.com', '+33 1 51 51 12 46', 'Umbrella Labs', 'Software Engineer', '5 Rue Lepic, Paris', now() - interval '74 days', now() - interval '66 days'),
    ('f866f11c-278e-44ea-99a2-b9223be4d7e4', 'Jean', 'Vaughan', 'jean.vaughan@example.com', '+33 1 57 31 42 16', 'Vandelay Industries', 'Researcher', '3 Via Roma, Milan', now() - interval '90 days', now() - interval '72 days'),
    ('37504b5e-a3cc-4bed-9b19-303df84f61ed', 'Donald', 'Bartik', 'donald.bartik@example.com', '+33 1 31 93 99 15', 'Northwind Traders', 'Technical Writer', '10 Downing Street, London', now() - interval '20 days', now() - interval '12 days'),
    ('5bd11943-da1f-40aa-b975-3c45b8c9fe1f', 'Guido', 'Benali', 'guido.benali@example.com', '+33 1 42 98 33 80', 'Analytical Engines', 'Product Manager', '221B Baker Street, London', now() - interval '12 days', now() - interval '7 days'),
    ('4dd58fee-694f-49bd-a474-6927eb86ca8d', 'James', 'Nair', 'james.nair@example.com', '+33 1 72 79 82 97', 'Globex', 'Support Lead', '1 Infinite Loop, Cupertino', now() - interval '164 days', now() - interval '48 days'),
    ('17054d35-9970-4b10-b52d-49f0b52451af', 'Bjarne', 'Mensah', 'bjarne.mensah@example.com', '+33 1 82 69 65 45', 'Vandelay Industries', 'CTO', '1 Infinite Loop, Cupertino', now() - interval '312 days', now() - interval '81 days'),
    ('52a1fcce-845b-468a-a604-3c2a75b0a1f1', 'Amelie', 'de Vries', 'amelie.devries@example.com', '+33 1 75 44 38 31', 'Globex', 'Technical Writer', '6 Nyhavn, Copenhagen', now() - interval '13 days', now() - interval '1 days'),
    ('1c38df40-0c02-4810-8c37-3733e4d4939c', 'Ines', 'Lovelace', 'ines.lovelace@example.com', '+33 1 29 26 86 91', 'Globex', 'Data Analyst', '19 Damrak, Amsterdam', now() - interval '114 days', now() - interval '59 days'),
    ('f66d608e-7e17-4f20-9e75-0e1622166c41', 'Youssef', 'Torvalds', 'youssef.torvalds@example.com', '+33 1 88 76 68 23', 'Contoso', 'Data Analyst', '3 Via Roma, Milan', now() - interval '217 days', now() - interval '98 days'),
    ('75450ade-fdfa-4047-91c8-ec918bfed37c', 'Fatima', 'Allen', 'fatima.allen@example.com', '+33 1 98 84 18 81', 'Cyberdyne', 'QA Engineer', '12 Marylebone Road, London', now() - interval '244 days', now() - interval '23 days'),
    ('3bf3d320-40c0-45e1-a5c6-9566701be7b8', 'Lars', 'Duarte', 'lars.duarte@example.com', '+33 1 69 58 30 67', 'Analytical Engines', 'Account Manager', '12 Marylebone Road, London', now() - interval '122 days', now() - interval '73 days'),
    ('75a51142-bcc8-4074-85b0-3d678709fd07', 'Sofia', 'Alvarez', 'sofia.alvarez@example.com', '+33 1 28 33 35 55', 'Northwind Traders', 'CTO', '1 Infinite Loop, Cupertino', now() - interval '108 days', now() - interval '6 days'),
    ('5f5e0bf3-c930-42cf-859e-febcfb2c4827', 'Malik', 'Petrova', 'malik.petrova@example.com', '+33 1 79 98 52 82', 'Hooli', 'Sales Director', '12 Marylebone Road, London', now() - interval '145 days', now() - interval '26 days'),
    ('55fdfa51-99f1-4f57-bca5-e0a1f1660cec', 'Hannah', 'Okafor', 'hannah.okafor@example.com', '+33 1 60 98 34 15', 'Vandelay Industries', 'Sales Director', '42 Wallaby Way, Sydney', now() - interval '22 days', now() - interval '22 days'),
    ('79a25715-670f-4990-8de2-9ad206c04ef4', 'Diego', 'Muller', 'diego.muller@example.com', '+33 1 76 61 19 50', 'Northwind Traders', 'Account Manager', '6 Nyhavn, Copenhagen', now() - interval '184 days', now() - interval '52 days'),
    ('16df7523-a531-48cc-9702-afd695ee389d', 'Priya', 'Hamilton', 'priya.hamilton@example.com', '+33 1 59 79 18 56', 'Contoso', 'Account Manager', '3 Via Roma, Milan', now() - interval '15 days', now() - interval '3 days'),
    ('d78b9e1b-d359-49aa-84b0-8e58650bf50b', 'Omar', 'Lamport', 'omar.lamport@example.com', '+33 1 15 64 33 48', 'Vandelay Industries', 'QA Engineer', '23 Karl-Marx-Allee, Berlin', now() - interval '263 days', now() - interval '76 days'),
    ('4f14fd99-5cfd-40a2-a0e1-0fd0c4d00ee4', 'Clara', 'Moreau', 'clara.moreau@example.com', '+33 1 89 90 77 96', 'Cyberdyne', 'CTO', '6 Nyhavn, Copenhagen', now() - interval '152 days', now() - interval '129 days'),
    ('f91a80d5-384b-41af-8a9f-6beae231fe99', 'Noah', 'Weber', 'noah.weber@example.com', '+33 1 51 79 89 24', 'Stark Solutions', 'QA Engineer', '5 Rue Lepic, Paris', now() - interval '68 days', now() - interval '45 days'),
    ('881bc8db-a77c-4d08-8936-61e3b172bcda', 'Zara', 'Garcia', 'zara.garcia@example.com', '+33 1 30 24 17 85', 'Cyberdyne', 'Data Analyst', '42 Wallaby Way, Sydney', now() - interval '121 days', now() - interval '101 days'),
    ('0f50d5d4-9176-4030-aa9b-c548f93097cc', 'Mateo', 'Silva', 'mateo.silva@example.com', '+33 1 56 77 28 66', 'Cyberdyne', 'QA Engineer', '77 Gran Via, Madrid', now() - interval '211 days', now() - interval '0 days'),
    ('fcf0a8b6-fc66-44e5-a2a9-b17b05b0281f', 'Elena', 'Fischer', 'elena.fischer@example.com', '+33 1 85 34 99 21', 'Vandelay Industries', 'Designer', '12 Marylebone Road, London', now() - interval '285 days', now() - interval '36 days'),
    ('f52b59f9-77e1-48cb-9f7d-0b71407856d0', 'Kofi', 'Dijkstra', 'kofi.dijkstra@example.com', '+33 1 40 19 15 21', 'Northwind Traders', 'Data Analyst', '19 Damrak, Amsterdam', now() - interval '374 days', now() - interval '131 days'),
    ('3a2c7172-ab6b-4001-a9b4-6bb19769882e', 'Yuki', 'Goldwasser', 'yuki.goldwasser@example.com', '+33 1 44 69 30 67', 'Northwind Traders', 'Designer', '77 Gran Via, Madrid', now() - interval '37 days', now() - interval '28 days'),
    ('f8fdd73f-8f2a-4e19-9370-3176d4f83612', 'Nadia', 'Stroustrup', 'nadia.stroustrup@example.com', '+33 1 33 82 50 23', 'Wayne Industries', 'Support Lead', '19 Damrak, Amsterdam', now() - interval '66 days', now() - interval '35 days'),
    ('5e6bcb70-2995-4517-9ccd-cf8831ca8262', 'Tomas', 'Diallo', 'tomas.diallo@example.com', '+33 1 79 30 98 43', 'Umbrella Labs', 'Support Lead', '42 Wallaby Way, Sydney', now() - interval '58 days', now() - interval '42 days'),
    ('3cbbc04a-a1d6-4b76-a665-885209c311d9', 'Ines', 'Ahmed', 'ines.ahmed@example.com', '+33 1 82 10 56 11', 'Stark Solutions', 'Researcher', '77 Gran Via, Madrid', now() - interval '298 days', now() - interval '237 days'),
    ('b1f44baa-1c91-4988-99df-523269b036ec', 'Rui', 'Costa', 'rui.costa@example.com', '+33 1 94 89 88 81', 'Contoso', 'Designer', '10 Downing Street, London', now() - interval '399 days', now() - interval '167 days'),
    ('93071542-817b-4fa3-a998-4dadfbba3357', 'Aisha', 'Braun', 'aisha.braun@example.com', '+33 1 46 65 26 40', 'Northwind Traders', 'Data Analyst', '8 Bahnhofstrasse, Zurich', now() - interval '289 days', now() - interval '124 days'),
    ('7912471a-33f0-4608-960f-0f658ecc4d66', 'Bram', 'Liskov', 'bram.liskov@example.com', '+33 1 56 51 33 52', 'Vandelay Industries', 'Support Lead', '6 Nyhavn, Copenhagen', now() - interval '356 days', now() - interval '261 days'),
    ('7501c52c-5d09-4fef-87a9-86a851c8bfa8', 'Chiara', 'Borg', 'chiara.borg@example.com', '+33 1 21 19 22 19', 'Initech', 'DevOps Engineer', '8 Bahnhofstrasse, Zurich', now() - interval '116 days', now() - interval '5 days'),
    ('c3d0bb4f-9ce3-4519-b155-ad58ce63b25f', 'Dmitri', 'Gosling', 'dmitri.gosling@example.com', '+33 1 89 18 57 85', 'Umbrella Labs', 'DevOps Engineer', '6 Nyhavn, Copenhagen', now() - interval '64 days', now() - interval '56 days'),
    ('42d681e5-b0fa-4daa-a523-43d470e19dbc', 'Eva', 'Rossi', 'eva.rossi@example.com', '+33 1 49 32 88 85', 'Cyberdyne', 'Researcher', '23 Karl-Marx-Allee, Berlin', now() - interval '359 days', now() - interval '221 days'),
    ('d2f39a71-6669-4913-b4db-d8191e0fd758', 'Felix', 'Jansen', 'felix.jansen@example.com', '+33 1 89 58 75 21', 'Globex', 'CTO', '8 Bahnhofstrasse, Zurich', now() - interval '399 days', now() - interval '22 days'),
    ('dc77d233-b99f-43a1-84dd-3f1fc18a06a4', 'Greta', 'Novak', 'greta.novak@example.com', '+33 1 43 86 57 46', 'Vandelay Industries', 'Designer', '12 Marylebone Road, London', now() - interval '221 days', now() - interval '59 days'),
    ('19273e17-0fb5-4858-9537-b67e1d12ad17', 'Hugo', 'Larsen', 'hugo.larsen@example.com', '+33 1 83 31 97 41', 'Analytical Engines', 'Product Manager', '1 Infinite Loop, Cupertino', now() - interval '392 days', now() - interval '346 days');
