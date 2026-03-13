-- Groups
INSERT INTO groups (id, name, description, icon) VALUES ('group-combos', 'Combos', 'Complete meals with drinks', 'fas fa-layer-group');
INSERT INTO groups (id, name, description, icon) VALUES ('group-food', 'Food', 'Main dishes and snacks', 'fas fa-hamburger');
INSERT INTO groups (id, name, description, icon) VALUES ('group-drinks', 'Drinks', 'Refreshing beverages', 'fas fa-wine-glass');

-- Customizations
INSERT INTO customizations (id, name, type, usage_count) VALUES ('cust-sauce', 'Sauce', 'radio', 0);
INSERT INTO customizations (id, name, type, usage_count) VALUES ('cust-toppings', 'Extra Toppings', 'checkbox', 0);

-- Customization Options (Sauce)
INSERT INTO customization_options (id, customization_id, name, price, is_selected_by_default, default_value, option_index) VALUES ('cust-sauce-opt-0', 'cust-sauce', 'Garlic Sauce', 0.00, true, 1, 0);
INSERT INTO customization_options (id, customization_id, name, price, is_selected_by_default, default_value, option_index) VALUES ('cust-sauce-opt-1', 'cust-sauce', 'Spicy Sauce', 0.00, false, 0, 1);
INSERT INTO customization_options (id, customization_id, name, price, is_selected_by_default, default_value, option_index) VALUES ('cust-sauce-opt-2', 'cust-sauce', 'Yogurt Sauce', 0.00, false, 0, 2);

-- Customization Options (Toppings)
INSERT INTO customization_options (id, customization_id, name, price, is_selected_by_default, default_value, option_index) VALUES ('cust-toppings-opt-0', 'cust-toppings', 'Extra Cheese', 0.50, false, 0, 0);
INSERT INTO customization_options (id, customization_id, name, price, is_selected_by_default, default_value, option_index) VALUES ('cust-toppings-opt-1', 'cust-toppings', 'Extra Bacon', 0.80, false, 0, 1);

-- Products (Combos)
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-combo-kebab-l', 'Kebab Large Combo', 12.50, 'Large Kebab with fries and drink', '/images/combo/combo-kebab-l.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-combo-kebab-m', 'Kebab Medium Combo', 10.50, 'Medium Kebab with fries and drink', '/images/combo/combo-kebab-m.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-combo-kebab-s', 'Kebab Small Combo', 8.50, 'Small Kebab with fries and drink', '/images/combo/combo-kebab-s.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-combo-pita', 'Pita Combo', 11.00, 'Pita bread sandwich with fries and drink', '/images/combo/combo-pita.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-combo-pizza', 'Pizza Mozzarella Combo', 13.00, 'Personal pizza with fries and drink', '/images/combo/combo-pizza-mozzarella.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-combo-box', 'Combo Box', 14.50, 'The ultimate meal in a box', '/images/combo/combo-box.png', true);

-- Products (Food)
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-food-box', 'Food Box', 9.50, 'Meal in a box', '/images/food/food-box.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-food-empanadillas', 'Empanadillas', 4.50, '3 pieces of crispy empanadillas', '/images/food/food-empanadillas.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-food-kebab-l', 'Kebab Large', 8.00, 'Large sized Kebab', '/images/food/food-kebab-l.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-food-kebab-m', 'Kebab Medium', 6.50, 'Medium sized Kebab', '/images/food/food-kebab-m.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-food-kebab-s', 'Kebab Small', 5.00, 'Small sized Kebab', '/images/food/food-kebab-s.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-food-pita', 'Pita', 7.00, 'Classic Pita bread sandwich', '/images/food/food-pita.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-food-pizza', 'Pizza Mozzarella', 9.00, 'Classic personal Pizza Mozzarella', '/images/food/food-pizza-mozzarella.png', true);

-- Products (Drinks)
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-drink-coca-cola', 'Coca-Cola', 2.50, '330ml can', '/images/drink/coca-cola.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-drink-fanta-limon', 'Fanta Limon', 2.50, '330ml can', '/images/drink/fanta-limon.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-drink-fanta-naranja', 'Fanta Naranja', 2.50, '330ml can', '/images/drink/fanta-naranja.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-drink-sprite', 'Sprite', 2.50, '330ml can', '/images/drink/sprite.png', true);
INSERT INTO products (id, name, price, description, image, active) VALUES ('prod-drink-water', 'Water', 1.50, '500ml bottle', '/images/drink/water.png', true);

-- Group Products
INSERT INTO group_products (group_id, product_id) VALUES ('group-combos', 'prod-combo-kebab-l');
INSERT INTO group_products (group_id, product_id) VALUES ('group-combos', 'prod-combo-kebab-m');
INSERT INTO group_products (group_id, product_id) VALUES ('group-combos', 'prod-combo-kebab-s');
INSERT INTO group_products (group_id, product_id) VALUES ('group-combos', 'prod-combo-pita');
INSERT INTO group_products (group_id, product_id) VALUES ('group-combos', 'prod-combo-pizza');
INSERT INTO group_products (group_id, product_id) VALUES ('group-combos', 'prod-combo-box');

INSERT INTO group_products (group_id, product_id) VALUES ('group-food', 'prod-food-box');
INSERT INTO group_products (group_id, product_id) VALUES ('group-food', 'prod-food-empanadillas');
INSERT INTO group_products (group_id, product_id) VALUES ('group-food', 'prod-food-kebab-l');
INSERT INTO group_products (group_id, product_id) VALUES ('group-food', 'prod-food-kebab-m');
INSERT INTO group_products (group_id, product_id) VALUES ('group-food', 'prod-food-kebab-s');
INSERT INTO group_products (group_id, product_id) VALUES ('group-food', 'prod-food-pita');
INSERT INTO group_products (group_id, product_id) VALUES ('group-food', 'prod-food-pizza');

INSERT INTO group_products (group_id, product_id) VALUES ('group-drinks', 'prod-drink-coca-cola');
INSERT INTO group_products (group_id, product_id) VALUES ('group-drinks', 'prod-drink-fanta-limon');
INSERT INTO group_products (group_id, product_id) VALUES ('group-drinks', 'prod-drink-fanta-naranja');
INSERT INTO group_products (group_id, product_id) VALUES ('group-drinks', 'prod-drink-sprite');
INSERT INTO group_products (group_id, product_id) VALUES ('group-drinks', 'prod-drink-water');

-- Product Customizations
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-combo-kebab-l', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-combo-kebab-m', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-combo-kebab-s', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-combo-pita', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-combo-box', 'cust-sauce');

INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-food-box', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-food-kebab-l', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-food-kebab-m', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-food-kebab-s', 'cust-sauce');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-food-pita', 'cust-sauce');

INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-combo-pizza', 'cust-toppings');
INSERT INTO product_customizations (product_id, customization_id) VALUES ('prod-food-pizza', 'cust-toppings');

-- Tables
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000001', 'Table 1', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000002', 'Table 2', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000003', 'Table 3', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000004', 'Table 4', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000005', 'Table 5', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000006', 'Table 6', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000007', 'Table 7', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000008', 'Table 8', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000009', 'Table 9', 4, 'AVAILABLE', TRUE);
INSERT INTO dining_tables (id, name, seats, status, active) VALUES ('00000000-0000-0000-0000-000000000010', 'Table 10', 4, 'AVAILABLE', TRUE);

-- Payment Configuration
DELETE FROM money_denominations WHERE config_id = 'default';
DELETE FROM payment_modes WHERE config_id = 'default';
DELETE FROM payment_configs WHERE id = 'default';

INSERT INTO payment_configs (id, active) VALUES ('default', TRUE);

-- Payment Modes
INSERT INTO payment_modes (config_id, mode) VALUES ('default', 'CASH');
INSERT INTO payment_modes (config_id, mode) VALUES ('default', 'CARD');

-- Money Denominations
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 50.00, '/images/money/1771142914327_euros50.png', 'BANKNOTE');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 20.00, '/images/money/1771142935336_euros20.jpg', 'BANKNOTE');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 10.00, '/images/money/1771142947210_diezEuros.png', 'BANKNOTE');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 5.00, '/images/money/1771142963956_euros5.gif', 'BANKNOTE');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 2.00, '/images/money/1771142976041_dosEuros.png', 'COIN');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 1.00, '/images/money/1771142992632_unEuro.png', 'COIN');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 0.50, '/images/money/1771143001895_cincuentaCentimos.png', 'COIN');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 0.20, '/images/money/1771143013372_vinteCentimos.png', 'COIN');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 0.10, '/images/money/1771143022884_diezCentimo.png', 'COIN');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 0.05, '/images/money/1771143035854_cincoCentimo.png', 'COIN');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 0.02, '/images/money/1771143044590_dosCentimos.png', 'COIN');
INSERT INTO money_denominations (config_id, denomination_value, image, type) VALUES ('default', 0.01, '/images/money/1771143060155_unCentimo.png', 'COIN');

-- Group Translations
INSERT INTO group_translations (id, group_id, language, name, description) VALUES ('gt-combos-es', 'group-combos', 'es', 'Combinados', 'Menús completos con bebida');
INSERT INTO group_translations (id, group_id, language, name, description) VALUES ('gt-combos-pt', 'group-combos', 'pt', 'Combinados', 'Refeições completas com bebida');
INSERT INTO group_translations (id, group_id, language, name, description) VALUES ('gt-food-es', 'group-food', 'es', 'Comida', 'Platos principales y aperitivos');
INSERT INTO group_translations (id, group_id, language, name, description) VALUES ('gt-food-pt', 'group-food', 'pt', 'Comida', 'Pratos principais e lanches');
INSERT INTO group_translations (id, group_id, language, name, description) VALUES ('gt-drinks-es', 'group-drinks', 'es', 'Bebidas', 'Refrescos y agua');
INSERT INTO group_translations (id, group_id, language, name, description) VALUES ('gt-drinks-pt', 'group-drinks', 'pt', 'Bebidas', 'Refrigerantes e água');

-- Product Translations (Selection)
INSERT INTO product_translations (id, product_id, language, name, description) VALUES ('pt-combo-kebab-l-es', 'prod-combo-kebab-l', 'es', 'Menú Kebab Grande', 'Kebab grande con patatas y bebida');
INSERT INTO product_translations (id, product_id, language, name, description) VALUES ('pt-combo-kebab-l-pt', 'prod-combo-kebab-l', 'pt', 'Combo Kebab Grande', 'Kebab grande com batatas e bebida');
INSERT INTO product_translations (id, product_id, language, name, description) VALUES ('pt-food-empanadillas-es', 'prod-food-empanadillas', 'es', 'Empanadillas', '3 unidades de empanadillas crujientes');
INSERT INTO product_translations (id, product_id, language, name, description) VALUES ('pt-food-empanadillas-pt', 'prod-food-empanadillas', 'pt', 'Empanadilhas', '3 unidades de empanadilhas crocantes');

-- Customization Translations
INSERT INTO customization_translations (id, customization_id, language, name) VALUES ('ct-sauce-es', 'cust-sauce', 'es', 'Salsa');
INSERT INTO customization_translations (id, customization_id, language, name) VALUES ('ct-sauce-pt', 'cust-sauce', 'pt', 'Molho');
INSERT INTO customization_translations (id, customization_id, language, name) VALUES ('ct-toppings-es', 'cust-toppings', 'es', 'Ingredientes Extra');
INSERT INTO customization_translations (id, customization_id, language, name) VALUES ('ct-toppings-pt', 'cust-toppings', 'pt', 'Ingredientes Extras');

-- Customization Option Translations
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-sauce-0-es', 'cust-sauce-opt-0', 'es', 'Salsa de Ajo');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-sauce-0-pt', 'cust-sauce-opt-0', 'pt', 'Molho de Alho');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-sauce-1-es', 'cust-sauce-opt-1', 'es', 'Salsa Picante');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-sauce-1-pt', 'cust-sauce-opt-1', 'pt', 'Molho Picante');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-sauce-2-es', 'cust-sauce-opt-2', 'es', 'Salsa de Yogur');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-sauce-2-pt', 'cust-sauce-opt-2', 'pt', 'Molho de Iogurte');

INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-toppings-0-es', 'cust-toppings-opt-0', 'es', 'Extra de Queso');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-toppings-0-pt', 'cust-toppings-opt-0', 'pt', 'Queijo Extra');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-toppings-1-es', 'cust-toppings-opt-1', 'es', 'Extra de Bacon');
INSERT INTO customization_option_translations (id, customization_option_id, language, name) VALUES ('cot-toppings-1-pt', 'cust-toppings-opt-1', 'pt', 'Bacon Extra');
