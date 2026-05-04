CREATE TABLE IF NOT EXISTS department (
  id   BIGINT       AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS people (
  id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
  name              VARCHAR(255) NOT NULL,
  age               INT          NOT NULL,
  email             VARCHAR(255),
  department        BIGINT,
  version           BIGINT,
  created_at        TIMESTAMP,
  last_modified_at  TIMESTAMP,
  created_by        VARCHAR(64),
  last_modified_by  VARCHAR(64),
  contact_phone     VARCHAR(64),
  contact_website   VARCHAR(255),
  CONSTRAINT fk_person_department FOREIGN KEY (department) REFERENCES department(id)
);

CREATE TABLE IF NOT EXISTS address (
  person BIGINT       NOT NULL,
  street VARCHAR(255) NOT NULL,
  city   VARCHAR(255) NOT NULL,
  zip    VARCHAR(32)  NOT NULL,
  CONSTRAINT fk_address_person FOREIGN KEY (person) REFERENCES people(id)
);

CREATE TABLE IF NOT EXISTS phone (
  person BIGINT      NOT NULL,
  label  VARCHAR(64) NOT NULL,
  number VARCHAR(64) NOT NULL,
  PRIMARY KEY (person, label),
  CONSTRAINT fk_phone_person FOREIGN KEY (person) REFERENCES people(id)
);

CREATE TABLE IF NOT EXISTS hobby (
  person   BIGINT       NOT NULL,
  position INT          NOT NULL,
  name     VARCHAR(255) NOT NULL,
  PRIMARY KEY (person, position),
  CONSTRAINT fk_hobby_person FOREIGN KEY (person) REFERENCES people(id)
);
