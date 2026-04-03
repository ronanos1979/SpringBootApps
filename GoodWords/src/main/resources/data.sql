create database goodwords;

CREATE USER 'goodwords-user'@'localhost';

GRANT ALL PRIVILEGES ON goodwords.* To 'goodwords-user'@'localhost' ;

alter user 'goodwords-user'@'localhost' IDENTIFIED BY 'goodwordspass';

insert into word (ID, USERNAME, LOOKUP_WORD, DESCRIPTION, TARGET_DATE, DONE) values(10001, 'ronan', 'thesaurus', 'list of words', CURRENT_DATE(),false);

