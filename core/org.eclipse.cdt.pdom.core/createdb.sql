connect 'jdbc:derby:db;create=true';

-- Strings table
create table Strings
	(id int generated always as identity primary key,
	 string varchar(256) not null);
create index StringIx on Strings(string);

-- Files table
create table Files
	(id int generated always as identity primary key,
	 name varchar(256) not null);
create index FilesIx on Files(name);

-- Names table (IASTName)
create table Names
	(nameId int not null,
	 fileId int not null,
	 offset int not null,
	 length int not null,
	 bindingId int not null);

-- Bindings table
create table Bindings
	(id int generated always as identity primary key,
	 nameId int not null,
	 type int not null);

disconnect;
exit;
