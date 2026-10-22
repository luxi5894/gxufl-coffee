-- ============================================================
-- GXUFL Coffee Atelier · 数据库初始化脚本
-- MySQL 8.0+ / utf8mb4
-- 执行方式: mysql -u root -p < docs/database.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS coffee_shop
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE coffee_shop;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS product_spec;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- 用户表（管理员与普通用户共用）
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    nickname    VARCHAR(40)  NOT NULL COMMENT '昵称',
    phone       VARCHAR(20)  NOT NULL COMMENT '登录账号 / 手机号',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    role        VARCHAR(10)  NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 停用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_phone (phone)
) ENGINE = InnoDB COMMENT = '用户表';

-- 商品分类（东南亚六国）
CREATE TABLE category (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(20) NOT NULL COMMENT '分类名称',
    name_en     VARCHAR(40) DEFAULT NULL COMMENT '英文名',
    icon        VARCHAR(20) DEFAULT NULL COMMENT '图标文字',
    sort        INT         NOT NULL DEFAULT 0,
    status      TINYINT     NOT NULL DEFAULT 1,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '商品分类';

-- 商品表
CREATE TABLE product (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    category_id     BIGINT        NOT NULL,
    name            VARCHAR(40)   NOT NULL COMMENT '商品名',
    subtitle        VARCHAR(60)   DEFAULT NULL COMMENT '副标题',
    description     TEXT          COMMENT '商品描述',
    price           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    original_price  DECIMAL(10,2) DEFAULT NULL,
    image           VARCHAR(255)  DEFAULT NULL COMMENT '主图',
    images          TEXT          COMMENT '图集，逗号分隔',
    tags            VARCHAR(100)  DEFAULT NULL COMMENT '标签，逗号分隔',
    origin          VARCHAR(60)   DEFAULT NULL COMMENT '产地',
    process         VARCHAR(80)   DEFAULT NULL COMMENT '工艺',
    temperature     VARCHAR(50)   DEFAULT NULL COMMENT '温度，逗号分隔',
    rating          DECIMAL(2,1)  NOT NULL DEFAULT 5.0,
    sales           INT           NOT NULL DEFAULT 0,
    stock           INT           NOT NULL DEFAULT 999,
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '1 上架 0 下架',
    recommended     TINYINT       NOT NULL DEFAULT 0 COMMENT '1 推荐',
    sort            INT           NOT NULL DEFAULT 0,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_product_category (category_id),
    KEY idx_product_status (status)
) ENGINE = InnoDB COMMENT = '咖啡商品';

-- 商品规格
CREATE TABLE product_spec (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    product_id  BIGINT        NOT NULL,
    name        VARCHAR(30)   NOT NULL COMMENT '规格名',
    price_delta DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '加价',
    sort        INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_spec_product (product_id)
) ENGINE = InnoDB COMMENT = '商品规格';

-- 购物车
CREATE TABLE cart_item (
    id          BIGINT    NOT NULL AUTO_INCREMENT,
    user_id     BIGINT    NOT NULL,
    product_id  BIGINT    NOT NULL,
    spec_id     BIGINT    DEFAULT NULL,
    spec_name   VARCHAR(30) DEFAULT NULL,
    quantity    INT       NOT NULL DEFAULT 1,
    created_at  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_cart_user (user_id)
) ENGINE = InnoDB COMMENT = '购物车';

-- 订单
CREATE TABLE orders (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    order_no     VARCHAR(32)   NOT NULL COMMENT '订单号',
    user_id      BIGINT        NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    item_count   INT           NOT NULL DEFAULT 0,
    status       TINYINT       NOT NULL DEFAULT 0 COMMENT '0 待制作 1 制作中 2 待取餐 3 已完成 4 已取消',
    remark       VARCHAR(100)  DEFAULT NULL,
    pickup_code  VARCHAR(8)    DEFAULT NULL COMMENT '取餐号',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user (user_id),
    KEY idx_order_status (status),
    KEY idx_order_created (created_at)
) ENGINE = InnoDB COMMENT = '订单';

-- 订单明细（快照）
CREATE TABLE order_item (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    order_id      BIGINT        NOT NULL,
    product_id    BIGINT        NOT NULL,
    product_name  VARCHAR(40)   NOT NULL,
    product_image VARCHAR(255)  DEFAULT NULL,
    spec_name     VARCHAR(30)   DEFAULT NULL,
    price         DECIMAL(10,2) NOT NULL,
    quantity      INT           NOT NULL,
    PRIMARY KEY (id),
    KEY idx_item_order (order_id)
) ENGINE = InnoDB COMMENT = '订单明细';

-- 评价
CREATE TABLE review (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    product_id  BIGINT       NOT NULL,
    nickname    VARCHAR(40)  NOT NULL,
    rating      TINYINT      NOT NULL DEFAULT 5,
    content     VARCHAR(200) NOT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1 展示 0 隐藏',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_review_product (product_id),
    KEY idx_review_user (user_id)
) ENGINE = InnoDB COMMENT = '评价';

-- ============================================================
-- 种子数据
-- ============================================================

-- 管理员: admin / admin123
-- 演示用户: 13800000000 / 123456
INSERT INTO sys_user (id, nickname, phone, password, role, status, created_at) VALUES
(1, '咖啡厅主理人', 'admin', '$2y$10$znDYXyTXFpoK1tnFXqylU.P8i1qNTLKUBswU4QTI15Sz7A5Dy.hGG', 'ADMIN', 1, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(2, '林小满', '13800000000', '$2y$10$qqR3.KUjETupiTfnfXSnwOp1.MeZ537qr7qiq8hI1I0UOSnDEupXO', 'USER', 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3, '陈屿', '13800000001', '$2y$10$qqR3.KUjETupiTfnfXSnwOp1.MeZ537qr7qiq8hI1I0UOSnDEupXO', 'USER', 1, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(4, '阮青禾', '13800000002', '$2y$10$qqR3.KUjETupiTfnfXSnwOp1.MeZ537qr7qiq8hI1I0UOSnDEupXO', 'USER', 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(5, '周予安', '13800000003', '$2y$10$qqR3.KUjETupiTfnfXSnwOp1.MeZ537qr7qiq8hI1I0UOSnDEupXO', 'USER', 1, DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO category (id, name, name_en, icon, sort, status) VALUES
(1, '越南', 'VIETNAM', 'VN', 1, 1),
(2, '泰国', 'THAILAND', 'TH', 2, 1),
(3, '印尼', 'INDONESIA', 'ID', 3, 1),
(4, '马来', 'MALAYSIA', 'MY', 4, 1),
(5, '新加坡', 'SINGAPORE', 'SG', 5, 1),
(6, '菲律宾', 'PHILIPPINES', 'PH', 6, 1);

INSERT INTO product (id, category_id, name, subtitle, description, price, original_price, image, images, tags, origin, process, temperature, rating, sales, stock, status, recommended, sort) VALUES
(1, 1, '越南蛋咖啡', 'Cà phê trứng · 河内街头的金色云朵', '传统越南滴漏咖啡，使用越南本土罗布斯塔豆，搭配新鲜蛋黄与炼乳手工打发出绵密蛋奶泡沫。入口丝滑醇厚，甜而不腻，是河内街头最具代表性的咖啡体验。', 28.00, 32.00, '/images/coffee-1.jpg', '/images/coffee-1.jpg', '招牌,越南', '越南 · 岘港', '越南滴漏壶萃取 + 蛋黄奶沫', '热饮,冰饮', 4.9, 328, 200, 1, 1, 1),
(2, 2, '泰式冰咖啡', 'Oliang · 清迈午后的炼乳香气', '泰国国民级冰咖啡，深烘焙泰国豆搭配炼乳，加入小豆蔻等泰式香料，浓郁香甜又清凉解暑。冰块的凉意与炼乳的绵密交织，是热带午后最好的注脚。', 25.00, 28.00, '/images/coffee-2.jpg', '/images/coffee-2.jpg', '热销,泰国', '泰国 · 清迈', '深烘焙 + 炼乳 + 冰块', '冰饮', 4.7, 286, 200, 1, 1, 2),
(3, 3, '苏门答腊麝香猫', 'Kopi Luwak · 雨林深处的稀世醇香', '世界顶级咖啡之一，产自印尼苏门答腊岛。麝香猫精选咖啡樱桃后经自然发酵处理，口感圆润醇厚，带有独特的木质与焦糖香气，回味悠长。', 58.00, 68.00, '/images/coffee-3.jpg', '/images/coffee-3.jpg', '珍品,印尼', '印尼 · 苏门答腊', '湿法处理 + 自然发酵', '热饮', 4.9, 96, 60, 1, 1, 3),
(4, 4, '南洋白咖啡', 'Kopi Putih · 槟城老店的怀旧奶香', '马来西亚经典 Kopi，源自槟城传统咖啡店。以淡奶替代炼乳，用拉茶手法冲泡，口感顺滑不涩、奶香四溢，是南洋华人记忆里的旧时光。', 22.00, 26.00, '/images/coffee-4.jpg', '/images/coffee-4.jpg', '经典,马来', '马来西亚 · 槟城', '中度烘焙 + 淡奶冲泡', '热饮,冰饮', 4.6, 254, 200, 1, 1, 4),
(5, 5, '新加坡Kopi-C', 'Kopi-C · 老咖啡店里的淡奶哲学', '新加坡传统咖啡，C 代表加入淡奶（Carnation）。源自旧式咖啡店 Kopitiam，黑咖啡与淡奶的经典搭配，奶香浓郁、口感清爽。', 24.00, 28.00, '/images/coffee-5.jpg', '/images/coffee-5.jpg', '人气,新加坡', '新加坡', '传统炭火烘焙 + 淡奶', '热饮,冰饮', 4.5, 198, 200, 1, 0, 5),
(6, 6, '菲律宾巴拉克咖啡', 'Kapeng Barako · 八打雁的浓烈坚果香', '菲律宾 Batangas 省出产的精品咖啡，使用 Liberica 品种豆。口味浓烈醇厚，带有独特的坚果与可可风味，是菲律宾最具代表性的本土咖啡。', 20.00, 24.00, '/images/coffee-6.jpg', '/images/coffee-6.jpg', '特色,菲律宾', '菲律宾 · 八打雁', '中度烘焙 + 蜂蜜处理', '热饮', 4.4, 172, 180, 1, 0, 6);

INSERT INTO product_spec (product_id, name, price_delta, sort) VALUES
(1, '热饮 标准杯', 0.00, 1),
(1, '冰饮 大杯', 3.00, 2),
(2, '标准杯 355ml', 0.00, 1),
(2, '大杯 473ml', 4.00, 2),
(3, '品鉴杯 180ml', 0.00, 1),
(3, '标准杯 237ml', 12.00, 2),
(4, '标准杯 355ml', 0.00, 1),
(4, '大杯 473ml', 3.00, 2),
(5, '标准杯 355ml', 0.00, 1),
(5, '大杯 473ml', 3.00, 2),
(6, '标准杯 355ml', 0.00, 1),
(6, '大杯 473ml', 3.00, 2);

INSERT INTO review (user_id, product_id, nickname, rating, content, status, created_at) VALUES
(2, 1, '林小满', 5, '蛋奶泡像云朵一样绵密，配上滴漏咖啡的苦，甜苦平衡得刚刚好。', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 1, '陈屿', 4, '风味很特别，对我来说稍微偏甜，但配一块可颂刚刚好。', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 4, '阮青禾', 5, '奶香很干净，不涩口，喝完杯底还有淡淡坚果味。', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, '林小满', 5, '清迈的味道，香料味很轻，冰镇后特别舒服。', 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 3, '陈屿', 5, '确实值这个价，木质和焦糖的尾韵很长，适合慢慢喝。', 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 5, '阮青禾', 4, '很顺口，下午犯困的时候来一杯正好。', 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 2, '周予安', 4, '甜度比想象中低一点，冰块少了会更好。', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 6, '周予安', 4, '浓烈但不上头，配甜品很合适。', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 1, '阮青禾', 2, '这次出品有点凉了，蛋泡也塌了。', 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO orders (id, order_no, user_id, total_amount, item_count, status, remark, pickup_code, created_at) VALUES
(1001, 'GX20261017093012001', 2, 28.00, 1, 3, '少糖', '2861', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1002, 'GX20261018101523002', 2, 53.00, 2, 3, NULL, '3194', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1003, 'GX20261019140244003', 3, 58.00, 1, 3, '堂食', '4527', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1004, 'GX20261020162135004', 2, 50.00, 2, 3, NULL, '6083', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1005, 'GX20261021110346005', 4, 47.00, 2, 3, '两杯都要冰的', '7315', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1006, 'GX20261022153057006', 2, 80.00, 3, 3, NULL, '8429', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1007, 'GX20261023100118007', 3, 58.00, 1, 2, '请帮我温一下杯', '9156', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1008, 'GX20261023104529008', 2, 25.00, 1, 1, '少冰', '9632', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1009, 'GX20261023111830009', 4, 22.00, 1, 0, NULL, '1784', DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
(1010, 'GX20261021182041010', 2, 20.00, 1, 4, '临时有事', '2053', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO order_item (order_id, product_id, product_name, product_image, spec_name, price, quantity) VALUES
(1001, 1, '越南蛋咖啡', '/images/coffee-1.jpg', '热饮 标准杯', 28.00, 1),
(1002, 1, '越南蛋咖啡', '/images/coffee-1.jpg', '热饮 标准杯', 28.00, 1),
(1002, 2, '泰式冰咖啡', '/images/coffee-2.jpg', '标准杯 355ml', 25.00, 1),
(1003, 3, '苏门答腊麝香猫', '/images/coffee-3.jpg', '品鉴杯 180ml', 58.00, 1),
(1004, 2, '泰式冰咖啡', '/images/coffee-2.jpg', '标准杯 355ml', 25.00, 2),
(1005, 2, '泰式冰咖啡', '/images/coffee-2.jpg', '标准杯 355ml', 25.00, 1),
(1005, 4, '南洋白咖啡', '/images/coffee-4.jpg', '标准杯 355ml', 22.00, 1),
(1006, 1, '越南蛋咖啡', '/images/coffee-1.jpg', '热饮 标准杯', 28.00, 2),
(1006, 5, '新加坡Kopi-C', '/images/coffee-5.jpg', '标准杯 355ml', 24.00, 1),
(1007, 3, '苏门答腊麝香猫', '/images/coffee-3.jpg', '品鉴杯 180ml', 58.00, 1),
(1008, 2, '泰式冰咖啡', '/images/coffee-2.jpg', '标准杯 355ml', 25.00, 1),
(1009, 4, '南洋白咖啡', '/images/coffee-4.jpg', '标准杯 355ml', 22.00, 1),
(1010, 6, '菲律宾巴拉克咖啡', '/images/coffee-6.jpg', '标准杯 355ml', 20.00, 1);
