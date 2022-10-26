CREATE TABLE Licenses (
	ID NUMBER(19) PRIMARY KEY, 
	Name VARCHAR(100) UNIQUE NOT NULL, 
	Type VARCHAR(15)
);

CREATE TABLE Entities (
	ID NUMBER(19) PRIMARY KEY,
	Name VARCHAR(255),
	Code VARCHAR(50) UNIQUE NOT NULL,
	Country VARCHAR(10) NOT NULL
);

CREATE TABLE Nationalities (
	ID NUMBER(19) PRIMARY KEY,
	Name VARCHAR(100) NOT NULL,
	Code VARCHAR(10) UNIQUE NOT NULL
);

CREATE TABLE ApprovedEntities (
	ID NUMBER(19) PRIMARY KEY,
	LicenseID NUMBER(19) NOT NULL,
	EntityID NUMBER(19) NOT NULL,
	Type VARCHAR(1) NOT NULL,
	NDA NUMBER DEFAULT 0,
	CONSTRAINT PK_AE_Licenses
			FOREIGN KEY (LicenseID)
			REFERENCES Licenses(ID),
	CONSTRAINT PK_AE_Entities
			FOREIGN KEY (EntityID)
			REFERENCES Entities(ID)
);

CREATE TABLE ApprovedNationalities (
	ID NUMBER(19) PRIMARY KEY,
	LicenseID NUMBER(19) NOT NULL,
	NationalityID NUMBER(19) NOT NULL,
	CONSTRAINT PK_AN_Licenses
			FOREIGN KEY (LicenseID)
			REFERENCES Licenses(ID),
	CONSTRAINT PK_AN_Nationalities
			FOREIGN KEY (NationalityID)
			REFERENCES Nationalities(ID)
);


CREATE TABLE DeniedNationalities (
	ID NUMBER(19) PRIMARY KEY,
	LicenseID NUMBER(19) NOT NULL,
	NationalityID NUMBER(19) NOT NULL,
	CONSTRAINT PK_DN_Licenses
			FOREIGN KEY (LicenseID)
			REFERENCES Licenses(ID),
	CONSTRAINT PK_DN_Nationalities
			FOREIGN KEY (NationalityID)
			REFERENCES Nationalities(ID)
);

CREATE TABLE NDA (
	ID NUMBER(19) PRIMARY KEY,
	UserID VARCHAR(12) NOT NULL,
	LicenseID NUMBER(19) NOT NULL,
	CONSTRAINT UK_NDA UNIQUE (UserID, LicenseID),
	CONSTRAINT PK_NDA_Licenses
			FOREIGN KEY (LicenseID)
			REFERENCES Licenses(ID)
);

CREATE TABLE Status (
	LastSyncTime NUMBER(19)
);

INSERT INTO Status(LastSyncTime) Values (0);
COMMIT;