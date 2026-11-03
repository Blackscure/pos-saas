
CREATE TABLE branches (
                          id UUID PRIMARY KEY,
                          business_id UUID NOT NULL,
                          name VARCHAR(150) NOT NULL,
                          code VARCHAR(50) NOT NULL,
                          phone VARCHAR(30),
                          email VARCHAR(150),
                          address VARCHAR(255),
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT fk_branches_business
                              FOREIGN KEY (business_id)
                                  REFERENCES businesses(id),

                          CONSTRAINT uq_branch_business_code
                              UNIQUE (business_id, code),

                          CONSTRAINT uq_branch_business_name
                              UNIQUE (business_id, name)
);

CREATE INDEX idx_branches_business_id
    ON branches(business_id);
