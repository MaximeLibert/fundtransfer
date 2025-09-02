CREATE TABLE IF NOT EXISTS account (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL,
  currency VARCHAR(3) NOT NULL,
  balance DECIMAL(19, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transfer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  from_account_id BIGINT NOT NULL,
  to_account_id BIGINT NOT NULL,
  amount DECIMAL(19, 2) NOT NULL,
  from_currency VARCHAR(3) NOT NULL,
  to_currency VARCHAR(3) NOT NULL,
  exchange_rate DECIMAL(19, 6) NOT NULL,
  amount_in_target_currency DECIMAL(19, 2) NOT NULL,
  timestamp DATETIME NOT NULL,
  FOREIGN KEY (from_account_id) REFERENCES account(id),
  FOREIGN KEY (to_account_id) REFERENCES account(id)
);
