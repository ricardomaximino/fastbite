import json
import os

template_data = {
    "id": "michelin",
    "icon": "⭐",
    "version": 1,
    "name": "Haute Cuisine & Degustation",
    "description": "5-star Michelin fine dining, exquisite tasting menus, wine pairings and haute pastry.",
    "translations": {
        "es": {
            "name": "Alta Cocina & Degustación",
            "description": "Restaurante gastronómico 5 estrellas Michelin, menús degustación, maridajes de autor y alta repostería."
        },
        "pt": {
            "name": "Alta Cozinha & Degustação",
            "description": "Restaurante gastronómico de autor 5 estrelas Michelin, menus degustação, harmonização de vinhos e alta pastelaria."
        }
    }
}

with open("build_michelin_demo/template.json", "w", encoding="utf-8") as f:
    json.dump(template_data, f, indent=2, ensure_ascii=False)

# Customizations raw definition
customizations_def = [
    {
        "id": "cust-michelin-doneness",
        "name": "Meat Doneness",
        "type": "radio",
        "translations": {
            "es": "Punto de Cocción",
            "pt": "Ponto da Carne"
        },
        "options": [
            {
                "id": "opt-done-rare",
                "name": "Rare (Poco Hecho)",
                "price": 0.0,
                "defaultValue": 0,
                "selectedByDefault": False,
                "optionIndex": 0,
                "translations": {"es": "Poco hecho (Sangrante)", "pt": "Mal passado"}
            },
            {
                "id": "opt-done-medium-rare",
                "name": "Medium Rare (Al Punto)",
                "price": 0.0,
                "defaultValue": 1,
                "selectedByDefault": True,
                "optionIndex": 1,
                "translations": {"es": "Al punto (Recomendado del chef)", "pt": "Ao ponto (Recomendado do chef)"}
            },
            {
                "id": "opt-done-medium-well",
                "name": "Medium Well (Hecho)",
                "price": 0.0,
                "defaultValue": 0,
                "selectedByDefault": False,
                "optionIndex": 2,
                "translations": {"es": "Hecho", "pt": "Bem passado"}
            }
        ]
    },
    {
        "id": "cust-michelin-luxury-extras",
        "name": "Luxury Additions",
        "type": "checkbox",
        "translations": {
            "es": "Suplementos de Alta Gastronomía",
            "pt": "Suplementos de Alta Gastronomia"
        },
        "options": [
            {
                "id": "opt-extra-caviar",
                "name": "Oscietra Royal Caviar (10g)",
                "price": 18.0,
                "defaultValue": 0,
                "selectedByDefault": False,
                "optionIndex": 0,
                "translations": {"es": "Caviar Imperial Oscietra (10g)", "pt": "Caviar Imperial Oscietra (10g)"}
            },
            {
                "id": "opt-extra-truffle",
                "name": "Fresh Melanosporum Black Truffle Shavings",
                "price": 14.0,
                "defaultValue": 0,
                "selectedByDefault": False,
                "optionIndex": 1,
                "translations": {"es": "Láminas de trufa negra fresca", "pt": "Lâminas de trufa negra fresca"}
            },
            {
                "id": "opt-extra-foie",
                "name": "Pan-seared Duck Foie Gras Escalope",
                "price": 12.0,
                "defaultValue": 0,
                "selectedByDefault": False,
                "optionIndex": 2,
                "translations": {"es": "Escalope de Foie Gras a la plancha", "pt": "Escalope de Foie Gras grelhado"}
            }
        ]
    },
    {
        "id": "cust-michelin-pairing",
        "name": "Harmonized Wine Pairing",
        "type": "radio",
        "translations": {
            "es": "Armonía de Vinos",
            "pt": "Harmonização de Vinhos"
        },
        "options": [
            {
                "id": "opt-pairing-none",
                "name": "Without Wine Pairing",
                "price": 0.0,
                "defaultValue": 1,
                "selectedByDefault": True,
                "optionIndex": 0,
                "translations": {"es": "Sin maridaje de vinos", "pt": "Sem harmonização de vinhos"}
            },
            {
                "id": "opt-pairing-national",
                "name": "Spanish Terroirs & Jewels Pairing",
                "price": 65.0,
                "defaultValue": 0,
                "selectedByDefault": False,
                "optionIndex": 1,
                "translations": {"es": "Maridaje Grandes Pagos de España", "pt": "Harmonização Grandes Vinhos de Espanha"}
            },
            {
                "id": "opt-pairing-prestige",
                "name": "Prestige International Grand Cru Pairing",
                "price": 115.0,
                "defaultValue": 0,
                "selectedByDefault": False,
                "optionIndex": 2,
                "translations": {"es": "Maridaje Prestige Grand Cru Internacional", "pt": "Harmonização Prestige Grand Cru Internacional"}
            }
        ]
    }
]

customizations_list = []
customization_options_list = []
customization_translations = []
customization_option_translations = []

for c in customizations_def:
    opts_clean = []
    for opt in c["options"]:
        opt_entity = {
            "id": opt["id"],
            "name": opt["name"],
            "price": opt["price"],
            "defaultValue": opt["defaultValue"],
            "optionIndex": opt["optionIndex"],
            "selectedByDefault": opt["selectedByDefault"]
        }
        opts_clean.append(opt_entity)
        
        flat_opt = {
            "id": opt["id"],
            "customization": {
                "id": c["id"],
                "name": c["name"],
                "type": c["type"],
                "usageCount": 0
            },
            "name": opt["name"],
            "price": opt["price"],
            "defaultValue": opt["defaultValue"],
            "optionIndex": opt["optionIndex"],
            "selectedByDefault": opt["selectedByDefault"]
        }
        customization_options_list.append(flat_opt)
        
        for lang, trans in opt["translations"].items():
            customization_option_translations.append({
                "id": f"cot-{opt['id']}-{lang}",
                "customizationOption": {"id": opt["id"]},
                "language": lang,
                "name": trans
            })
            
    c_entity = {
        "id": c["id"],
        "name": c["name"],
        "type": c["type"],
        "usageCount": 0,
        "options": opts_clean
    }
    customizations_list.append(c_entity)
    
    for lang, trans_name in c["translations"].items():
        customization_translations.append({
            "id": f"ct-{c['id']}-{lang}",
            "customization": {"id": c["id"]},
            "language": lang,
            "name": trans_name
        })

# Products definition
products_def = [
    {
        "id": "prod-michelin-tartar-atun",
        "name": "Balfegó Bluefin Tuna Tartare & Oscietra Caviar",
        "description": "Wild Mediterranean bluefin tuna, yuzu kosho emulsion, Oscietra caviar and crispy nori coral.",
        "price": 32.0,
        "image": "/user-images/kebab/michelin/tartar-atun.webp",
        "customizations": ["cust-michelin-luxury-extras"],
        "translations": {
            "es": ("Tartar de Atún Rojo Balfegó & Caviar Oscietra", "Atún rojo Balfegó salvaje, emulsión de yuzu kosho, caviar imperial Oscietra y coral crujiente de alga nori."),
            "pt": ("Tártaro de Atum Rabilho Balfegó & Caviar Oscietra", "Atum rabilho selvagem, emulsão de yuzu kosho, caviar imperial Oscietra e crocante de nori.")
        }
    },
    {
        "id": "prod-michelin-carpaccio-wagyu",
        "name": "A5 Wagyu Kagoshima Carpaccio with Melanosporum Truffle",
        "description": "Delicate slices of A5 Wagyu, 36-month aged Parmigiano Reggiano crisps and winter black truffle.",
        "price": 36.0,
        "image": "/user-images/kebab/michelin/carpaccio-wagyu.webp",
        "customizations": ["cust-michelin-luxury-extras"],
        "translations": {
            "es": ("Carpaccio de Wagyu A5 Kagoshima & Trufa Negra", "Finas láminas de Wagyu A5 de Kagoshima, crujientes de Parmesano 36 meses y trufa negra melanosporum."),
            "pt": ("Carpaccio de Wagyu A5 Kagoshima & Trufa Negra", "Finas fatias de Wagyu A5 de Kagoshima, crocante de Parmesão 36 meses e trufa negra de inverno.")
        }
    },
    {
        "id": "prod-michelin-vieira-braseada",
        "name": "Pan-Seared King Scallop & Cauliflower Silk",
        "description": "Caramelized King Scallop, velvety truffled cauliflower purée and Iberian acorn-fed ham crunch.",
        "price": 28.0,
        "image": "/user-images/kebab/michelin/vieira-braseada.webp",
        "customizations": ["cust-michelin-luxury-extras"],
        "translations": {
            "es": ("Vieira Braseada sobre Seda de Coliflor Trufada", "Vieira de concha caramelizada, puré sedoso de coliflor a la trufa y velo crujiente de jamón ibérico de bellota."),
            "pt": ("Vieira Braseada com Creme Suave de Couve-Flor Trufada", "Vieira caramelizada, puré aveludado de couve-flor trufada e estaladiço de presunto ibérico de bolota.")
        }
    },
    {
        "id": "prod-michelin-lubina-salvaje",
        "name": "Wild Atlantic Sea Bass with Marine Plankton Emulsion",
        "description": "Line-caught sea bass roasted on charcoal, seaweed reduction, marine plankton froth and glasswort.",
        "price": 42.0,
        "image": "/user-images/kebab/michelin/lubina-salvaje.webp",
        "customizations": ["cust-michelin-luxury-extras"],
        "translations": {
            "es": ("Lubina Salvaje a la Brasa con Emulsión de Plancton Marino", "Lubina de anzuelo asada a la brasa de encina, fondo marino de algas, emulsión de fitoplancton y salicornia fresca."),
            "pt": ("Robalo Selvagem na Brasa com Emulsão de Plâncton Marinho", "Robalo de linha grelhado na brasa, caldo rico de algas marinhas, emulsão de plâncton e salicórnia.")
        }
    },
    {
        "id": "prod-michelin-bogavante-azul",
        "name": "Roasted European Blue Lobster with Coral Bisque",
        "description": "Brittany blue lobster roasted in brown butter noisette, saffron lobster bisque and tender sea succulents.",
        "price": 54.0,
        "image": "/user-images/kebab/michelin/bogavante-azul.webp",
        "customizations": ["cust-michelin-luxury-extras"],
        "translations": {
            "es": ("Bogavante Azul Asado con Bisque de sus Corales", "Bogavante azul asado a la mantequilla noisette, bisque concentrado de sus propios corales al azafrán."),
            "pt": ("Lavagante Azul Assado com Bisque dos Seus Corais", "Lavagante azul assado com manteiga noisette, bisque perfumado de açafrão e suculentas marinhas.")
        }
    },
    {
        "id": "prod-michelin-solomillo-rubia",
        "name": "Aged Rubia Gallega Beef Tenderloin & Robuchon Purée",
        "description": "60-day dry-aged Galician beef tenderloin, silky Robuchon potato purée, glazed shallots and Priorat wine jus.",
        "price": 48.0,
        "image": "/user-images/kebab/michelin/solomillo-rubia-gallega.webp",
        "customizations": ["cust-michelin-doneness", "cust-michelin-luxury-extras"],
        "translations": {
            "es": ("Solomillo de Vaca Rubia Gallega Madurada (60 Días)", "Solomillo de rubia gallega madurada a la brasa, puré de patata estilo Robuchon, chalotas glaseadas y reducción de vino Priorat."),
            "pt": ("Lombo de Vaca Rubia Galega Maturada (60 Dias)", "Lombo de vaca rubia galega grelhado na brasa, puré cremoso Robuchon, chalotas caramelizadas e redução de vinho Priorat.")
        }
    },
    {
        "id": "prod-michelin-pichon-bresse",
        "name": "Bresse Pigeon in Two Textures with Sour Cherries",
        "description": "Roasted supreme, confit leg, spiced sweet cherry reduction and pan-roasted escalope of duck foie gras.",
        "price": 44.0,
        "image": "/user-images/kebab/michelin/pichon-bresse.webp",
        "customizations": ["cust-michelin-doneness"],
        "translations": {
            "es": ("Pichón de Bresse en Dos Cocciones & Cerezas al Oporto", "Suprema asada sangrante, muslito confitado, reducción especiada de cerezas al oporto y medallón de foie gras."),
            "pt": ("Pombo de Bresse em Duas Cozeduras & Cerejas ao Vinho do Porto", "Suprema mal passada, coxa confitada, redução aromática de cerejas ao vinho do Porto e medalhão de foie gras.")
        }
    },
    {
        "id": "prod-michelin-menu-degustacion",
        "name": "Signature 8-Course Michelin Tasting Experience",
        "description": "Complete gastronomic journey designed by the executive chef. Includes bread service, amuse-bouches, and petit fours.",
        "price": 145.0,
        "image": "/user-images/kebab/michelin/menu-degustacion.webp",
        "customizations": ["cust-michelin-pairing"],
        "translations": {
            "es": ("Menú Degustación Signature (8 Pases)", "Experiencia gastronómica integral diseñada por el chef. Incluye servicio de panes artesanos, aperitivos de bienvenida y mignardises."),
            "pt": ("Menu Degustação Signature (8 Pratos)", "Experiência gastronómica completa criada pelo chef. Inclui serviço de pães artesanais, boas-vindas e petit fours.")
        }
    },
    {
        "id": "prod-michelin-esfera-chocolate",
        "name": "Valrhona Grand Cru Golden Chocolate Sphere",
        "description": "Guanaja 70% chocolate dome, molten passion fruit and mango core, hazelnut praline and warm chocolate ganache.",
        "price": 20.0,
        "image": "/user-images/kebab/michelin/esfera-chocolate.webp",
        "customizations": [],
        "translations": {
            "es": ("Esfera Dorada de Chocolate Valrhona Grand Cru", "Cúpula crujiente de chocolate Guanaja 70% con polvo de oro, núcleo fundente de maracuyá y praliné tostado de avellanas."),
            "pt": ("Esfera Dourada de Chocolate Valrhona Grand Cru", "Cúpula de chocolate Guanaja 70% com detalhes dourados, recheio líquido de maracujá e praliné de avelãs.")
        }
    },
    {
        "id": "prod-michelin-texturas-citricas",
        "name": "Mediterranean Citrus Textures with Basil Sorbet",
        "description": "Yuzu cloud, candied blood orange, calamansi gelée, and garden basil sorbet.",
        "price": 18.0,
        "image": "/user-images/kebab/michelin/texturas-citricas.webp",
        "customizations": [],
        "translations": {
            "es": ("Texturas de Cítricos Mediterráneos con Sorbete de Albahaca", "Nube de yuzu, naranja sanguina confitada, gelée de calamansí y sorbete artesanal de albahaca fresca del huerto."),
            "pt": ("Texturas de Cítricos Mediterrânicos com Sorvete de Manjericão", "Nuvem de yuzu, laranja sanguínea cristalizada, gelée de calamansi e sorvete fresco de manjericão.")
        }
    },
    {
        "id": "prod-michelin-coctel-ahumado",
        "name": "Smoked Truffle Old Fashioned",
        "description": "Aged Japanese whisky, black winter truffle essence, Angostura bitters and charred orange peel smoke.",
        "price": 22.0,
        "image": "/user-images/kebab/michelin/coctel-ahumado.webp",
        "customizations": [],
        "translations": {
            "es": ("Old Fashioned Ahumado a la Trufa Negra", "Whisky japonés reserva, infusión de trufa negra melanosporum, bíter aromático y humo de piel de naranja quemada."),
            "pt": ("Old Fashioned Fumado com Trufa Negra", "Whisky japonês envelhecido, infusão de trufa negra, bitters e fumo aromático de casca de laranja tostada.")
        }
    },
    {
        "id": "prod-michelin-champagne-cup",
        "name": "Champagne Dom Pérignon Vintage Brut (Glass)",
        "description": "Iconic vintage champagne served in crystal flute. Fine bubbles, toasted brioche and white peach notes.",
        "price": 38.0,
        "image": "/user-images/kebab/michelin/champagne-domperignon.webp",
        "customizations": [],
        "translations": {
            "es": ("Copa de Champagne Dom Pérignon Vintage Brut", "Copa del emblemático champagne millésimé en copa de cristal fino. Burbuja delicada, brioche tostado y notas minerales."),
            "pt": ("Taça de Champagne Dom Pérignon Vintage Brut", "Taça de prestígio do clássico millésimé em cristal fino. Bolha delicada, notas minerais e brioche tostado.")
        }
    }
]

products_list = []
product_translations = []

for p in products_def:
    products_list.append({
        "id": p["id"],
        "name": p["name"],
        "price": p["price"],
        "description": p["description"],
        "image": p["image"],
        "customizations": p["customizations"],
        "active": True
    })
    for lang, (trans_name, trans_desc) in p["translations"].items():
        product_translations.append({
            "id": f"pt-{p['id']}-{lang}",
            "product": {"id": p["id"]},
            "language": lang,
            "name": trans_name,
            "description": trans_desc
        })

# Groups definition
groups_def = [
    {
        "id": "grp-michelin-experience",
        "name": "Tasting Experiences",
        "description": "Curated culinary journeys celebrating terroir, technique and sensory emotion.",
        "icon": "⭐",
        "products": ["prod-michelin-menu-degustacion"],
        "translations": {
            "es": ("Menú Degustación Signature", "Recorridos gastronómicos completos que celebran la técnica, el producto y la emoción."),
            "pt": ("Menus de Degustação", "Percursos gastronómicos completos que celebram o produto e a alta técnica culinária.")
        }
    },
    {
        "id": "grp-michelin-starters",
        "name": "Amuse-Bouche & Starters",
        "description": "Delicate ocean gems and tartares to open the senses.",
        "icon": "🥢",
        "products": [
            "prod-michelin-tartar-atun",
            "prod-michelin-carpaccio-wagyu",
            "prod-michelin-vieira-braseada"
        ],
        "translations": {
            "es": ("Entrantes & Aperitivos de Autor", "Delicadas piezas de mar, carpaccios y tartares para abrir el apetito con excelencia."),
            "pt": ("Entradas & Aperitivos de Autor", "Delicadezas do mar, carpaccios e tártaros para abrir os sentidos.")
        }
    },
    {
        "id": "grp-michelin-mains",
        "name": "Haute Cuisine Mains (Sea & Land)",
        "description": "Wild charcoal-grilled catches and long-matured heritage meats.",
        "icon": "🥩",
        "products": [
            "prod-michelin-lubina-salvaje",
            "prod-michelin-bogavante-azul",
            "prod-michelin-solomillo-rubia",
            "prod-michelin-pichon-bresse"
        ],
        "translations": {
            "es": ("Platos Principales (Mar & Tierra)", "Pescados salvajes a la brasa, mariscos nobles y cortes de carnes maduradas excepcionales."),
            "pt": ("Pratos Principais (Mar & Terra)", "Peixes nobres da costa, lavagante azul e carnes maturadas de raça autóctone.")
        }
    },
    {
        "id": "grp-michelin-desserts",
        "name": "Haute Pastry & Desserts",
        "description": "Visual sculptures in chocolate and vibrant Mediterranean citrus.",
        "icon": "🍨",
        "products": [
            "prod-michelin-esfera-chocolate",
            "prod-michelin-texturas-citricas"
        ],
        "translations": {
            "es": ("Alta Repostería & Postres", "Esculturas en chocolate Grand Cru y composiciones cítricas refrescantes."),
            "pt": ("Alta Pastelaria & Sobremesas", "Esculturas em chocolate Grand Cru e composições cítricas requintadas.")
        }
    },
    {
        "id": "grp-michelin-cellar",
        "name": "Sommelier Cellar & Signatures",
        "description": "World-class champagnes, Grand Cru wines and artisanal cocktails.",
        "icon": "🍾",
        "products": [
            "prod-michelin-champagne-cup",
            "prod-michelin-coctel-ahumado"
        ],
        "translations": {
            "es": ("Bodega del Sommelier & Coctelería", "Champagnes emblemáticos por copa, grandes reservas y coctelería de autor ahumada."),
            "pt": ("Garrafeira do Sommelier & Cocktails", "Champagnes de prestígio em taça, vinhos premiados e cocktails de autor.")
        }
    }
]

groups_list = []
group_translations = []

for g in groups_def:
    groups_list.append({
        "id": g["id"],
        "name": g["name"],
        "description": g["description"],
        "icon": g["icon"],
        "products": g["products"]
    })
    for lang, (trans_name, trans_desc) in g["translations"].items():
        group_translations.append({
            "id": f"gt-{g['id']}-{lang}",
            "group": {"id": g["id"]},
            "language": lang,
            "name": trans_name,
            "description": trans_desc
        })

# Tables definition
tables_def = [
    ("00000000-0000-0000-0000-000000000001", "Table 1 (Main Hall)", 2, {"es": "Mesa 1 (Salón Principal)", "pt": "Mesa 1 (Salão Principal)"}),
    ("00000000-0000-0000-0000-000000000002", "Table 2 (Main Hall)", 4, {"es": "Mesa 2 (Salón Principal)", "pt": "Mesa 2 (Salão Principal)"}),
    ("00000000-0000-0000-0000-000000000003", "Table 3 (Main Hall)", 4, {"es": "Mesa 3 (Salón Principal)", "pt": "Mesa 3 (Salão Principal)"}),
    ("00000000-0000-0000-0000-000000000004", "Table 4 (Garden View)", 2, {"es": "Mesa 4 (Vistas al Jardín)", "pt": "Mesa 4 (Vista Jardim)"}),
    ("00000000-0000-0000-0000-000000000005", "Chef's Table (Kitchen Pass)", 6, {"es": "Mesa del Chef (Pase de Cocina)", "pt": "Mesa do Chef (Vista de Cozinha)"}),
    ("00000000-0000-0000-0000-000000000006", "Private VIP Salon", 8, {"es": "Reservado VIP", "pt": "Sala Privada VIP"})
]

tables_list = []
table_translations = []

for tid, name, seats, trans in tables_def:
    tables_list.append({
        "id": tid,
        "name": name,
        "seats": seats,
        "status": "AVAILABLE",
        "active": True,
        "orderIds": []
    })
    for lang, trans_name in trans.items():
        table_translations.append({
            "id": f"tt-{tid}-{lang}",
            "table": {"id": tid},
            "language": lang,
            "name": trans_name
        })

# Payment config
payment_configs = [
    {
        "id": "default",
        "activeModes": ["CASH", "CARD"],
        "moneyDenominations": [
            {"value": 500.0, "image": "/images/euro_50.png", "type": "BANKNOTE"},
            {"value": 200.0, "image": "/images/euro_50.png", "type": "BANKNOTE"},
            {"value": 100.0, "image": "/images/euro_50.png", "type": "BANKNOTE"},
            {"value": 50.0, "image": "/images/euro_50.png", "type": "BANKNOTE"},
            {"value": 20.0, "image": "/images/euro_20.png", "type": "BANKNOTE"},
            {"value": 10.0, "image": "/images/euro_10.png", "type": "BANKNOTE"}
        ],
        "active": True
    }
]

data_export = {
    "formatVersion": 1,
    "groups": groups_list,
    "products": products_list,
    "customizations": customizations_list,
    "customizationOptions": customization_options_list,
    "tables": tables_list,
    "discountRules": [],
    "orders": [],
    "users": [],
    "paymentConfigs": payment_configs,
    "groupTranslations": group_translations,
    "productTranslations": product_translations,
    "customizationTranslations": customization_translations,
    "customizationOptionTranslations": customization_option_translations,
    "discountRuleTranslations": [],
    "tableTranslations": table_translations
}

with open("build_michelin_demo/data.json", "w", encoding="utf-8") as f:
    json.dump(data_export, f, indent=2, ensure_ascii=False)

print("Generated clean, schema-compliant data.json and template.json for michelin_demo!")
