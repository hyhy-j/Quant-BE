CREATE TABLE IF NOT EXISTS stocks
(
    id         BIGSERIAL    PRIMARY KEY,
    code       VARCHAR(10)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    market     VARCHAR(10)  NOT NULL DEFAULT 'KRX',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO stocks (code, name, market) VALUES
    ('005930', '삼성전자',        'KRX'),
    ('000660', 'SK하이닉스',      'KRX'),
    ('402340', 'SK스퀘어',        'KRX'),
    ('207940', '삼성바이오로직스', 'KRX'),
    ('005380', '현대차',          'KRX'),
    ('373220', 'LG에너지솔루션',  'KRX'),
    ('032830', '삼성생명',        'KRX'),
    ('028260', '삼성물산',        'KRX'),
    ('329180', 'HD현대중공업',    'KRX'),
    ('000270', '기아',            'KRX')
ON CONFLICT (code) DO NOTHING;