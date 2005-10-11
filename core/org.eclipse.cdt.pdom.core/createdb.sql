connect 'jdbc:derby:db;create=true';

-- Files table
CREATE TABLE Strings
	(id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	 str VARCHAR(256) NOT NULL);

disconnect;
exit;
