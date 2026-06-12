-- WeChat JSAPI Store database initialization
-- Please create and select target database before running this script.
-- Example database name: `wechat_store`

-- -------------------------------------------
-- 1. Book
-- -------------------------------------------
CREATE TABLE book (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(128)   NOT NULL              COMMENT 'book name',
    subtitle    VARCHAR(255)   DEFAULT NULL           COMMENT 'subtitle',
    cover_url   VARCHAR(512)   DEFAULT NULL           COMMENT 'cover image url',
    price       DECIMAL(10, 2) NOT NULL               COMMENT 'price in yuan',
    original_price DECIMAL(10, 2) DEFAULT NULL         COMMENT 'original price',
    stock       INT            NOT NULL DEFAULT 0      COMMENT 'stock',
    intro       TEXT                                   COMMENT 'intro',
    sort        INT            DEFAULT 0               COMMENT 'sort desc',
    status      TINYINT        DEFAULT 1               COMMENT '1 on shelf, 0 off shelf',
    deleted     TINYINT        DEFAULT 0               COMMENT '0 normal, 1 deleted',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='book';

-- -------------------------------------------
-- 2. WeChat user
-- -------------------------------------------
CREATE TABLE wx_user (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid         VARCHAR(64)  NOT NULL UNIQUE       COMMENT 'wechat openid',
    nickname       VARCHAR(128) DEFAULT NULL           COMMENT 'wechat nickname',
    avatar         VARCHAR(512) DEFAULT NULL           COMMENT 'wechat avatar',
    receiver_name  VARCHAR(64)  DEFAULT NULL           COMMENT 'receiver name',
    phone          VARCHAR(32)  DEFAULT NULL           COMMENT 'receiver phone',
    school         VARCHAR(128) DEFAULT NULL           COMMENT 'school',
    province       VARCHAR(64)  DEFAULT NULL           COMMENT 'province',
    city           VARCHAR(64)  DEFAULT NULL           COMMENT 'city',
    district       VARCHAR(64)  DEFAULT NULL           COMMENT 'district',
    detail_address VARCHAR(255) DEFAULT NULL           COMMENT 'detail address',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='wechat user';

-- -------------------------------------------
-- 3. H5 config
-- -------------------------------------------
CREATE TABLE h5_config (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    site_title      VARCHAR(128) NOT NULL COMMENT 'site title',
    site_subtitle   VARCHAR(255) NOT NULL COMMENT 'site subtitle',
    service_wechat  VARCHAR(128) NOT NULL COMMENT 'service wechat',
    service_phone   VARCHAR(64)  NOT NULL COMMENT 'service phone',
    work_time       VARCHAR(128) NOT NULL COMMENT 'work time',
    notice_text     VARCHAR(512) NOT NULL COMMENT 'notice text',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='h5 config';

INSERT INTO h5_config (
    id,
    site_title,
    site_subtitle,
    service_wechat,
    service_phone,
    work_time,
    notice_text
) VALUES (
    1,
    '示例教辅资料订购系统',
    '微信生态内的轻量商品订购入口',
    '请联系管理员',
    '待配置',
    '09:00 - 18:00',
    '下单后请保持电话畅通，配送信息以后续通知为准。'
) ON DUPLICATE KEY UPDATE id = id;

-- -------------------------------------------
-- 4. Order
-- Status flow: CREATED -> PAID -> DELIVERING -> FINISHED, or CANCELLED
-- -------------------------------------------
CREATE TABLE order_info (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no         VARCHAR(64)    NOT NULL UNIQUE       COMMENT 'order no',
    user_id          BIGINT         NOT NULL              COMMENT 'wx_user.id',
    total_amount     DECIMAL(10, 2) NOT NULL              COMMENT 'total amount',
    pay_amount       DECIMAL(10, 2) NOT NULL              COMMENT 'pay amount',
    status           VARCHAR(32)    NOT NULL DEFAULT 'CREATED'
                                                           COMMENT 'CREATED/PAID/DELIVERING/FINISHED/CANCELLED',
    pay_status       TINYINT        DEFAULT 0              COMMENT '0 pending, 1 success, 2 closed',
    receiver_name    VARCHAR(64)    NOT NULL              COMMENT 'receiver name',
    phone            VARCHAR(32)    NOT NULL              COMMENT 'receiver phone',
    school           VARCHAR(128)   NOT NULL              COMMENT 'school',
    province         VARCHAR(64)    NOT NULL              COMMENT 'province',
    city             VARCHAR(64)    NOT NULL              COMMENT 'city',
    district         VARCHAR(64)    NOT NULL              COMMENT 'district',
    detail_address   VARCHAR(255)   NOT NULL              COMMENT 'detail address',
    remark           VARCHAR(255)   DEFAULT NULL           COMMENT 'remark',
    tracking_company VARCHAR(64)    DEFAULT NULL           COMMENT 'tracking company',
    tracking_no      VARCHAR(128)   DEFAULT NULL           COMMENT 'tracking no',
    pay_time         DATETIME       DEFAULT NULL           COMMENT 'pay time',
    deliver_time     DATETIME       DEFAULT NULL           COMMENT 'deliver time',
    finish_time      DATETIME       DEFAULT NULL           COMMENT 'finish time',
    create_time      DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_pay_time (pay_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='order info';

-- -------------------------------------------
-- 5. Order item
-- -------------------------------------------
CREATE TABLE order_item (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT         NOT NULL               COMMENT 'order_info.id',
    book_id     BIGINT         NOT NULL               COMMENT 'book.id',
    book_name   VARCHAR(128)   NOT NULL               COMMENT 'book name snapshot',
    cover_url   VARCHAR(512)   DEFAULT NULL            COMMENT 'cover url snapshot',
    price       DECIMAL(10, 2) NOT NULL               COMMENT 'price snapshot',
    quantity    INT            NOT NULL               COMMENT 'quantity',
    subtotal    DECIMAL(10, 2) NOT NULL               COMMENT 'subtotal',
    create_time DATETIME       DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_order_id (order_id),
    INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='order item';

-- -------------------------------------------
-- 6. Payment record
-- -------------------------------------------
CREATE TABLE payment_record (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id       BIGINT         NOT NULL               COMMENT 'order_info.id',
    order_no       VARCHAR(64)    NOT NULL               COMMENT 'order no',
    transaction_id VARCHAR(128)   DEFAULT NULL            COMMENT 'wechat transaction id',
    mchid          VARCHAR(64)    DEFAULT NULL            COMMENT 'merchant id',
    pay_channel    VARCHAR(32)    DEFAULT 'JSAPI'         COMMENT 'pay channel',
    amount         DECIMAL(10, 2) NOT NULL               COMMENT 'amount',
    pay_status     TINYINT        DEFAULT 0               COMMENT '0 pending, 1 success, 2 closed',
    notify_content TEXT                                   COMMENT 'reserved for redacted notification metadata',
    pay_time       DATETIME       DEFAULT NULL            COMMENT 'pay time',
    create_time    DATETIME       DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_transaction_id (transaction_id),
    INDEX idx_order_id (order_id),
    INDEX idx_pay_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='payment record';
