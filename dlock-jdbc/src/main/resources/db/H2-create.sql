CREATE TABLE IF NOT EXISTS "@@tableName@@" (
  "LCK_KEY" varchar(100) PRIMARY KEY,
  "LCK_HNDL_ID" varchar(100) not null,
  "CREATED_TIME" DATETIME not null,
  "EXPIRE_SEC" int not null
);
CREATE UNIQUE INDEX IF NOT EXISTS "@@tableName@@_HNDL_UX" ON  "@@tableName@@" ("LCK_HNDL_ID");